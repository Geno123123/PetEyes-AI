<div align="center">

# 🐾 PetEyes (펫아이즈)

**반려동물 안구질환 AI 진단 서비스**

스마트폰 카메라로 찍은 반려동물(강아지·고양이) 눈 사진을 AI가 분석해 안구질환을 진단하고,
진단 기록 관리부터 병원 연계까지 한 번에 제공하는 모바일 기반 서비스입니다.

세종대학교 SW중심대학사업단 · 2026-1학기 창의설계경진대회 (컴1-04 캡스톤디자인)

</div>

<br>

## 목차

- [소개](#소개)
- [화면 미리보기](#화면-미리보기)
- [주요 기능](#주요-기능)
- [시스템 아키텍처](#시스템-아키텍처)
- [AI 진단 파이프라인](#ai-진단-파이프라인)
- [설계 문서](#설계-문서)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [팀](#팀)
- [프로젝트 진행 과정](#프로젝트-진행-과정)

<br>

## 소개

반려동물을 키우는 가구는 매년 늘고 있지만, 안구질환은 초기 증상을 보호자가 스스로 알아차리기 어렵고
병원 방문 전까지 방치되기 쉽습니다. 실제로 국내 반려동물 연간 치료비는 2023년 78.7만원에서
2025년 146.3만원으로 크게 늘었습니다.

**PetEyes**는 이 문제를 해결하기 위해 다음 흐름을 하나로 묶었습니다.

> 안구 사진 촬영 → AI 진단(질환명·중증도) → 보호자용 문진서 생성 → 진단 추이 관리 → 주변 병원 연계

보호자, 수의사, 관리자 세 종류의 사용자를 지원하는 웹/앱 서비스로 설계되었습니다.

<br>

## 화면 미리보기

| 로그인 / 홈 | 카메라 / 로딩 | 진단 결과 / 병원 찾기 |
|:---:|:---:|:---:|
| ![로그인/홈](docs/images/screenshots/01-login-home.png) | ![카메라/로딩](docs/images/screenshots/02-camera-loading.png) | ![진단결과/병원찾기](docs/images/screenshots/04-diagnosis-hospital.png) |

| 병원 리뷰 | Q&A / 진단 추이 | 관리자 대시보드 |
|:---:|:---:|:---:|
| ![리뷰](docs/images/screenshots/05-review.png) | ![Q&A/진단추이](docs/images/screenshots/06-qna-trend.png) | ![관리자 대시보드](docs/images/screenshots/08-admin-dashboard.png) |

수의사용 Q&A 답변 화면은 [`docs/images/screenshots/07-vet-answer.png`](docs/images/screenshots/07-vet-answer.png) 에서 확인할 수 있습니다.
전체 발표 포스터는 [`docs/images/poster.png`](docs/images/poster.png) 를 참고하세요.

<br>

## 주요 기능

### 🐶 보호자
- **AI 안구 진단**: 스마트폰 카메라로 촬영/업로드한 눈 사진을 AI로 분석
- **맞춤형 진단 결과**: 질환명·정확도·중증도와 함께 자연어로 된 리포트(문진서) 제공
- **진단 추이 조회**: 반려동물별 중증도 변화를 그래프로 추적, 상태에 맞는 케어 가이드 제공
- **동물병원 검색 및 리뷰**: 위치 기반 병원 추천, AI 키워드 요약이 포함된 리뷰 열람/작성
- **Q&A 게시판**: 수의사에게 직접 질문하고 답변 받기

### 🩺 수의사
- **Q&A 답변**: 보호자 질문에 1:1 맞춤 답변 작성
- **수의사 인증**: 면허 제출 후 전문가 인증 절차

### 🛠️ 관리자
- 공지사항 관리, 리뷰/Q&A 모더레이션, 광고 관리, 사용자 및 학습 데이터 관리
- 핵심 지표(KPI) 대시보드 및 서비스 통계 모니터링

<br>

## 시스템 아키텍처

```
┌──────────────┐      API 요청      ┌─────────────────────────┐
│   App        │ ───────────────▶ │   API Server              │
│ (React       │                   │  Spring Boot + Redis +    │
│  Native)     │ ◀─────────────── │  Docker                   │
└──────────────┘      결과 응답     └───────┬──────────┬────────┘
                                            │          │
                                     API 요청 │          │ 이미지/데이터
                                            ▼          ▼
                                   ┌────────────┐  ┌─────────────────┐
                                   │  AI Server  │  │  DB              │
                                   │  FastAPI +  │  │  MySQL + S3      │
                                   │  PyTorch    │  └─────────────────┘
                                   └─────┬───────┘
                                         │
                              ┌──────────┴───────────┐
                              ▼                       ▼
                        Groq (Llama 3.1 8B)     ChromaDB (수의학 RAG)
                        → 문진서 생성            → 유사 사례/정보 검색
```

> 발표 포스터에는 DB로 PostgreSQL이 표기되어 있으나, 실제 백엔드 구현체는 **MySQL**을 사용합니다.

<br>

## AI 진단 파이프라인

```
안구 사진 업로드
   ↓  YOLOv8n (mAP@50 0.9769) — 눈 영역 자동 검출/크롭
   ↓  정상 / 비정상 판별
   ↓  ConvNeXt — 질환군 1차 분류
   ↓  EfficientNet-B3 / ConvNeXt — 세부 질환 분류
   ↓  RGB/HSV 색공간 분석 + 보호자 설문 응답 결합 — 중증도 산출
   ↓  ChromaDB(RAG, KR-ELECTRA 임베딩) 수의학 정보 검색
   ↓  Groq API (Llama 3.1 8B) — 보호자용 자연어 문진서 생성
   ↓
진단 결과 반환 (질환명 · 확률 · 중증도 · 증상/치료/원인/대처)
```

![AI 진단 파이프라인](docs/images/screenshots/03-ai-pipeline.png)

- 눈 영역이 자동 검출되지 않으면 사용자가 직접 영역을 지정하거나 재촬영할 수 있도록 폴백을 제공합니다.
- 설문조사(문진)는 선택 사항이며, 스킵하더라도 이미지 기반 분석만으로 결과를 제공합니다.
- RAG 기반 문진서 생성으로 LLM의 환각(hallucination)을 방지합니다.

<br>

## 설계 문서

<details>
<summary><b>유스케이스 다이어그램</b> (클릭해서 펼치기)</summary>
<br>

![유스케이스 다이어그램](docs/images/usecase-diagram.png)

</details>

<details>
<summary><b>도메인 모델 (클래스 다이어그램)</b></summary>
<br>

![도메인 모델 다이어그램](docs/images/domain-model-diagram.png)

</details>

<details>
<summary><b>진단(Diagnosis) 서비스 계층 클래스 다이어그램</b></summary>
<br>

![진단 서비스 클래스 다이어그램](docs/images/diagnosis-service-class-diagram.png)

</details>

<br>

## 기술 스택

| 영역 | 스택 |
|---|---|
| **Frontend** | React Native, Expo Camera, TypeScript, NativeWind, Zustand |
| **Backend** | Spring Boot 3.5 (Java 17), Spring Security + OAuth2(Naver/Kakao), Spring Data JPA, MySQL, JWT(jjwt), AWS S3 SDK, springdoc-openapi |
| **AI Server** | FastAPI, PyTorch, Ultralytics YOLOv8n, timm(EfficientNet-B3), ChromaDB, sentence-transformers, Groq(Llama 3.1 8B) |
| **Infra** | Docker, GitHub Actions(CI/CD), Nginx, AWS S3, DockerHub |

> 발표 자료에는 PostgreSQL / MongoDB / Redis / Terraform / Prometheus·Grafana 등도 기술 스택 후보로 표기되어 있으나,
> 이 저장소의 실제 구현 기준 스택은 위 표를 따릅니다.

<br>

## 프로젝트 구조

```
PetEyes-AI/
├── backend/               # Spring Boot API 서버
│   ├── src/main/java/com/capstone/backend/
│   │   ├── auth/          # 회원가입·로그인·OAuth(Naver/Kakao)·JWT
│   │   ├── user/          # 회원 정보, 수의사 인증
│   │   ├── pet/           # 반려동물 프로필
│   │   ├── diagnosis/     # 진단 기록·추이
│   │   ├── disease/       # 질환 정보
│   │   ├── hospital/      # 병원 검색(네이버 지도 연동)
│   │   ├── review/        # 병원 리뷰
│   │   ├── qna/           # Q&A 게시판
│   │   ├── image/         # S3 이미지 업로드
│   │   ├── ai/             # AI 서버 연동 클라이언트
│   │   └── admin/         # 관리자 기능
│   ├── docs/API.md        # API 명세
│   ├── scripts/           # DB 시드/이미지 업로드 스크립트
│   └── Dockerfile, docker-compose.yml
│
├── ai-server/              # FastAPI 기반 AI 추론 서버
│   ├── ai_server.py        # YOLOv8n·분류 모델·RAG·Groq 파이프라인
│   ├── setup_ragChroma.py  # ChromaDB(RAG) 초기 구축 스크립트
│   └── requirements.txt
│
├── frontend/                # React Native(Expo) 앱 — 추가 예정
│
├── docs/images/             # 다이어그램 및 스크린샷
└── .github/workflows/       # CI/CD (backend Docker 빌드·배포)
```

> `frontend/` 는 아직 이 저장소에 포함되지 않았습니다. 곧 추가될 예정입니다.

<br>

## 시작하기

### 1. 백엔드 (Spring Boot)

```bash
cd backend
cp .env.example .env   # 값 채워 넣기 (DB, JWT, OAuth, S3 등)
./gradlew bootRun
```

- 로컬 실행 시 MySQL(`dev_db`)이 떠 있어야 합니다. 필요한 환경 변수는 [`backend/.env.example`](backend/.env.example) 참고.
- 서버 실행 후 API 문서는 `http://localhost:8080/swagger` 에서 확인할 수 있습니다.
- 전체 엔드포인트 목록은 [`backend/docs/API.md`](backend/docs/API.md) 참고.

### 2. AI 서버 (FastAPI)

```bash
cd ai-server
pip install -r requirements.txt
cp .env.example .env   # GROQ_API_KEY 채워 넣기
python setup_ragChroma.py   # ChromaDB(RAG) 최초 1회 구축
uvicorn ai_server:app --host 0.0.0.0 --port 8000
```

- 모델 가중치(YOLOv8n, EfficientNet-B3 등)는 `ai-server/models/` 에 별도로 배치해야 합니다(저장소에는 포함되지 않음).
- 자세한 내용은 [`ai-server/README.md`](ai-server/README.md) 참고.

### 3. Docker로 백엔드 + DB 함께 띄우기

```bash
cd backend
docker compose up -d
```

<br>

## 팀

**4조 아무거나 (AMU-GUNA)** · 세종대학교 컴퓨터공학과 캡스톤디자인

| 이름 | 역할 |
|---|---|
| 조형배 | AI 개발 |
| 이서정 | Frontend 개발 |
| 박유진 | Backend 개발 |
| 이승준 | Backend 개발 |

> 이 저장소의 GitHub 협업자는 연락이 닿은 AI/Frontend 담당 2인입니다. Backend 코드는 팀원들이 작성한 원본을 그대로 포함하고 있으며,
> 프로젝트 전체 내용을 온전히 남기기 위해 함께 정리해 올렸습니다.

<br>

## 프로젝트 진행 과정

| 단계 | 내용 |
|---|---|
| 01. Discovery | 요구사항 분석(SRS), 유스케이스·UML 설계, IA·와이어프레임 |
| 02. 프로토타입 및 검증 | Figma 목업 제작, FastAPI 기본 구조, YOLOv8n PoC 검증 |
| 03. MVP 개발 | AI 파이프라인 구현, 진단 결과·추이 UI, 병원 찾기·Q&A 연동 |
| 04. 완성 및 고도화 | 통합 테스트·최적화, 최종 발표·데모, 캡스톤 최종 제출 |

개발 방법론으로는 요구사항 변경과 AI 성능의 불확실성에 대응하기 위해
**프로토타입(Figma 클릭형 UI) + 애자일(Scrum, 스프린트 기반 반복 개발)** 을 혼합해 채택했습니다.

<br>

---

<div align="center">

근거 있는 UX/UI 설계로 사용자의 진짜 문제를 해결하다 — PetEyes

</div>
