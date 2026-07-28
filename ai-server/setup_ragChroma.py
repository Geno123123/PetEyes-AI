"""
ChromaDB RAG 초기화 스크립트 (v3)
- 반려견/반려묘 species 메타데이터 유지
- 질환별 source_name/source_url/source_language 메타데이터 추가
- 질환 설명 본문과 출처 메타데이터 분리
- 반려견 11개 질환 × 6항목 = 66개 청크
- 반려묘 6개 질환 × 6항목 = 36개 청크
- 합계 102개 청크

주의:
- 본 문서는 보호자 안내 및 문진서 생성 보조용입니다.
- 확정 진단, 처방, 약물 사용 지시는 수의사 진료를 대체하지 않습니다.
- ChromaDB metadata에는 문자열 기반 출처 정보를 저장합니다.
"""

import chromadb
from chromadb.utils import embedding_functions

CHROMA_DB_PATH = "chroma_db"
CHROMA_COLLECTION = "pet_eye_disease"

# ─────────────────────────────────────────────
# 질환별 출처 메타데이터
# ─────────────────────────────────────────────
DISEASE_SOURCES = {
    "dog": {
        "정상": {
            "source_name": "AI Hub - 반려동물 안구질환 데이터",
            "source_url": "https://aihub.or.kr/aihubdata/data/view.do?dataSetSn=562",
            "source_language": "ko",
        },
        "결막염": {
            "source_name": "VCA Animal Hospitals - Conjunctivitis in Dogs / MSD Veterinary Manual - The Conjunctiva in Animals",
            "source_url": "https://vcahospitals.com/know-your-pet/conjunctivitis-in-dogs | https://www.msdvetmanual.com/eye-diseases-and-disorders/ophthalmology/the-conjunctiva-in-animals",
            "source_language": "en",
        },
        "궤양성각막질환": {
            "source_name": "VCA Animal Hospitals - Corneal Ulcers in Dogs / MSD Veterinary Manual - The Cornea in Animals",
            "source_url": "https://vcahospitals.com/know-your-pet/corneal-ulcers-in-dogs | https://www.msdvetmanual.com/eye-diseases-and-disorders/ophthalmology/the-cornea-in-animals",
            "source_language": "en",
        },
        "백내장": {
            "source_name": "VCA Animal Hospitals - Cataracts in Dogs / Cornell Riney Canine Health Center - Canine Cataracts",
            "source_url": "https://vcahospitals.com/know-your-pet/cataracts-in-dogs | https://www.vet.cornell.edu/departments-centers-and-institutes/riney-canine-health-center/canine-health-topics/canine-cataracts",
            "source_language": "en",
        },
        "비궤양성각막질환": {
            "source_name": "MSD Veterinary Manual - The Cornea in Animals / MSD Veterinary Manual - Disorders of the Cornea in Dogs",
            "source_url": "https://www.msdvetmanual.com/eye-diseases-and-disorders/ophthalmology/the-cornea-in-animals | https://www.msdvetmanual.com/dog-owners/eye-disorders-of-dogs/disorders-of-the-cornea-in-dogs",
            "source_language": "en",
        },
        "색소침착성각막염": {
            "source_name": "MSD Veterinary Manual - The Cornea in Animals / VCA Animal Hospitals - Eyelid Entropion in Dogs",
            "source_url": "https://www.msdvetmanual.com/eye-diseases-and-disorders/ophthalmology/the-cornea-in-animals | https://vcahospitals.com/know-your-pet/eyelid-entropion-in-dogs",
            "source_language": "en",
        },
        "안검내반증": {
            "source_name": "VCA Animal Hospitals - Eyelid Entropion in Dogs / MSD Veterinary Manual - Disorders of the Eyelids in Dogs",
            "source_url": "https://vcahospitals.com/know-your-pet/eyelid-entropion-in-dogs | https://www.msdvetmanual.com/dog-owners/eye-disorders-of-dogs/disorders-of-the-eyelids-in-dogs",
            "source_language": "en",
        },
        "안검염": {
            "source_name": "VCA Animal Hospitals - Blepharitis in Dogs / Today’s Veterinary Practice - Clinical Approach to Canine Eyelid Disease: Blepharitis",
            "source_url": "https://vcahospitals.com/know-your-pet/blepharitis-in-dogs | https://todaysveterinarypractice.com/ophthalmology/observations-in-ophthalmology-clinical-approach-to-canine-eyelid-disease-blepharitis/",
            "source_language": "en",
        },
        "안검종양": {
            "source_name": "Today’s Veterinary Practice - Diagnosis and Treatment of Eyelid Tumors / MSD Veterinary Manual - Eyelids in Animals",
            "source_url": "https://todaysveterinarypractice.com/ophthalmology/eyelid-tumors-dogs-cats/ | https://www.msdvetmanual.com/eye-diseases-and-disorders/ophthalmology/eyelids-in-animals",
            "source_language": "en",
        },
        "유루증": {
            "source_name": "VCA Animal Hospitals - Eye Discharge or Epiphora in Dogs / VIN Veterinary Partner - Runny Eyes",
            "source_url": "https://vcahospitals.com/know-your-pet/eye-discharge-or-epiphora-in-dogs | https://veterinarypartner.vin.com/default.aspx?id=4951527&pid=19239",
            "source_language": "en",
        },
        "핵경화": {
            "source_name": "VCA Animal Hospitals - Lenticular Sclerosis in Dogs / Cornell Riney Canine Health Center - Canine Cataracts",
            "source_url": "https://vcahospitals.com/know-your-pet/lenticular-sclerosis-in-dogs | https://www.vet.cornell.edu/departments-centers-and-institutes/riney-canine-health-center/canine-health-topics/canine-cataracts",
            "source_language": "en",
        },
    },
    "cat": {
        "정상": {
            "source_name": "AI Hub - 반려동물 안구질환 데이터",
            "source_url": "https://aihub.or.kr/aihubdata/data/view.do?dataSetSn=562",
            "source_language": "ko",
        },
        "각막궤양": {
            "source_name": "Cornell Feline Health Center - Corneal Ulcers / VCA Animal Hospitals - Corneal Ulcers in Cats",
            "source_url": "https://www.vet.cornell.edu/departments-centers-and-institutes/cornell-feline-health-center/health-information/feline-health-topics/corneal-ulcers | https://vcahospitals.com/know-your-pet/corneal-ulcers-in-cats",
            "source_language": "en",
        },
        "각막부골편": {
            "source_name": "MSD Veterinary Manual - Disorders of the Cornea in Cats / MSD Veterinary Manual - Eye Disorders Resulting from Generalized Diseases in Cats",
            "source_url": "https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/disorders-of-the-cornea-in-cats | https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/eye-disorders-resulting-from-generalized-diseases-in-cats",
            "source_language": "en",
        },
        "결막염": {
            "source_name": "대한수의사회지 - 수의학 강좌: 고양이 결막염 / MSD Veterinary Manual - Eye Disorders Resulting from Generalized Diseases in Cats",
            "source_url": "https://koreascience.kr/article/JAKO200641848478621.page | https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/eye-disorders-resulting-from-generalized-diseases-in-cats",
            "source_language": "ko|en",
        },
        "비궤양성각막염": {
            "source_name": "MSD Veterinary Manual - Disorders of the Cornea in Cats / MSD Veterinary Manual - Eye Disorders Resulting from Generalized Diseases in Cats",
            "source_url": "https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/disorders-of-the-cornea-in-cats | https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/eye-disorders-resulting-from-generalized-diseases-in-cats",
            "source_language": "en",
        },
        "안검염": {
            "source_name": "MSD Veterinary Manual - Eye Disorders Resulting from Generalized Diseases in Cats / Merck Veterinary Manual - Eyelids in Animals",
            "source_url": "https://www.msdvetmanual.com/cat-owners/eye-disorders-of-cats/eye-disorders-resulting-from-generalized-diseases-in-cats | https://www.merckvetmanual.com/eye-diseases-and-disorders/ophthalmology/eyelids-in-animals",
            "source_language": "en",
        },
    },
}

# ─────────────────────────────────────────────
# 반려견 질환 데이터 (dog)
# ─────────────────────────────────────────────
DOG_DISEASE_DATA = {
    "결막염": {
        "증상": "결막염은 눈꺼풀 안쪽과 안구 표면 일부를 덮는 결막에 염증이 생긴 상태입니다. 눈의 충혈, 눈물 증가, 탁하거나 노란색·초록색 눈 분비물, 눈을 찡그림, 과도한 깜빡임, 눈 주변 발적 또는 부종이 나타날 수 있습니다.",
        "원인": "감염, 알레르기, 먼지·화학물질·연기 같은 환경 자극, 이물질, 안구건조증, 눈꺼풀이나 속눈썹 이상 등 다양한 원인으로 발생할 수 있습니다. 겉모습만으로 원인을 확정하기 어렵기 때문에 병력 확인과 안과 검사가 필요할 수 있습니다.",
        "치료": "치료는 원인에 따라 달라집니다. 세균 감염이 의심되면 수의사 판단에 따라 항생제 점안제나 안연고가 사용될 수 있고, 알레르기나 염증성 원인인 경우 항염증 치료가 고려될 수 있습니다. 각막궤양, 이물, 안구건조증 등 동반 질환이 있으면 함께 치료해야 합니다.",
        "관리": "눈 주변 분비물을 깨끗하게 닦아주고, 반려견이 눈을 비비지 않도록 주의합니다. 먼지, 연기, 강한 바람, 자극성 세정제 같은 환경 자극을 줄이고, 임의로 사람용 안약을 사용하지 않습니다.",
        "중증도기준": "경증은 가벼운 충혈이나 맑은 눈물, 소량의 분비물이 있는 상태입니다. 중등증은 노란색·초록색 분비물, 뚜렷한 발적, 부종, 눈 찡그림이 동반되는 경우입니다. 중증은 통증이 심하거나 눈을 잘 뜨지 못하고, 각막 혼탁·각막 손상·시력 이상이 의심되는 경우입니다.",
        "병원안내": "충혈, 분비물, 부종, 눈 찡그림이 지속되면 동물병원 방문을 권장합니다. 특히 노란색·초록색 분비물, 심한 통증, 각막 혼탁, 눈을 못 뜨는 증상, 시력 이상이 보이면 빠른 진료가 필요합니다.",
    },
    "궤양성각막질환": {
        "증상": "각막 표면에 상처나 궤양이 생긴 상태로, 눈을 찡그림, 빛을 피함, 눈물 증가, 눈 비빔, 각막 혼탁, 충혈이 나타날 수 있습니다. 깊은 궤양이나 진행성 궤양에서는 각막이 빠르게 약해질 수 있습니다.",
        "원인": "외상, 이물질, 속눈썹 또는 눈꺼풀 자극, 안구건조증, 감염, 기존 각막 질환 등이 원인이 될 수 있습니다. 모든 각막궤양은 2차 세균 오염이나 각막 기질 손상 위험이 있습니다.",
        "치료": "치료는 궤양의 깊이와 원인에 따라 달라집니다. 표층 궤양은 수의사 판단에 따라 점안 치료와 원인 교정이 시행될 수 있고, 깊거나 녹는 듯 진행하는 궤양은 세균 배양·세포검사와 전문적인 안과 치료 또는 수술적 처치가 필요할 수 있습니다.",
        "관리": "눈을 비비지 않도록 넥카라를 착용하고, 처방받은 점안제를 정해진 간격으로 사용해야 합니다. 임의로 스테로이드 안약을 사용하면 궤양이 악화될 수 있으므로 수의사 지시 없이 사용하지 않습니다.",
        "중증도기준": "경증은 표층 상피 손상과 제한적인 혼탁이 있는 경우입니다. 중등증은 통증, 충혈, 눈물, 혼탁이 뚜렷한 경우입니다. 중증은 깊은 궤양, 빠르게 커지는 궤양, 각막 천공 위험, 시력 저하가 의심되는 경우입니다.",
        "병원안내": "각막궤양은 악화 속도가 빠를 수 있으므로 의심 시 빠른 방문이 필요합니다. 눈을 잘 못 뜨거나 각막이 뿌옇고 통증이 심하면 즉시 진료를 권장합니다.",
    },
    "백내장": {
        "증상": "수정체가 불투명해져 동공 부위가 하얗거나 뿌옇게 보일 수 있습니다. 진행되면 시력 저하, 물체에 부딪힘, 계단이나 어두운 곳에서 주저함, 활동성 감소가 나타날 수 있습니다.",
        "원인": "유전, 노령 변화, 당뇨병, 눈 외상, 염증 등 여러 원인으로 발생할 수 있습니다. 강아지에서는 유전성 백내장과 당뇨 관련 백내장이 중요한 원인으로 알려져 있습니다.",
        "치료": "백내장의 진행 정도와 눈 내부 상태에 따라 치료 방향이 달라집니다. 근본적인 시력 회복 치료는 수술적 수정체 제거가 고려될 수 있으며, 수술 전에는 망막 기능과 동반 염증 여부 등 전문 검사가 필요합니다.",
        "관리": "시야가 떨어진 경우 가구 배치를 자주 바꾸지 않고, 계단이나 모서리에서 다치지 않도록 환경을 정리합니다. 당뇨가 있는 경우 혈당 관리가 중요하며, 눈의 충혈이나 통증이 생기면 빨리 진료를 받아야 합니다.",
        "중증도기준": "경증은 작은 국소 혼탁이나 시력 영향이 적은 상태입니다. 중등증은 혼탁 범위가 넓어지고 시야 저하가 의심되는 상태입니다. 중증은 수정체 대부분이 혼탁하거나 일상생활에서 시력 저하가 뚜렷한 상태입니다.",
        "병원안내": "눈이 뿌옇게 보이거나 시야 이상이 의심되면 안과 검진이 필요합니다. 갑작스러운 시력 저하, 통증, 충혈이 동반되면 빠른 방문을 권장합니다.",
    },
    "비궤양성각막질환": {
        "증상": "각막 혼탁, 표면 혈관 증가, 색소 변화, 회백색 또는 갈색 변화가 나타날 수 있습니다. 궤양이 없는 만성 각막염에서는 통증이 심하지 않을 수 있지만, 진행하면 시야를 가릴 수 있습니다.",
        "원인": "만성 자극, 면역 매개성 각막염, 자외선 노출, 건성안, 눈꺼풀 또는 속눈썹 이상, 전신 질환과 관련된 염증 등이 원인이 될 수 있습니다.",
        "치료": "원인과 병변 깊이에 따라 치료가 달라집니다. 수의사 판단에 따라 항염증 점안제, 면역 조절 점안제, 윤활제, 원인 질환 치료가 사용될 수 있습니다. 깊거나 진행성 병변은 전문 안과 진료가 필요합니다.",
        "관리": "장기 관리가 필요한 경우가 많으므로 점안 치료를 꾸준히 유지하고 정기적으로 병변 범위를 확인합니다. 자외선과 먼지 자극을 줄이고, 눈을 비비지 않도록 주의합니다.",
        "중증도기준": "경증은 주변부의 제한적인 혼탁이나 혈관 변화가 있는 상태입니다. 중등증은 혼탁과 혈관 변화가 확대되어 시야 일부를 방해할 수 있는 상태입니다. 중증은 중심부 침범, 뚜렷한 시력 저하, 통증 또는 동반 염증이 의심되는 상태입니다.",
        "병원안내": "각막 혼탁이나 혈관신생이 보이면 1~2주 내 진료를 권장합니다. 통증, 눈을 못 뜸, 급격한 혼탁 증가가 있으면 빠른 진료가 필요합니다.",
    },
    "색소침착성각막염": {
        "증상": "각막에 갈색 또는 흑갈색 색소가 침착되어 눈 표면이 어둡게 보일 수 있습니다. 병변이 중심부로 진행하면 시야를 가릴 수 있고, 눈물 증가나 만성 충혈이 동반될 수 있습니다.",
        "원인": "눈꺼풀 말림, 털 또는 속눈썹 자극, 안구건조증, 만성 염증, 단두종의 안면 구조 등 반복적인 각막 자극이 원인이 될 수 있습니다.",
        "치료": "원인 자극을 줄이는 것이 중요합니다. 수의사 판단에 따라 윤활제, 안구건조증 치료, 염증 조절, 눈꺼풀 문제 교정이 필요할 수 있습니다. 색소가 시야를 크게 가리면 전문 안과적 처치가 검토될 수 있습니다.",
        "관리": "눈 주변 털과 분비물을 관리하고, 눈 비빔을 줄이며, 건조하거나 자극적인 환경을 피합니다. 색소 범위가 넓어지는지 사진으로 주기적으로 비교하면 변화 확인에 도움이 됩니다.",
        "중증도기준": "경증은 각막 주변부에 국한된 색소 침착입니다. 중등증은 색소가 중심부 방향으로 확장되는 상태입니다. 중증은 동공 중심부를 가리거나 시력 저하가 의심되는 상태입니다.",
        "병원안내": "색소가 새로 보이거나 범위가 넓어지면 2주 내 진료를 권장합니다. 중심부 침범, 시력 저하, 통증이 있으면 빠른 진료가 필요합니다.",
    },
    "안검내반증": {
        "증상": "눈꺼풀 가장자리가 안쪽으로 말려 털이나 속눈썹이 각막을 자극하는 상태입니다. 눈물 증가, 눈 찡그림, 눈 비빔, 충혈, 각막 혼탁 또는 궤양이 동반될 수 있습니다.",
        "원인": "품종과 얼굴 형태에 따른 선천적 요인이 흔하며, 통증으로 인한 눈꺼풀 경련, 흉터, 만성 염증 이후에도 발생할 수 있습니다.",
        "치료": "지속적인 각막 자극이 있으면 수술적 교정이 근본 치료로 고려될 수 있습니다. 성장 중인 어린 개에서는 임시 교정이 사용될 수 있고, 동반된 각막 손상은 별도로 치료해야 합니다.",
        "관리": "진료 전까지 눈을 비비지 않도록 하고, 각막 자극 증상이 있으면 넥카라를 사용할 수 있습니다. 임의로 안약을 사용하기보다 각막 손상 여부를 먼저 확인해야 합니다.",
        "중증도기준": "경증은 눈물이나 가벼운 자극 증상만 있는 상태입니다. 중등증은 충혈, 눈 찡그림, 반복적인 비빔이 있는 상태입니다. 중증은 각막궤양, 혼탁, 심한 통증, 시력 이상이 동반되는 상태입니다.",
        "병원안내": "눈꺼풀이 안쪽으로 말려 보이거나 털이 눈에 닿으면 진료가 필요합니다. 각막 손상이나 통증이 있으면 즉시 방문을 권장합니다.",
    },
    "안검염": {
        "증상": "눈꺼풀 또는 눈꺼풀 가장자리에 염증이 생기는 상태입니다. 눈꺼풀이 붉고 붓고 가려워 보일 수 있으며, 눈을 찡그림, 과도한 깜빡임, 눈 주변 피부 변화, 분비물이 동반될 수 있습니다.",
        "원인": "세균·곰팡이·기생충 감염, 알레르기성 피부질환, 지루성 피부염, 면역 매개성 질환, 눈 주변 피부질환 등 다양한 원인이 가능합니다.",
        "치료": "원인에 따라 치료가 달라집니다. 감염성 원인은 적절한 항감염 치료가 필요할 수 있고, 알레르기나 면역성 원인은 염증 조절과 피부질환 관리가 함께 필요할 수 있습니다.",
        "관리": "눈꺼풀 주변을 깨끗하게 유지하고, 딱지나 분비물을 무리하게 떼지 않습니다. 눈을 긁거나 비비면 악화될 수 있으므로 필요 시 넥카라를 사용할 수 있습니다.",
        "중증도기준": "경증은 국소적인 발적과 가벼운 부종입니다. 중등증은 부종, 가려움, 분비물, 눈 찡그림이 뚜렷한 상태입니다. 중증은 광범위한 피부 손상, 통증, 각막 자극 또는 동반 결막염이 있는 상태입니다.",
        "병원안내": "눈꺼풀 발적이나 부종이 지속되면 1~2주 내 진료를 권장합니다. 통증, 심한 분비물, 피부 상처, 각막 이상이 보이면 빠른 진료가 필요합니다.",
    },
    "안검종양": {
        "증상": "눈꺼풀 가장자리나 주변에 혹, 결절, 돌출 병변, 색소 변화가 보일 수 있습니다. 병변이 각막을 문지르면 눈물, 충혈, 눈 찡그림, 분비물이 생길 수 있습니다.",
        "원인": "노령견에서 눈꺼풀 종괴가 흔히 발생할 수 있습니다. 개의 눈꺼풀 종양은 양성인 경우가 많지만, 모양만으로 양성·악성을 확정할 수 없으므로 검사가 필요합니다.",
        "치료": "크기, 위치, 성장 속도, 각막 자극 여부에 따라 경과 관찰, 절제, 조직검사 등이 고려됩니다. 각막을 자극하거나 빠르게 커지는 병변은 수의사의 진료가 필요합니다.",
        "관리": "혹의 크기와 모양을 사진으로 기록해 변화 여부를 관찰합니다. 반려견이 긁거나 비비지 않도록 하고, 출혈·궤양·분비물이 생기면 진료를 받습니다.",
        "중증도기준": "경증은 작고 변화가 적으며 각막 자극이 없는 상태입니다. 중등증은 크기 증가, 분비물, 눈물, 가벼운 각막 자극이 있는 상태입니다. 중증은 빠른 성장, 출혈, 궤양, 각막 손상, 시력 영향이 의심되는 상태입니다.",
        "병원안내": "새로운 눈꺼풀 혹이 보이면 진료를 통해 확인하는 것이 안전합니다. 빠르게 커지거나 각막을 자극하거나 출혈이 있으면 빠른 방문을 권장합니다.",
    },
    "유루증": {
        "증상": "눈물이 눈 밖으로 과도하게 흘러 눈 아래 털이 젖거나 갈색·붉은색으로 착색될 수 있습니다. 피부가 짓무르거나 냄새, 가려움이 동반될 수 있습니다.",
        "원인": "눈물 생산 증가 또는 눈물 배출 장애로 발생합니다. 눈물관 문제, 안구 표면 자극, 속눈썹·눈꺼풀 이상, 알레르기, 결막염, 각막질환, 단두종 구조 등이 관련될 수 있습니다.",
        "치료": "유루증은 하나의 증상으로, 원인에 따라 치료가 달라집니다. 눈물관 문제, 감염, 염증, 털 자극, 눈꺼풀 이상 등이 확인되면 해당 원인을 치료합니다.",
        "관리": "눈 아래 털을 깨끗하고 건조하게 유지하고, 분비물을 부드럽게 닦아줍니다. 눈 주변 피부가 짓무르지 않도록 관리하되, 원인 질환이 지속되면 병원 진료가 필요합니다.",
        "중증도기준": "경증은 가벼운 눈물 증가와 제한적인 착색입니다. 중등증은 지속적인 젖음, 뚜렷한 착색, 피부 자극이 있는 상태입니다. 중증은 피부염, 냄새, 통증, 심한 분비물 또는 동반 안과질환이 의심되는 상태입니다.",
        "병원안내": "눈물 자국이 지속되거나 피부 자극이 있으면 진료를 권장합니다. 충혈, 통증, 각막 혼탁, 노란색·초록색 분비물이 함께 있으면 빠른 진료가 필요합니다.",
    },
    "핵경화": {
        "증상": "노령견에서 수정체 중심부가 균일한 회청색 또는 푸른빛으로 보일 수 있습니다. 백내장과 달리 망막 반사가 보존되는 경우가 많고, 일반적으로 급격한 실명이나 통증은 동반되지 않습니다.",
        "원인": "나이가 들면서 수정체 섬유가 압축되고 밀도가 증가하는 정상적인 노화 변화와 관련됩니다. 백내장과 외관상 혼동될 수 있습니다.",
        "치료": "핵경화 자체는 일반적으로 치료가 필요하지 않습니다. 다만 백내장이나 다른 안과질환과 감별하기 위해 검진이 필요할 수 있습니다.",
        "관리": "시야 변화, 물체 충돌, 활동성 감소가 있는지 관찰합니다. 노령견은 정기 안과 검진을 통해 백내장이나 망막질환 동반 여부를 확인하는 것이 좋습니다.",
        "중증도기준": "정상 노화 범위는 회청색 혼탁이 있으나 통증이나 뚜렷한 시력 저하가 없는 상태입니다. 시력 저하나 혼탁 증가가 뚜렷하면 백내장 등 다른 질환 가능성을 고려해야 합니다.",
        "병원안내": "특이 증상이 없다면 정기검진 수준으로 관리합니다. 시력 저하, 충혈, 통증, 급격한 혼탁 변화가 보이면 빠른 검진을 권장합니다.",
    },
    "정상": {
        "증상": "현재 안구에서 뚜렷한 이상 소견이 관찰되지 않는 상태입니다. 과도한 충혈, 눈곱, 눈물, 각막 혼탁, 눈 비빔, 눈꺼풀 부종 등의 증상이 두드러지지 않습니다.",
        "원인": "질병성 원인이 확인되지 않은 정상 안구 상태입니다. 다만 촬영 환경, 조명 반사, 일시적인 눈물 등으로 인해 가벼운 변화가 보일 수 있으므로 지속적인 관찰이 필요합니다.",
        "치료": "특별한 치료는 필요하지 않습니다. 다만 갑작스러운 충혈, 눈곱 증가, 눈물 과다, 눈을 찡그림, 각막 혼탁 등이 새로 나타나면 동물병원 진료가 필요할 수 있습니다.",
        "관리": "눈 주변을 청결하게 유지하고, 목욕 후 샴푸나 이물질이 눈에 들어가지 않도록 주의합니다. 눈을 자주 비비거나 눈물·눈곱이 갑자기 증가하는지 정기적으로 관찰합니다.",
        "중증도기준": "정상 범위는 뚜렷한 충혈, 혼탁, 분비물, 눈꺼풀 부종, 통증 반응이 없는 상태입니다. 증상이 새로 발생하거나 지속될 경우 정상 범위를 벗어난 것으로 판단할 수 있습니다.",
        "병원안내": "특이 증상이 없다면 정기검진 수준으로 관리합니다. 충혈, 눈곱, 눈물, 혼탁, 눈 비빔, 시야 이상 등이 관찰되면 1주일 내 동물병원 방문을 권장합니다.",
    },
}

# ─────────────────────────────────────────────
# 반려묘 질환 데이터 (cat)
# ─────────────────────────────────────────────
CAT_DISEASE_DATA = {
    "각막궤양": {
        "증상": "각막 표면에 손상이나 궤양이 생긴 상태입니다. 고양이는 눈을 찡그리거나 비비고, 눈물이나 분비물이 증가하며, 각막이 뿌옇게 보이거나 밝은 빛을 피할 수 있습니다.",
        "원인": "외상, 이물질, 눈꺼풀 아래 자극물, 감염, 고양이 헤르페스바이러스 관련 각막질환, 건성안 등이 원인이 될 수 있습니다.",
        "치료": "궤양 깊이와 원인에 따라 치료가 달라집니다. 수의사 판단에 따라 항감염 점안제, 통증 조절, 보호용 넥카라, 항바이러스 치료 등이 고려될 수 있으며, 깊거나 진행성 궤양은 수술적 처치가 필요할 수 있습니다.",
        "관리": "눈을 비비지 않도록 하고, 스트레스를 줄이며, 처방받은 점안제를 꾸준히 사용합니다. 임의로 스테로이드 안약을 사용하지 않습니다.",
        "중증도기준": "경증은 표층 손상과 제한적인 혼탁입니다. 중등증은 뚜렷한 통증, 분비물, 혼탁이 있는 상태입니다. 중증은 깊은 궤양, 빠른 진행, 각막 천공 위험, 시력 이상이 의심되는 상태입니다.",
        "병원안내": "각막궤양은 통증과 악화 위험이 크므로 의심 시 빠른 진료가 필요합니다. 눈을 못 뜨거나 각막이 뿌옇고 분비물이 많으면 즉시 방문을 권장합니다.",
    },
    "각막부골편": {
        "증상": "고양이에서 특징적으로 나타나는 각막 질환으로, 각막 중심부 또는 주변부에 갈색~검은색의 혼탁한 병변이 보일 수 있습니다. 통증, 눈물, 눈 찡그림, 주변 각막 염증이 동반될 수 있습니다.",
        "원인": "만성 각막 자극, 고양이 헤르페스바이러스 관련 질환, 건성안, 품종적 요인 등이 관련될 수 있습니다. 각막 조직 일부가 어두워지고 괴사되는 질환입니다.",
        "치료": "초기 병변은 경과 관찰과 약물 치료가 고려될 수 있으나, 병변이 돌출되거나 통증이 심하거나 진행하면 수술적 제거가 필요할 수 있습니다.",
        "관리": "눈 비빔을 막고, 스트레스를 줄이며, 재발 가능성을 고려해 정기적으로 관찰합니다. 병변의 색과 크기 변화를 사진으로 기록하면 도움이 됩니다.",
        "중증도기준": "경증은 작고 얕은 갈색 병변입니다. 중등증은 병변이 커지거나 주변 염증이 동반되는 상태입니다. 중증은 검은색 병변이 돌출되거나 통증이 심하고 각막 깊은 층 침범이 의심되는 상태입니다.",
        "병원안내": "각막에 갈색 또는 검은색 병변이 보이면 빠른 안과 진료를 권장합니다. 통증, 눈을 못 뜸, 병변 돌출이 있으면 즉시 방문이 필요합니다.",
    },
    "결막염": {
        "증상": "결막에 염증이 생긴 상태로, 눈 충혈, 눈물 증가, 눈곱이나 분비물, 눈꺼풀 부종, 눈 찡그림이 나타날 수 있습니다. 고양이에서는 감염성 상부호흡기 질환과 함께 나타나는 경우도 있습니다.",
        "원인": "고양이 헤르페스바이러스, 클라미디아, 세균 감염, 알레르기, 환경 자극 등이 원인이 될 수 있습니다. 다묘 환경에서는 감염성 원인의 전파 가능성도 고려해야 합니다.",
        "치료": "원인에 따라 항바이러스 치료, 항생제 치료, 염증 조절, 동반 호흡기 증상 관리가 필요할 수 있습니다. 정확한 치료는 수의사의 진단에 따라 결정해야 합니다.",
        "관리": "눈 주변 분비물을 부드럽게 닦고, 스트레스를 줄이며, 다묘 가정에서는 감염 의심 개체의 접촉을 줄입니다. 임의로 사람용 안약을 사용하지 않습니다.",
        "중증도기준": "경증은 가벼운 충혈과 맑은 눈물입니다. 중등증은 분비물, 뚜렷한 발적, 눈꺼풀 부종이 있는 상태입니다. 중증은 각막 혼탁, 눈을 못 뜸, 심한 통증, 시력 이상이 의심되는 상태입니다.",
        "병원안내": "충혈이나 분비물이 지속되면 진료가 필요합니다. 각막 혼탁, 눈을 못 뜸, 심한 분비물, 식욕 저하나 호흡기 증상이 동반되면 빠른 방문을 권장합니다.",
    },
    "비궤양성각막염": {
        "증상": "각막에 궤양성 상처 없이 염증이나 혼탁이 나타나는 상태입니다. 각막 흐림, 혈관신생, 눈물 증가, 눈 찡그림, 시야 저하가 보일 수 있습니다.",
        "원인": "고양이 헤르페스바이러스 관련 각막염, 만성 염증, 면역 반응, 반복적인 각막 자극 등이 원인이 될 수 있습니다.",
        "치료": "원인에 따라 항바이러스 치료, 항감염 치료, 염증 조절, 윤활 치료가 고려될 수 있습니다. 궤양 여부를 확인한 뒤 치료 방향을 정해야 합니다.",
        "관리": "스트레스를 줄이고, 눈을 비비지 않도록 하며, 재발 여부를 관찰합니다. 장기 관리가 필요한 경우 정기검진이 중요합니다.",
        "중증도기준": "경증은 제한적인 혼탁과 가벼운 눈물입니다. 중등증은 혼탁과 혈관신생이 확장되는 상태입니다. 중증은 중심부 침범, 통증, 시력 저하, 동반 각막궤양 의심 소견이 있는 상태입니다.",
        "병원안내": "각막 혼탁이 지속되거나 재발하면 1~2주 내 진료를 권장합니다. 통증, 눈을 못 뜸, 급격한 혼탁 증가가 있으면 빠른 진료가 필요합니다.",
    },
    "안검염": {
        "증상": "눈꺼풀에 염증이 생긴 상태로, 눈꺼풀 발적, 부종, 딱지, 가려움, 눈 찡그림, 분비물이 나타날 수 있습니다. 고양이에서는 전신 피부질환이나 감염성 질환과 관련될 수 있습니다.",
        "원인": "감염, 알레르기, 기생충·진균 등 피부질환, 면역성 질환, 고양이 헤르페스바이러스 관련 안질환 등이 원인이 될 수 있습니다.",
        "치료": "원인에 따라 항감염 치료, 항염증 치료, 피부질환 관리, 동반 결막염 또는 각막질환 치료가 필요할 수 있습니다. 치료는 수의사의 진단에 따라 결정합니다.",
        "관리": "눈꺼풀 주변을 깨끗하게 유지하고, 긁거나 비비지 않도록 합니다. 딱지를 억지로 떼지 말고, 재발하거나 양쪽 눈에 반복되면 진료를 받습니다.",
        "중증도기준": "경증은 국소적인 발적과 가벼운 부종입니다. 중등증은 딱지, 분비물, 가려움, 눈 찡그림이 동반되는 상태입니다. 중증은 광범위한 피부 손상, 통증, 각막 자극 또는 결막염이 동반되는 상태입니다.",
        "병원안내": "눈꺼풀 발적이나 부종이 지속되면 진료가 필요합니다. 통증, 심한 분비물, 피부 상처, 각막 혼탁이 보이면 빠른 방문을 권장합니다.",
    },
    "정상": {
        "증상": "현재 안구에서 뚜렷한 이상 소견이 관찰되지 않는 상태입니다. 과도한 충혈, 눈곱, 눈물, 각막 혼탁, 눈 비빔, 눈꺼풀 부종 등의 증상이 두드러지지 않습니다.",
        "원인": "질병성 원인이 확인되지 않은 정상 안구 상태입니다. 다만 조명 반사, 일시적 눈물, 촬영 각도에 따라 약간의 변화가 보일 수 있으므로 지속적인 관찰이 필요합니다.",
        "치료": "특별한 치료는 필요하지 않습니다. 다만 갑작스러운 충혈, 눈곱 증가, 눈물 과다, 눈을 찡그림, 각막 혼탁이 새로 나타나면 동물병원 진료가 필요할 수 있습니다.",
        "관리": "눈 주변을 청결하게 유지하고, 눈곱·눈물·충혈·눈 비빔이 갑자기 증가하는지 관찰합니다. 스트레스를 줄이고 정기적인 건강검진을 권장합니다.",
        "중증도기준": "정상 범위는 뚜렷한 충혈, 혼탁, 분비물, 눈꺼풀 부종, 통증 반응이 없는 상태입니다. 증상이 새로 발생하거나 지속될 경우 정상 범위를 벗어난 것으로 판단할 수 있습니다.",
        "병원안내": "특이 증상이 없다면 정기검진 수준으로 관리합니다. 충혈, 눈곱, 눈물, 혼탁, 눈 비빔, 시야 이상 등이 관찰되면 1주일 내 동물병원 방문을 권장합니다.",
    },
}

# ─────────────────────────────────────────────
# ChromaDB 삽입
# ─────────────────────────────────────────────
def build_metadata(species: str, disease: str, section: str) -> dict:
    source = DISEASE_SOURCES.get(species, {}).get(disease, {})

    return {
        "disease": disease,
        "section": section,
        "species": species,
        "source_name": source.get("source_name", ""),
        "source_url": source.get("source_url", ""),
        "source_language": source.get("source_language", ""),
    }


def setup_chroma():
    client = chromadb.PersistentClient(path=CHROMA_DB_PATH)
    emb_fn = embedding_functions.SentenceTransformerEmbeddingFunction(
        model_name="snunlp/KR-ELECTRA-discriminator"
    )
    collection = client.get_or_create_collection(
        name=CHROMA_COLLECTION,
        embedding_function=emb_fn,
        metadata={"hnsw:space": "cosine"},
    )

    documents, metadatas, ids = [], [], []

    # 반려견 데이터 삽입
    for disease, items in DOG_DISEASE_DATA.items():
        for section, content in items.items():
            doc_id = f"dog_{disease}_{section}"
            documents.append(f"[반려견 {disease}] {section}: {content}")
            metadatas.append(build_metadata("dog", disease, section))
            ids.append(doc_id)

    # 반려묘 데이터 삽입
    for disease, items in CAT_DISEASE_DATA.items():
        for section, content in items.items():
            doc_id = f"cat_{disease}_{section}"
            documents.append(f"[반려묘 {disease}] {section}: {content}")
            metadatas.append(build_metadata("cat", disease, section))
            ids.append(doc_id)

    # 기존 데이터 초기화 후 재삽입
    existing = collection.count()
    if existing > 0:
        print(f"기존 {existing}개 청크 삭제 후 재삽입합니다.")
        collection.delete(where={"species": {"$in": ["dog", "cat"]}})

    collection.add(documents=documents, metadatas=metadatas, ids=ids)

    dog_cnt = len(DOG_DISEASE_DATA) * 6
    cat_cnt = len(CAT_DISEASE_DATA) * 6
    print("✅ ChromaDB 삽입 완료")
    print(f"   반려견: {len(DOG_DISEASE_DATA)}개 질환 × 6항목 = {dog_cnt}개 청크")
    print(f"   반려묘: {len(CAT_DISEASE_DATA)}개 질환 × 6항목 = {cat_cnt}개 청크")
    print(f"   합계: {dog_cnt + cat_cnt}개 청크")

    # 출처 누락 확인
    missing_sources = []
    for species, disease_data in [("dog", DOG_DISEASE_DATA), ("cat", CAT_DISEASE_DATA)]:
        for disease in disease_data.keys():
            if disease not in DISEASE_SOURCES.get(species, {}):
                missing_sources.append((species, disease))

    if missing_sources:
        print("⚠️ 출처 메타데이터 누락:")
        for species, disease in missing_sources:
            print(f"   - {species}: {disease}")
    else:
        print("✅ 모든 질환에 source metadata가 포함되어 있습니다.")


if __name__ == "__main__":
    setup_chroma()
