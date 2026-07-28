SET NAMES utf8mb4;

-- 0) 중복 기준을 (질병명+종)으로 강제
SET @drop_old_idx := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'diseases'
        AND index_name = 'uk_diseases_name'
    ),
    'ALTER TABLE diseases DROP INDEX uk_diseases_name',
    'SELECT 1'
  )
);
PREPARE stmt_drop_old_idx FROM @drop_old_idx;
EXECUTE stmt_drop_old_idx;
DEALLOCATE PREPARE stmt_drop_old_idx;

SET @add_new_idx := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'diseases'
        AND index_name = 'uk_diseases_name_species'
    ),
    'SELECT 1',
    'ALTER TABLE diseases ADD UNIQUE KEY uk_diseases_name_species (disease_name, species)'
  )
);
PREPARE stmt_add_new_idx FROM @add_new_idx;
EXECUTE stmt_add_new_idx;
DEALLOCATE PREPARE stmt_add_new_idx;

-- 1) 기존 10개는 DOG로 고정
UPDATE diseases
SET species = 'DOG'
WHERE disease_id BETWEEN 1 AND 10;

-- 2) 고양이 질병만 추가/갱신
INSERT INTO diseases
(disease_name, species, category, disease_image_url, mild_example_image_url, caution_example_image_url, danger_example_image_url, symptoms, description, care_guide)
VALUES
('결막염','CAT','결막질환',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/결막염/main.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/결막염/mild.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/결막염/caution.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/결막염/danger.jpg',
'결막 충혈 및 부종, 눈곱, 눈 주위 붉어짐, 눈물 흘림, 눈 비빔', '', ''),
('안검염','CAT','눈꺼풀질환',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/안검염/danger.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/안검염/mild.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/안검염/caution.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/안검염/danger.jpg',
'눈가 충혈, 부어오름, 눈곱 증가, 눈 주위 가려움/비빔', '', ''),
('각막궤양','CAT','각막질환',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막궤양/main.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막궤양/mild.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막궤양/caution.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막궤양/danger.jpg',
'눈 통증, 눈을 잘 못 뜸, 과다 눈물, 각막 혼탁/충혈', '', ''),
('각막부골편','CAT','각막질환',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막부골편/main.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막부골편/mild.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막부골편/caution.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/각막부골편/danger.jpg',
'각막의 검은/갈색 반점, 눈부심, 눈물 증가, 만성 통증', '', ''),
('비궤양성각막염','CAT','각막질환',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/비궤양성각막염/danger.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/비궤양성각막염/mild.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/비궤양성각막염/caution.jpg',
'https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/비궤양성각막염/danger.jpg',
'각막 혼탁, 혈관 신생, 충혈, 시야 저하', '', '')
ON DUPLICATE KEY UPDATE
category = VALUES(category),
disease_image_url = VALUES(disease_image_url),
mild_example_image_url = VALUES(mild_example_image_url),
caution_example_image_url = VALUES(caution_example_image_url),
danger_example_image_url = VALUES(danger_example_image_url),
symptoms = VALUES(symptoms);
