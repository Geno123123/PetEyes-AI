SET NAMES utf8mb4;

-- DOG: 상/중/하 -> danger/caution/mild
UPDATE diseases
SET
  mild_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/', disease_name, '/mild.jpg'),
  caution_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/', disease_name, '/caution.jpg'),
  danger_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/DOG/', disease_name, '/danger.jpg')
WHERE species = 'DOG';

-- CAT: 상/중/하 -> danger/caution/mild
UPDATE diseases
SET
  mild_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/', disease_name, '/mild.jpg'),
  caution_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/', disease_name, '/caution.jpg'),
  danger_example_image_url = CONCAT('https://YOUR_S3_BUCKET_NAME.s3.ap-northeast-2.amazonaws.com/uploads/diseases/CAT/', disease_name, '/danger.jpg')
WHERE species = 'CAT';
