# AI Server

반려동물 안구질환 AI 진단 서버입니다. (FastAPI)

## 주요 기능

- YOLOv8 기반 안구 영역 검출
- EfficientNet/ConvNeXt 기반 안구질환 분류
- RGB/HSV 기반 이미지 중증도 분석
- 증상 설문 점수 결합
- ChromaDB RAG 기반 문진서 생성
- Groq API 기반 보호자 안내문 생성

## 실행

```bash
pip install -r requirements.txt
cp .env.example .env   # GROQ_API_KEY 값을 채워 넣으세요
python setup_ragChroma.py   # ChromaDB(RAG) 최초 1회 구축
uvicorn ai_server:app --host 0.0.0.0 --port 8000
```

## 실행 전 준비

아래 파일 및 폴더는 GitHub에 포함하지 않으며, 서버 실행 환경에 별도로 배치해야 합니다.

- `.env` (`.env.example` 참고)
- `models/` — 모델 가중치(YOLOv8n, EfficientNet-B3 등)
- `chroma_db/` — `setup_ragChroma.py` 실행으로 생성

## 엔드포인트

- `POST /report` — 문진서(진단 리포트) 생성
- `POST /extract-keywords` — 리뷰 텍스트 키워드 추출
- `GET /health` — 헬스체크

백엔드([`../backend`](../backend))의 `ai` 패키지가 이 서버를 호출하는 클라이언트 역할을 합니다.
