-- Medicatch Platform DB 초기화
CREATE DATABASE IF NOT EXISTS medicatch_health
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS medicatch_insurance
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON medicatch_health.* TO 'medicatch'@'%';
GRANT ALL PRIVILEGES ON medicatch_insurance.* TO 'medicatch'@'%';
FLUSH PRIVILEGES;
