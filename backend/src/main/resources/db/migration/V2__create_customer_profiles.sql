CREATE TABLE IF NOT EXISTS customer_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  date_of_birth DATE NULL,
  gender VARCHAR(30) NULL,
  address VARCHAR(255) NULL,
  city VARCHAR(100) NULL,
  state VARCHAR(100) NULL,
  postal_code VARCHAR(20) NULL,
  country VARCHAR(100) NULL,
  occupation VARCHAR(100) NULL,
  annual_income DECIMAL(15, 2) NULL,
  id_proof_type VARCHAR(50) NULL,
  id_proof_number VARCHAR(100) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uq_customer_profiles_user UNIQUE (user_id),
  CONSTRAINT fk_customer_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_profiles_user ON customer_profiles (user_id);
