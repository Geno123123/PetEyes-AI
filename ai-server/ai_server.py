
import os
import io
import base64
from dotenv import load_dotenv
load_dotenv()

from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, Dict, Any

from PIL import Image, ImageOps
from ultralytics import YOLO
import numpy as np
import torch
import timm
from torchvision import transforms

import chromadb
from chromadb.utils import embedding_functions
from groq import Groq


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(BASE_DIR, "models")
CHROMA_DB_PATH = os.path.join(BASE_DIR, "chroma_db")

GROQ_API_KEY = os.environ.get("GROQ_API_KEY")
CHROMA_COLLECTION = "pet_eye_disease"
YOLO_MODEL_PATH = os.path.join(MODEL_DIR, "yolo_best.pt")


EFFNET_PATHS = {
    "dog_binary": os.path.join(MODEL_DIR, "effnet_b3_dog_binary.pth"),
    "cat_binary": os.path.join(MODEL_DIR, "effnet_b3_cat_binary_origin_v2.pth"),

    "dog_group": os.path.join(MODEL_DIR, "effnet_b3_dog_group_origin_v2_retry.pth"),
    "cat_group": os.path.join(MODEL_DIR, "effnet_b3_cat_group_origin_v2.pth"),

    "dog_detail_각막질환": os.path.join(MODEL_DIR, "effnet_b3_dog_detail_각막질환_v3.pth"),
    "dog_detail_수정체질환": os.path.join(MODEL_DIR, "effnet_b3_dog_detail_수정체질환_v3.pth"),
    "dog_detail_외안부계열분류": os.path.join(MODEL_DIR, "effnet_b3_dog_detail_외안부계열분류_v3.pth"),
    "dog_detail_결막눈물계": os.path.join(MODEL_DIR, "effnet_b3_dog_detail_결막눈물계_v3.pth"),
    "dog_detail_안검계": os.path.join(MODEL_DIR, "effnet_b3_dog_detail_안검계_v3.pth"),

    "cat_detail_각막질환": os.path.join(MODEL_DIR, "effnet_b3_cat_detail_각막질환_v3.pth"),
    "cat_detail_외안부질환": os.path.join(MODEL_DIR, "effnet_b3_cat_detail_외안부질환_v3.pth"),
}


GROUP_TO_DETAIL_KEY = {
    "dog": {
        "각막질환": "dog_detail_각막질환",
        "수정체질환": "dog_detail_수정체질환",
    },
    "cat": {
        "각막질환": "cat_detail_각막질환",
        "외안부질환": "cat_detail_외안부질환",
    },
}


SEVERITY_WEIGHTS = {
    "결막염": (0.7, 0.3),
    "궤양성각막질환": (0.7, 0.3),
    "비궤양성각막질환": (0.7, 0.3),
    "백내장": (0.7, 0.3),
    "색소침착성각막염": (0.7, 0.3),
    "유루증": (0.7, 0.3),

    "각막궤양": (0.7, 0.3),
    "각막부골편": (0.7, 0.3),
    "비궤양성각막염": (0.7, 0.3),

    "핵경화": (0.5, 0.5),
    "안검염": (0.5, 0.5),
    "안검내반증": (0.0, 1.0),
    "안검종양": (0.0, 1.0),
    "정상": (1.0, 0.0),
}

IMG_SIZE = 300
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


chroma_collection = None
groq_client = None
yolo_model = None
effnet_models = {}


class ResizeWithPad:
    def __init__(self, size, fill=0):
        self.size = size
        self.fill = fill

    def __call__(self, img):
        w, h = img.size
        scale = self.size / max(w, h)

        new_w = int(round(w * scale))
        new_h = int(round(h * scale))

        img = img.resize((new_w, new_h), Image.Resampling.BICUBIC)

        pad_w = self.size - new_w
        pad_h = self.size - new_h

        left = pad_w // 2
        top = pad_h // 2

        return ImageOps.expand(
            img,
            border=(left, top, pad_w - left, pad_h - top),
            fill=self.fill,
        )

classify_tf = transforms.Compose([
    ResizeWithPad(IMG_SIZE, fill=0),
    transforms.ToTensor(),
    transforms.Normalize(
        [0.485, 0.456, 0.406],
        [0.229, 0.224, 0.225],
    ),
])


@asynccontextmanager
async def lifespan(app: FastAPI):
    global chroma_collection, groq_client, yolo_model, effnet_models

    if os.path.exists(YOLO_MODEL_PATH):
        yolo_model = YOLO(YOLO_MODEL_PATH)
        print("✅ YOLO 모델 로드 완료")
    else:
        print(f"⚠️ YOLO 모델 없음: {YOLO_MODEL_PATH}")

    for name, path in EFFNET_PATHS.items():
        if not os.path.exists(path):
            print(f"⚠️ EfficientNet [{name}] 없음: {path}")
            continue

        try:
            ckpt = torch.load(path, map_location=DEVICE, weights_only=False)

            model = timm.create_model(
                "efficientnet_b3",
                pretrained=False,
                num_classes=ckpt["num_classes"],
            )

            model.load_state_dict(ckpt["model_state"])
            model.to(DEVICE).eval()

            class_to_idx = ckpt["class_to_idx"]

            effnet_models[name] = {
                "model": model,
                "class_to_idx": class_to_idx,
                "idx_to_class": {v: k for k, v in class_to_idx.items()},
            }

            print(f"✅ EfficientNet [{name}] 로드 ({ckpt['num_classes']}클래스)")

        except Exception as e:
            print(f"❌ EfficientNet [{name}] 실패: {e}")

    emb_fn = embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name="snunlp/KR-ELECTRA-discriminator"
    )

    client = chromadb.PersistentClient(path=CHROMA_DB_PATH)

    chroma_collection = client.get_collection(
        name=CHROMA_COLLECTION,
        embedding_function=emb_fn,
    )

    print(f"✅ ChromaDB 로드 완료 — {chroma_collection.count()}개 청크")

    if GROQ_API_KEY:
        groq_client = Groq(api_key=GROQ_API_KEY)
        print("✅ Groq 초기화 완료")
    else:
        print("❌ GROQ_API_KEY 없음")

    print(f"\n🚀 눈봄 AI 서버 v5.3 준비 완료! device: {DEVICE}")

    yield

    print("서버 종료")


app = FastAPI(
    title="눈봄 AI 서버",
    description="반려동물 안구질환 AI 진단 — v3 계층형 EfficientNet 구조",
    version="0.5.3",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class ReportRequest(BaseModel):
    pet_type: str
    disease: str
    severity_label: str
    score: float

class ReportResponse(BaseModel):
    pet_type: str
    disease: str
    severity_label: str
    score: float
    rag_chunks: int
    report: str

class DiagnoseResponse(BaseModel):
    detected: bool


    confidence: float

    pet_type: str
    binary_result: str
    disease_group: Optional[str] = None
    disease: str


    severity_label: str
    score: float
    score_text: Optional[str] = None


    image_score: Optional[float] = None
    survey_score: Optional[float] = None


    rgb_hsv_score: Optional[float] = None
    rgb_hsv_score_text: Optional[str] = None
    rgb_hsv_severity_label: Optional[str] = None
    rgb_hsv_analysis: Optional[Dict[str, Any]] = None


    eye_detection_confidence: Optional[float] = None
    crop_method: Optional[str] = None

    report: str
    cropped_image: str

class KeywordRequest(BaseModel):
    hospital_id: int
    reviews: list[str]

class KeywordResponse(BaseModel):
    hospital_id: int
    keywords: list[str]


def effnet_predict(model_key, pil_img):
    if model_key not in effnet_models:
        return None, 0.0

    info = effnet_models[model_key]

    tensor = classify_tf(pil_img).unsqueeze(0).to(DEVICE)

    with torch.no_grad():
        logits = info["model"](tensor)
        probs = torch.softmax(logits, dim=1)[0]
        conf, idx = probs.max(0)

    label = info["idx_to_class"][idx.item()]
    confidence = round(float(conf.item()), 4)

    return label, confidence

def yolo_crop(img, yolo_model):
    w, h = img.size
    img_area = w * h

    if min(w, h) < 500:
        return img, 0.0, "fallback_full_small_image"

    results = yolo_model(img, verbose=False)
    boxes = results[0].boxes

    if boxes is None or len(boxes) == 0:
        return img, 0.0, "fallback_full_no_yolo"

    confs = boxes.conf.cpu().numpy()
    best_idx = confs.argmax()

    box = boxes.xyxy[best_idx].cpu().numpy()
    yolo_conf = float(boxes.conf[best_idx].item())

    x1, y1, x2, y2 = box

    box_w = x2 - x1
    box_h = y2 - y1
    box_area = box_w * box_h
    area_ratio = box_area / img_area

    if yolo_conf < 0.35:
        return img, round(yolo_conf, 4), f"fallback_full_low_conf_{area_ratio:.4f}"

    if area_ratio < 0.05:
        return img, round(yolo_conf, 4), f"fallback_full_too_small_{area_ratio:.4f}"

    if area_ratio > 0.20:
        return img, round(yolo_conf, 4), f"fallback_full_closeup_{area_ratio:.4f}"

    pad_ratio = 0.5

    mx = box_w * pad_ratio
    my = box_h * pad_ratio

    nx1 = max(0, int(x1 - mx))
    ny1 = max(0, int(y1 - my))
    nx2 = min(w, int(x2 + mx))
    ny2 = min(h, int(y2 + my))

    eye = img.crop((nx1, ny1, nx2, ny2))

    cw, ch = eye.size

    if min(cw, ch) < 180:
        return img, round(yolo_conf, 4), f"fallback_full_crop_too_small_{area_ratio:.4f}"

    return eye, round(yolo_conf, 4), f"yolo_crop_padded_{area_ratio:.4f}"

def calc_survey_score(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10):
    filled = [
        s for s in [q1, q2, q3, q4, q5, q6, q7, q8, q9, q10]
        if s > 0
    ]

    if not filled:
        return None

    return round(sum((s - 1) / 4.0 for s in filled) / len(filled), 4)

def severity_label_from_score(score):
    if score < 25:
        return "정상 범위"
    if score < 50:
        return "경증"
    if score < 75:
        return "중증"
    return "위험"

def calc_conjunctivitis_score_by_ratio(ratio):
    if ratio < 0.05:
        score = ratio / 0.05 * 0.30
    elif ratio < 0.20:
        score = 0.30 + ((ratio - 0.05) / 0.15) * 0.35
    elif ratio < 0.35:
        score = 0.65 + ((ratio - 0.20) / 0.15) * 0.25
    else:
        score = 0.90 + min((ratio - 0.35) / 0.20, 1) * 0.10

    return min(max(score, 0.0), 1.0)

def calc_rgb_hsv_severity(eye_pil, disease):
    import cv2

    base_analysis = {
        "target": disease,
        "color_space": "PIL RGB → OpenCV BGR → HSV",
        "method": None,
        "ratio": None,
        "raw_score": None,
        "score_percent": None,
        "note": None,
    }

    if disease == "정상":
        base_analysis["method"] = "none"
        base_analysis["note"] = "정상 예측이므로 RGB/HSV 중증도 계산을 적용하지 않음"
        return {
            "image_score": None,
            "rgb_hsv_score": None,
            "rgb_hsv_severity_label": None,
            "rgb_hsv_analysis": base_analysis,
        }

    if disease in ("안검내반증", "안검종양"):
        base_analysis["method"] = "not_supported"
        base_analysis["note"] = "해당 질환은 현재 이미지 색상 기반 중증도보다 문진 기반 판단을 우선함"
        return {
            "image_score": None,
            "rgb_hsv_score": None,
            "rgb_hsv_severity_label": None,
            "rgb_hsv_analysis": base_analysis,
        }

    image_weight, _ = SEVERITY_WEIGHTS.get(disease, (0.7, 0.3))

    if image_weight == 0.0:
        base_analysis["method"] = "image_weight_zero"
        base_analysis["note"] = "해당 질환은 이미지 기반 중증도 가중치가 0임"
        return {
            "image_score": None,
            "rgb_hsv_score": None,
            "rgb_hsv_severity_label": None,
            "rgb_hsv_analysis": base_analysis,
        }

    arr = np.array(eye_pil)[:, :, :3]

    bgr = cv2.cvtColor(arr, cv2.COLOR_RGB2BGR)
    hsv = cv2.cvtColor(bgr, cv2.COLOR_BGR2HSV)

    total = bgr.shape[0] * bgr.shape[1]

    image_score = None
    ratio = None
    method = None
    note = None

    if "결막염" in disease:
        method = "HSV red mask calibrated"



        m1 = cv2.inRange(hsv, (0, 70, 60), (10, 255, 255))
        m2 = cv2.inRange(hsv, (165, 70, 60), (180, 255, 255))

        red_pixels = cv2.countNonZero(m1) + cv2.countNonZero(m2)
        ratio = red_pixels / total

        image_score = calc_conjunctivitis_score_by_ratio(ratio)

        note = "HSV 붉은 영역 비율을 완화된 기준으로 보정하여 결막 충혈 정도를 추정"

    elif "백내장" in disease:
        method = "HSV low saturation bright mask"

        m = cv2.inRange(hsv, (0, 0, 160), (180, 60, 255))

        ratio = cv2.countNonZero(m) / total
        image_score = min(ratio * 3, 1)

        note = "HSV에서 밝고 채도 낮은 영역 비율을 이용해 혼탁 정도를 추정"

    elif "색소침착" in disease:
        method = "HSV dark pigment mask"

        m = cv2.inRange(hsv, (0, 30, 0), (180, 255, 80))

        ratio = cv2.countNonZero(m) / total
        image_score = min(ratio * 4, 1)

        note = "HSV에서 어두운 색소 영역 비율을 이용해 색소침착 정도를 추정"

    elif "유루증" in disease:
        method = "HSV brown tear stain mask"

        m = cv2.inRange(hsv, (10, 40, 50), (30, 255, 200))

        ratio = cv2.countNonZero(m) / total
        image_score = min(ratio * 6, 1)

        note = "HSV에서 갈색/착색 영역 비율을 이용해 눈물자국 정도를 추정"

    else:
        method = "grayscale variance"

        gray = cv2.cvtColor(bgr, cv2.COLOR_BGR2GRAY)

        variance = float(gray.var())
        ratio = variance / 10000
        image_score = min(ratio, 1)

        note = "기타 질환은 grayscale 분산으로 표면 혼탁/질감 변화를 근사 추정"

    image_score = round(float(image_score), 4)
    rgb_hsv_score = round(image_score * 100, 2)
    rgb_hsv_label = severity_label_from_score(rgb_hsv_score)

    base_analysis.update({
        "method": method,
        "ratio": round(float(ratio), 6) if ratio is not None else None,
        "raw_score": image_score,
        "score_percent": rgb_hsv_score,
        "note": note,
    })

    return {
        "image_score": image_score,
        "rgb_hsv_score": rgb_hsv_score,
        "rgb_hsv_severity_label": rgb_hsv_label,
        "rgb_hsv_analysis": base_analysis,
    }

def combine_severity(disease, img_s, surv_s):
    if disease == "정상":
        return 0.00, "정상 범위"

    image_weight, survey_weight = SEVERITY_WEIGHTS.get(disease, (0.7, 0.3))

    if img_s is None and surv_s is None:
        raw_score = 0.50

    elif img_s is None:
        raw_score = float(surv_s)

    elif surv_s is None:

        raw_score = float(img_s) * 0.85

    else:
        raw_score = float(img_s) * image_weight + float(surv_s) * survey_weight

    score = round(raw_score * 100, 2)
    severity_label = severity_label_from_score(score)

    return score, severity_label

def retrieve_rag(disease, pet_type):
    pet_kor = "반려견" if pet_type == "dog" else "반려묘"

    result = chroma_collection.query(
        query_texts=[f"{pet_kor} {disease} 증상 원인 치료 관리"],
        n_results=6,
        where={
            "$and": [
                {"disease": disease},
                {"species": pet_type},
            ]
        },
    )

    docs = result.get("documents", [[]])[0]

    return "\n\n".join(docs), len(docs)

def gen_report(pet_type, disease, sev_label, score, rag_ctx):
    pet_kor = "반려견" if pet_type == "dog" else "반려묘"

    sys_p = """당신은 수의사를 보조하는 AI입니다.
반려동물 안구질환 진단 결과를 바탕으로 보호자가 동물병원에 제출할 문진서를 작성합니다.
다음 항목을 반드시 포함하세요:
1. 진단 요약 (질환명, 중증도)
2. 주요 증상 설명
3. 예상 원인
4. 권장 치료 방향
5. 가정 내 관리 방법
6. 병원 방문 긴급도 (즉시/1주일 내/정기검진)
*, **, #, - 등 모든 마크다운 기호를 절대 사용하지 마세요. 번호(1. 2. 3.)와 줄바꿈만 사용하세요.
반드시 마지막에 'AI 진단 결과는 참고용이며 수의사 진료를 권장합니다.' 문구를 포함하세요.
수의학 DB에 없는 처방, 투약, 수술 내용은 절대 생성하지 마세요."""

    usr_p = f"""다음 정보를 바탕으로 문진서를 작성해주세요.

[진단 정보]
- 동물 종류: {pet_kor}
- 진단 질환: {disease}
- 중증도: {sev_label} (점수: {score}/100)

[수의학 DB 참고 정보]
{rag_ctx}"""

    resp = groq_client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {"role": "system", "content": sys_p},
            {"role": "user", "content": usr_p},
        ],
        max_tokens=1000,
        temperature=0.3,
    )

    return resp.choices[0].message.content.strip()




@app.post("/diagnose", response_model=DiagnoseResponse, summary="안구 사진 진단")
async def diagnose(
    pet_type: str = Form(default="dog"),
    image: UploadFile = File(...),
    q1: int = Form(default=0, description="눈 긁기/비빔 (1~5, 0=미응답)"),
    q2: int = Form(default=0, description="눈곱 정도"),
    q3: int = Form(default=0, description="충혈 정도"),
    q4: int = Form(default=0, description="눈물 정도"),
    q5: int = Form(default=0, description="눈꺼풀 부기"),
    q6: int = Form(default=0, description="눈 흐림"),
    q7: int = Form(default=0, description="눈 주변 털빠짐"),
    q8: int = Form(default=0, description="시력 이상"),
    q9: int = Form(default=0, description="눈물 자국 착색"),
    q10: int = Form(default=0, description="증상 지속 기간"),
):
    if not yolo_model:
        raise HTTPException(500, "YOLO 모델이 로드되지 않았습니다.")

    if not groq_client:
        raise HTTPException(500, "Groq API 키가 설정되지 않았습니다.")

    if pet_type not in ("dog", "cat"):
        raise HTTPException(400, "pet_type은 'dog' 또는 'cat'만 허용됩니다.")

    contents = await image.read()

    try:
        img = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception:
        raise HTTPException(400, "이미지를 열 수 없습니다.")


    eye, eye_detection_confidence, crop_method = yolo_crop(img, yolo_model)

    buf = io.BytesIO()
    eye.save(buf, format="JPEG")
    crop_b64 = base64.b64encode(buf.getvalue()).decode()


    b_key = f"{pet_type}_binary"
    g_key = f"{pet_type}_group"

    binary_result = "비정상"
    binary_conf = 0.0

    if b_key in effnet_models:
        binary_result, binary_conf = effnet_predict(b_key, eye)

    if binary_result == "정상":
        rgb_hsv = calc_rgb_hsv_severity(eye, "정상")
        score, severity_label = combine_severity("정상", None, None)

        print("========== DIAGNOSE DEBUG ==========")
        print("crop_method:", crop_method)
        print("eye_detection_confidence:", eye_detection_confidence)
        print("binary_result:", binary_result, "binary_conf:", binary_conf)
        print("disease: 정상 final_conf:", binary_conf)
        print("rgb_hsv:", rgb_hsv)

        return DiagnoseResponse(
            detected=True,
            confidence=binary_conf,
            pet_type=pet_type,
            binary_result="정상",
            disease_group=None,
            disease="정상",
            severity_label=severity_label,
            score=score,
            score_text=f"{score:.2f}",
            image_score=rgb_hsv["image_score"],
            survey_score=None,
            rgb_hsv_score=rgb_hsv["rgb_hsv_score"],
            rgb_hsv_score_text=None,
            rgb_hsv_severity_label=rgb_hsv["rgb_hsv_severity_label"],
            rgb_hsv_analysis=rgb_hsv["rgb_hsv_analysis"],
            eye_detection_confidence=eye_detection_confidence,
            crop_method=crop_method,
            report="정상 소견입니다. 정기적인 검진을 권장합니다.\n\nAI 진단 결과는 참고용이며 수의사 진료를 권장합니다.",
            cropped_image=crop_b64,
        )

    disease_group = None
    group_conf = 0.0

    if g_key in effnet_models:
        disease_group, group_conf = effnet_predict(g_key, eye)

    disease = disease_group or "질환 의심"
    final_conf = group_conf

    if disease_group:
        if pet_type == "dog" and disease_group == "외안부질환":
            route, route_conf = effnet_predict("dog_detail_외안부계열분류", eye)
            final_conf = route_conf or group_conf

            if route == "결막_눈물계":
                detail, detail_conf = effnet_predict("dog_detail_결막눈물계", eye)

                if detail:
                    disease = detail
                    final_conf = detail_conf

            elif route == "안검계":
                detail, detail_conf = effnet_predict("dog_detail_안검계", eye)

                if detail:
                    disease = detail
                    final_conf = detail_conf

            else:
                disease = route or disease_group
                final_conf = route_conf or group_conf

        else:
            detail_key = GROUP_TO_DETAIL_KEY.get(pet_type, {}).get(disease_group)

            if detail_key and detail_key in effnet_models:
                detail, detail_conf = effnet_predict(detail_key, eye)

                if detail:
                    disease = detail
                    final_conf = detail_conf


    survey_score = calc_survey_score(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10)

    rgb_hsv = calc_rgb_hsv_severity(eye, disease)

    image_score = rgb_hsv["image_score"]
    rgb_hsv_score = rgb_hsv["rgb_hsv_score"]
    rgb_hsv_severity_label = rgb_hsv["rgb_hsv_severity_label"]
    rgb_hsv_analysis = rgb_hsv["rgb_hsv_analysis"]

    score, severity_label = combine_severity(disease, image_score, survey_score)


    rag_ctx, _ = retrieve_rag(disease, pet_type)
    report = gen_report(pet_type, disease, severity_label, score, rag_ctx)

    print("========== DIAGNOSE DEBUG ==========")
    print("crop_method:", crop_method)
    print("eye_detection_confidence:", eye_detection_confidence)
    print("binary_result:", binary_result, "binary_conf:", binary_conf)
    print("disease_group:", disease_group, "group_conf:", group_conf)
    print("disease:", disease, "final_conf:", final_conf)
    print("rgb_hsv_score:", rgb_hsv_score, "rgb_hsv_label:", rgb_hsv_severity_label)
    print("rgb_hsv_analysis:", rgb_hsv_analysis)
    print("severity:", severity_label, "score:", score, "image_score:", image_score, "survey_score:", survey_score)

    return DiagnoseResponse(
        detected=True,
        confidence=final_conf,
        pet_type=pet_type,
        binary_result=binary_result,
        disease_group=disease_group,
        disease=disease,
        severity_label=severity_label,
        score=score,
        score_text=f"{score:.2f}",
        image_score=image_score,
        survey_score=survey_score,
        rgb_hsv_score=rgb_hsv_score,
        rgb_hsv_score_text=f"{rgb_hsv_score:.2f}" if rgb_hsv_score is not None else None,
        rgb_hsv_severity_label=rgb_hsv_severity_label,
        rgb_hsv_analysis=rgb_hsv_analysis,
        eye_detection_confidence=eye_detection_confidence,
        crop_method=crop_method,
        report=report,
        cropped_image=crop_b64,
    )




@app.post("/report", response_model=ReportResponse, summary="문진서 생성")
async def create_report(req: ReportRequest):
    if not groq_client:
        raise HTTPException(500, "Groq API 키가 설정되지 않았습니다.")

    if req.pet_type not in ("dog", "cat"):
        raise HTTPException(400, "pet_type은 'dog' 또는 'cat'만 허용됩니다.")

    ctx, cnt = retrieve_rag(req.disease, req.pet_type)

    if not ctx:
        raise HTTPException(404, f"'{req.disease}' 정보를 찾을 수 없습니다.")

    rpt = gen_report(req.pet_type, req.disease, req.severity_label, req.score, ctx)

    return ReportResponse(
        pet_type=req.pet_type,
        disease=req.disease,
        severity_label=req.severity_label,
        score=req.score,
        rag_chunks=cnt,
        report=rpt,
    )




@app.post("/extract-keywords", response_model=KeywordResponse, summary="리뷰 키워드 추출")
async def extract_keywords(req: KeywordRequest):
    if not groq_client:
        raise HTTPException(500, "Groq API 키가 설정되지 않았습니다.")

    if not req.reviews:
        raise HTTPException(400, "리뷰가 비어있습니다.")

    combined = "\n".join(req.reviews[-50:])

    resp = groq_client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {
                "role": "user",
                "content": f"""다음 동물병원 리뷰들에서 자주 언급되는 키워드 10개를 추출해주세요.
키워드는 짧은 명사 또는 형용사로, 쉼표로 구분해서만 출력하세요.
예시: 친절함, 대기시간, 주차, 청결, 야간진료

리뷰:
{combined}

키워드만 출력:""",
            }
        ],
        max_tokens=100,
        temperature=0.3,
    )

    keywords = [
        keyword.strip()
        for keyword in resp.choices[0].message.content.strip().split(",")
        if keyword.strip()
    ]

    return KeywordResponse(
        hospital_id=req.hospital_id,
        keywords=keywords,
    )




@app.get("/health", summary="서버 상태 확인")
async def health():
    return {
        "status": "ok",
        "device": str(DEVICE),
        "yolo": yolo_model is not None,
        "chroma_chunks": chroma_collection.count() if chroma_collection else 0,
        "groq": groq_client is not None,
        "effnet_loaded": list(effnet_models.keys()),
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "ai_server:app",
        host="0.0.0.0",
        port=8001,
        reload=True,
    )
