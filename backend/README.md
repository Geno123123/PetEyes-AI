# PetEyes Backend (API Server)

PetEyes의 API 서버입니다. 회원/인증, 반려동물 프로필, 진단 기록, 병원 검색, 리뷰, Q&A, 관리자 기능을 제공하며
AI 서버([`../ai-server`](../ai-server))와 통신해 진단 요청을 중계합니다.

## 기술 스택

- Java 17, Spring Boot 3.5
- Spring Security + OAuth2 Client (네이버/카카오 로그인), JWT(jjwt)
- Spring Data JPA + MySQL
- AWS S3 SDK (이미지 업로드)
- springdoc-openapi (Swagger UI)

## 로컬 실행

1. MySQL을 준비합니다. (`dev_db` 데이터베이스, `application-local.yml` 참고)
2. 환경 변수를 설정합니다.

   ```bash
   cp .env.example .env
   # .env 파일을 열어 DB_PASSWORD, JWT_SECRET, NAVER_*, KAKAO_*, AWS_* 값을 채워 넣으세요.
   ```

3. 서버를 실행합니다.

   ```bash
   ./gradlew bootRun
   ```

   기본적으로 `spring.profiles.active=local` 프로파일로 실행됩니다 (`application.yml`).

4. API 문서: `http://localhost:8080/swagger`
5. 전체 엔드포인트 설명: [`docs/API.md`](docs/API.md)

## 환경 변수

자세한 목록은 [`.env.example`](.env.example) 참고. 로컬(`local`)과 배포(`prod`) 프로파일에서
AWS 자격증명 변수명이 다르게 쓰입니다(각각 `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`,
`AWS_S3_ACCESS_KEY`/`AWS_S3_SECRET_KEY`). 배포 환경에서는 GitHub Actions Secrets로 관리됩니다
(`.github/workflows/deploy.yml` 참고).

## Docker

```bash
docker compose up -d
```

`docker-compose.yml` 은 MySQL 컨테이너와 백엔드 앱 컨테이너를 함께 띄웁니다.
운영 배포는 GitHub Actions → DockerHub → SSH 배포 파이프라인으로 자동화되어 있습니다.

## 패키지 구조

도메인 단위(auth, user, pet, diagnosis, disease, hospital, review, qna, image, ai, admin)로
controller / dto / service 를 나눠 구성했습니다. 자세한 트리는 [루트 README](../README.md#프로젝트-구조) 참고.

## scripts/

- `disease_*.sql`: 질환 데이터/이미지 URL 시드 및 보정 스크립트
- `convert_disease_images_to_aes256.sh`, `upload_disease_images_and_generate_sql.sh`: S3 이미지 업로드/암호화 유틸리티

SQL/스크립트 안의 S3 버킷명은 `YOUR_S3_BUCKET_NAME` 플레이스홀더로 치환되어 있습니다. 실행 전 실제 버킷명으로 바꾸거나
`AWS_S3_BUCKET` 환경 변수를 채워 넣으세요. 질환 이미지 기본 URL(`DataInitializer`)도 하드코딩 대신
`app.s3.public-base-url` 설정값을 사용하도록 되어 있습니다.
