# PetEyes Backend API 명세

> Swagger 명세 기준 API 설명입니다. 서버 실행 후 `/swagger` 에서 실제 인터랙티브 문서를 확인할 수 있습니다.

[Auth]
1) 회원가입 API는 POST /api/auth/signup 엔드포인트를 통해 email, password, name, nickname, phoneNumber, role 값을 받아 회원가입을 처리한다. email은 중복을 허용하지 않으며, 이미 등록된 email로는 회원가입할 수 없다. password는 평문으로 저장하지 않고 암호화하여 저장하며, 로그인 시에는 입력한 password를 동일한 방식으로 암호화한 뒤 저장된 암호화 값과 비교하여 인증한다.

2) 로그인 API는 POST /api/auth/login 엔드포인트를 통해 email, password를 받아 사용자 인증을 수행하고 access token을 발급한다.

3) 토큰 교환 API는 POST /api/auth/token 엔드포인트를 통해 code 값을 받아 인증 코드를 access token으로 교환한다.

[Users] (인증 필요)
4) 내 정보 조회 API는 GET /api/users/me 엔드포인트를 통해 현재 access token 기준 사용자 정보를 조회한다.

5) 내 정보 수정 API는 PATCH /api/users/me 엔드포인트를 통해 name, nickname, profileImageUrl, phoneNumber 값을 받아 사용자 정보를 수정한다.

6) 비밀번호 변경 API는 PATCH /api/users/me/password 엔드포인트를 통해 currentPassword, newPassword 값을 받아 비밀번호를 변경한다.

7) 회원 탈퇴 API는 DELETE /api/users/me 엔드포인트를 통해 현재 로그인한 사용자의 계정을 삭제한다.

8) 수의사 인증 신청 API는 POST /api/users/me/vet-verification 엔드포인트를 통해 licenseNumber, proofImageUrl 값을 받아 수의사 인증 신청을 생성 또는 갱신하고 상태를 PENDING으로 변경한다.

9) 수의사 인증 단일 신청 API는 POST /api/users/me/vet-verification/with-image 엔드포인트를 통해 multipart/form-data의 licenseNumber, file 값을 받아 이미지 업로드와 인증 신청을 한 번에 처리한다.

10) 수의사 인증 이미지 업로드 API는 POST /api/users/me/vet-verification/image 엔드포인트를 통해 multipart/form-data의 file 값을 받아 인증 증빙 이미지를 업로드하고 URL을 반환한다.

[Pets] (인증 필요)
11) 반려동물 목록 조회 API는 GET /api/pets 엔드포인트를 통해 로그인 사용자의 반려동물 목록을 조회한다.

12) 반려동물 상세 조회 API는 GET /api/pets/{petId} 엔드포인트를 통해 petId에 해당하는 반려동물 상세 정보를 조회한다.

13) 반려동물 등록 API는 POST /api/pets 엔드포인트를 통해 name, breed, species, birthDate 값을 받아 반려동물을 등록한다.

14) 반려동물 수정 API는 PUT /api/pets/{petId} 엔드포인트를 통해 name, breed, species, birthDate 값을 받아 해당 반려동물 정보를 수정한다.

15) 반려동물 삭제 API는 DELETE /api/pets/{petId} 엔드포인트를 통해 해당 반려동물을 삭제한다.

[Diagnoses] (인증 필요)
16) 진단 결과 저장 API는 POST /api/pets/{petId}/diagnoses 엔드포인트를 통해 imageUrl, eyePosition, diseaseId, rawSeverity, confidence 값을 받아 진단 기록을 저장한다.

17) 진단 목록 조회 API는 GET /api/pets/{petId}/diagnoses 엔드포인트를 통해 해당 반려동물의 진단 기록 목록을 조회한다.

18) 진단 상세 조회 API는 GET /api/pets/{petId}/diagnoses/{diagnosisId} 엔드포인트를 통해 특정 진단 상세 정보를 조회한다.

19) 진단 삭제 API는 DELETE /api/pets/{petId}/diagnoses/{diagnosisId} 엔드포인트를 통해 특정 진단 기록을 삭제한다.

20) 진단 추이 조회 API는 GET /api/pets/{petId}/diagnoses/trend 엔드포인트를 통해 반려동물의 진단 추이 데이터를 조회한다.

[Diseases]
21) 질환 목록 조회 API는 GET /api/diseases 엔드포인트를 통해 등록된 안구 질환 목록을 조회한다.

22) 질환 상세 조회 API는 GET /api/diseases/{diseaseId} 엔드포인트를 통해 diseaseId에 해당하는 안구 질환 상세 정보를 조회한다.

[Hospitals] (인증 필요)
23) 주변 병원 조회 API는 GET /api/hospitals/nearby 엔드포인트를 통해 latitude, longitude를 필수로 받고 radiusKm, limit, night, sortByReviewCount를 선택으로 받아 주변 병원 목록을 조회한다.

[Reviews] (인증 필요)
24) 리뷰 작성 API는 POST /api/hospitals/{hospitalId}/reviews 엔드포인트를 통해 review, costRating, expertiseRating, serviceRating 값을 받아 병원 리뷰를 작성한다.

25) 병원 리뷰 목록 조회 API는 GET /api/hospitals/{hospitalId}/reviews 엔드포인트를 통해 해당 병원의 리뷰 목록을 조회한다.

26) 리뷰 수정 API는 PUT /api/hospitals/reviews/{reviewId} 엔드포인트를 통해 review, costRating, expertiseRating, serviceRating 값을 받아 본인이 작성한 리뷰를 수정한다.

27) 병원 키워드 조회 API는 GET /api/hospitals/{hospitalId}/keywords 엔드포인트를 통해 AI가 추출해 저장한 병원 키워드를 조회한다.

28) 리뷰 삭제 API는 DELETE /api/hospitals/reviews/{reviewId} 엔드포인트를 통해 본인이 작성한 리뷰를 삭제한다.

[Q&A] (인증 필요)
29) 게시글 목록 조회 API는 GET /api/qna 엔드포인트를 통해 species, answered 쿼리 파라미터(선택)를 받아 Q&A 게시글 목록을 조회한다.

30) 게시글 상세 조회 API는 GET /api/qna/{postId} 엔드포인트를 통해 postId에 해당하는 게시글 상세 정보를 조회한다.

31) 내 답변 목록 조회 API는 GET /api/qna/my-answers 엔드포인트를 통해 로그인 사용자가 작성한 답변 목록을 조회한다.

32) 게시글 작성 API는 POST /api/qna 엔드포인트를 통해 title, content, species 값을 받아 게시글을 작성한다.

33) 게시글 수정 API는 PUT /api/qna/{postId} 엔드포인트를 통해 title, content, species 값을 받아 본인 게시글을 수정한다.

34) 게시글 삭제 API는 DELETE /api/qna/{postId} 엔드포인트를 통해 본인 게시글을 삭제한다.

35) 답변 작성 API는 POST /api/qna/{postId}/answers 엔드포인트를 통해 content 값을 받아 답변을 작성한다.

36) 답변 삭제 API는 DELETE /api/qna/{postId}/answers/{answerId} 엔드포인트를 통해 본인 답변을 삭제한다.

[AI] (인증 필요)
37) 안구 사진 진단 및 문진서 생성 API는 POST /api/ai/pets/{petId}/diagnose 엔드포인트를 통해 multipart/form-data의 image, eye_position, pet_type, q1~q10 값을 받아 AI 진단과 문진서 생성을 수행한다.

38) 진단 기반 문진서 재생성 API는 GET /api/ai/pets/{petId}/diagnoses/{diagnosisId}/report 엔드포인트를 통해 기존 진단 결과 기반 문진서를 재생성한다.

39) 병원 리뷰 키워드 추출 API는 GET /api/ai/hospitals/{hospitalId}/keywords 엔드포인트를 통해 병원 리뷰를 분석한 키워드를 반환한다.

[Images] (인증 필요)
40) 이미지 업로드 API는 POST /api/images 엔드포인트를 통해 multipart/form-data의 file 값을 받아 이미지를 업로드하고 URL 정보를 반환한다.

[Admin] (인증 필요, 관리자 권한)
41) 관리자 통계 조회 API는 GET /api/admin/stats 엔드포인트를 통해 전체 서비스 통계를 조회한다.

42) 관리자 대시보드 통계 조회 API는 GET /api/admin/stats/dashboard 엔드포인트를 통해 대시보드용 통계를 조회한다.

43) 관리자 사용자 목록 조회 API는 GET /api/admin/users 엔드포인트를 통해 사용자 목록을 조회한다.

44) 관리자 사용자 권한 변경 API는 PATCH /api/admin/users/{id}/role 엔드포인트를 통해 role 값을 받아 사용자 권한을 변경한다.

45) 관리자 수의사 인증 상태 변경 API는 PATCH /api/admin/users/{id}/vet-verification 엔드포인트를 통해 status, reviewNote 값을 받아 수의사 인증 상태를 변경한다.

46) 관리자 사용자 삭제 API는 DELETE /api/admin/users/{id} 엔드포인트를 통해 사용자를 삭제한다.

47) 관리자 병원 목록 조회 API는 GET /api/admin/hospitals 엔드포인트를 통해 병원 목록을 조회한다.

48) 관리자 병원 등록 API는 POST /api/admin/hospitals 엔드포인트를 통해 name, address, phoneNumber, latitude, longitude, parkingAvailable, nightCare 값을 받아 병원을 등록한다.

49) 관리자 병원 수정 API는 PUT /api/admin/hospitals/{id} 엔드포인트를 통해 name, address, phoneNumber, latitude, longitude, parkingAvailable, nightCare 값을 받아 병원 정보를 수정한다.

50) 관리자 병원 삭제 API는 DELETE /api/admin/hospitals/{id} 엔드포인트를 통해 병원을 삭제한다.

51) 관리자 리뷰 목록 조회 API는 GET /api/admin/reviews 엔드포인트를 통해 전체 리뷰 목록을 조회한다.

52) 관리자 리뷰 삭제 API는 DELETE /api/admin/reviews/{id} 엔드포인트를 통해 리뷰를 삭제한다.

53) 관리자 Q&A 목록 조회 API는 GET /api/admin/qna 엔드포인트를 통해 Q&A 게시글 목록을 조회한다.

54) 관리자 Q&A 삭제 API는 DELETE /api/admin/qna/{id} 엔드포인트를 통해 게시글을 삭제한다.

[기타]
55) 홈 화면 확인 API는 GET /api/home 엔드포인트를 통해 로그인 성공 확인용 HTML 페이지를 반환한다.
