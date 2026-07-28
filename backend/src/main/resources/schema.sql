DROP TABLE IF EXISTS diagnoses;
DROP TABLE IF EXISTS diseases;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS hospitals;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id VARCHAR(320) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    password VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    vet_verification_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    vet_license_number VARCHAR(100),
    vet_proof_image_url VARCHAR(500),
    vet_verification_requested_at DATETIME,
    vet_verification_reviewed_at DATETIME,
    vet_verification_review_note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_provider_id UNIQUE (provider_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VET')),
    CONSTRAINT chk_users_vet_verification_status CHECK (vet_verification_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE pets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    breed VARCHAR(100) NOT NULL,
    CONSTRAINT pk_pets PRIMARY KEY (id),
    CONSTRAINT fk_pets_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE hospitals (
    hospital_id BIGINT NOT NULL AUTO_INCREMENT,
    address VARCHAR(255) NOT NULL,
    diagnosis_subject VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    operating_hours VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    CONSTRAINT pk_hospitals PRIMARY KEY (hospital_id)
);

CREATE TABLE reviews (
    review_id BIGINT NOT NULL AUTO_INCREMENT,
    hospital_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    review VARCHAR(1000) NOT NULL,
    rating DOUBLE NOT NULL,
    cost_rating DOUBLE NOT NULL,
    expertise_rating DOUBLE NOT NULL,
    service_rating DOUBLE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals (hospital_id),
    CONSTRAINT fk_reviews_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE diseases (
    disease_id BIGINT NOT NULL AUTO_INCREMENT,
    disease_name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    disease_image_url VARCHAR(500) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    symptoms VARCHAR(500) NOT NULL,
    care_guide VARCHAR(1000) NOT NULL,
    CONSTRAINT pk_diseases PRIMARY KEY (disease_id),
    CONSTRAINT uk_diseases_name_species UNIQUE (disease_name, species)
);

CREATE TABLE diagnoses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    disease_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    disease_name VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    confidence DOUBLE NOT NULL,
    rgb_hsv_score DOUBLE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_diagnoses PRIMARY KEY (id),
    CONSTRAINT fk_diagnoses_pet_id FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_diagnoses_disease_id FOREIGN KEY (disease_id) REFERENCES diseases (disease_id)
);

CREATE INDEX idx_pets_user_id ON pets (user_id);
CREATE INDEX idx_hospitals_lat_lng ON hospitals (latitude, longitude);
CREATE INDEX idx_reviews_hospital_id ON reviews (hospital_id);
CREATE INDEX idx_reviews_hospital_rating ON reviews (hospital_id, rating);
CREATE INDEX idx_diagnoses_pet_id ON diagnoses (pet_id);
CREATE INDEX idx_diagnoses_disease_id ON diagnoses (disease_id);
CREATE INDEX idx_diagnoses_disease_name ON diagnoses (disease_name);
