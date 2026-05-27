CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  email VARCHAR(180) NOT NULL,
  password VARCHAR(255),
  phone VARCHAR(40) NOT NULL,
  city VARCHAR(100) NOT NULL,
  province VARCHAR(100) NOT NULL,
  avatar_url VARCHAR(500),
  role VARCHAR(30) NOT NULL,
  enabled BIT NOT NULL DEFAULT 1,
  blocked BIT NOT NULL DEFAULT 0,
  verified BIT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  icon VARCHAR(60),
  active BIT NOT NULL DEFAULT 1,
  display_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(160) NOT NULL,
  slug VARCHAR(190) NOT NULL,
  description TEXT NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'ARS',
  `condition` VARCHAR(20) NOT NULL,
  brand VARCHAR(80),
  size VARCHAR(60),
  wheel_size VARCHAR(60),
  city VARCHAR(100) NOT NULL,
  province VARCHAR(100) NOT NULL,
  status VARCHAR(30) NOT NULL,
  featured BIT NOT NULL DEFAULT 0,
  featured_until DATETIME(6),
  views_count BIGINT NOT NULL DEFAULT 0,
  whatsapp_clicks_count BIGINT NOT NULL DEFAULT 0,
  report_count INT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6),
  seller_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_products_slug (slug),
  KEY idx_product_slug (slug),
  KEY idx_product_status (status),
  KEY idx_product_category (category_id),
  KEY idx_product_created_at (created_at),
  KEY idx_product_featured (featured),
  CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_images (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  order_index INT NOT NULL DEFAULT 0,
  main_image BIT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_product_images_product (product_id),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_favorites (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_favorites_user_product (user_id, product_id),
  KEY idx_product_favorites_user (user_id),
  KEY idx_product_favorites_product (product_id),
  CONSTRAINT fk_product_favorites_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_product_favorites_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_view_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  viewer_user_id BIGINT,
  ip_address VARCHAR(80),
  user_agent VARCHAR(500),
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_product_view_events_product (product_id),
  KEY idx_product_view_events_user (viewer_user_id),
  CONSTRAINT fk_product_view_events_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_product_view_events_user FOREIGN KEY (viewer_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS whatsapp_click_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT,
  ip_address VARCHAR(80),
  user_agent VARCHAR(500),
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_whatsapp_click_events_product (product_id),
  KEY idx_whatsapp_click_events_user (user_id),
  CONSTRAINT fk_whatsapp_click_events_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_whatsapp_click_events_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(20) NOT NULL,
  product_id BIGINT,
  reported_user_id BIGINT,
  reporter_id BIGINT,
  reason VARCHAR(120) NOT NULL,
  detail TEXT,
  status VARCHAR(20) NOT NULL,
  admin_comment TEXT,
  created_at DATETIME(6) NOT NULL,
  reviewed_at DATETIME(6),
  reviewed_by_id BIGINT,
  PRIMARY KEY (id),
  KEY idx_report_status (status),
  KEY idx_reports_product (product_id),
  KEY idx_reports_reported_user (reported_user_id),
  KEY idx_reports_reporter (reporter_id),
  KEY idx_reports_reviewed_by (reviewed_by_id),
  CONSTRAINT fk_reports_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES users (id),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
  CONSTRAINT fk_reports_reviewed_by FOREIGN KEY (reviewed_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS site_visit_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  path VARCHAR(500) NOT NULL,
  ip_address VARCHAR(80),
  user_agent VARCHAR(500),
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS store_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  store_name VARCHAR(140) NOT NULL,
  slug VARCHAR(160) NOT NULL,
  logo_url VARCHAR(255),
  banner_url VARCHAR(255),
  description TEXT,
  phone VARCHAR(40),
  city VARCHAR(100),
  province VARCHAR(100),
  premium BIT NOT NULL DEFAULT 0,
  premium_until DATETIME(6),
  active BIT NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_store_profiles_owner (owner_id),
  UNIQUE KEY uk_store_profiles_slug (slug),
  CONSTRAINT fk_store_profiles_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS featured_payments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  paid_at DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_featured_payments_product (product_id),
  KEY idx_featured_payments_user (user_id),
  CONSTRAINT fk_featured_payments_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_featured_payments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
