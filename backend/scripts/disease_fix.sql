SET NAMES utf8mb4;

-- DOG인데 CAT URL이 들어간 케이스 교정
UPDATE diseases
SET disease_image_url = 'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/안검염/main.png'
WHERE disease_id = 1;

UPDATE diseases
SET disease_image_url = 'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/결막염/danger.jpg'
WHERE disease_id = 10;

-- DOG URL 경로 누락 케이스 교정
UPDATE diseases
SET disease_image_url = 'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/궤양성각막질환/main.png'
WHERE disease_id = 7;

UPDATE diseases
SET disease_image_url = 'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/비궤양성각막질환/main.png'
WHERE disease_id = 8;

UPDATE diseases
SET disease_image_url = 'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/백내장/main.png'
WHERE disease_id = 9;

-- CAT 증상 누락 케이스 보정
UPDATE diseases
SET symptoms = '눈 통증, 눈을 잘 못 뜸, 과다 눈물, 각막 혼탁/충혈'
WHERE disease_id = 109;

UPDATE diseases
SET symptoms = '각막의 검은/갈색 반점, 눈부심, 눈물 증가, 만성 통증'
WHERE disease_id = 110;

UPDATE diseases
SET symptoms = '각막 혼탁, 혈관 신생, 충혈, 시야 저하'
WHERE disease_id = 111;
