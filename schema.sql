-- Zellige Stays - Production Ready MySQL 8.0 Schema

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ====================================================
-- USERS
-- ====================================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('client', 'host', 'guide', 'admin') NOT NULL DEFAULT 'client',
    profile_image VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- SETTINGS
-- ====================================================
CREATE TABLE IF NOT EXISTS settings (
    app_name VARCHAR(100) NOT NULL,
    app_logo VARCHAR(255),
    app_slogan VARCHAR(255),
    support_email VARCHAR(100),
    support_phone VARCHAR(50),
    primary_color VARCHAR(20),
    secondary_color VARCHAR(20),
    monthly_subscription_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    free_trial_duration_months INT NOT NULL DEFAULT 1,
    maintenance_mode TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- CITIES
-- ====================================================
CREATE TABLE IF NOT EXISTS cities (
    id VARCHAR(50) PRIMARY KEY,
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_es VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    is_featured TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cities_featured (is_featured),
    INDEX idx_cities_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- CATEGORIES
-- ====================================================
CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(50) PRIMARY KEY,
    name_ar VARCHAR(100) NOT NULL,
    name_fr VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_es VARCHAR(100) NOT NULL,
    icon VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- HOST SUBSCRIPTIONS
-- ====================================================
CREATE TABLE IF NOT EXISTS subscriptions (
    id VARCHAR(50) PRIMARY KEY,
    host_id VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('active', 'expired', 'cancelled') NOT NULL DEFAULT 'active',
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sub_host (host_id),
    INDEX idx_sub_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- PROPERTIES
-- ====================================================
CREATE TABLE IF NOT EXISTS properties (
    id VARCHAR(50) PRIMARY KEY,
    host_id VARCHAR(50) NOT NULL,
    city_id VARCHAR(50) NOT NULL,
    category_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    price_per_night DECIMAL(10,2) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    is_featured TINYINT(1) DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_prop_host (host_id),
    INDEX idx_prop_city (city_id),
    INDEX idx_prop_status (status),
    INDEX idx_prop_featured (is_featured)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- PROPERTY IMAGES
-- ====================================================
CREATE TABLE IF NOT EXISTS property_images (
    id VARCHAR(50) PRIMARY KEY,
    property_id VARCHAR(50) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_prop_img (property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- RESERVATIONS
-- ====================================================
CREATE TABLE IF NOT EXISTS reservations (
    id VARCHAR(50) PRIMARY KEY,
    property_id VARCHAR(50) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2) NOT NULL,
    payment_status ENUM('pending', 'paid', 'failed') DEFAULT 'pending',
    reservation_status ENUM('pending_payment', 'confirmed', 'completed', 'cancelled') DEFAULT 'pending_payment',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE RESTRICT,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_res_prop (property_id),
    INDEX idx_res_client (client_id),
    INDEX idx_res_status (reservation_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- PAYMENTS
-- ====================================================
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    reservation_id VARCHAR(50) NULL,
    subscription_id VARCHAR(50) NULL,
    guide_booking_id VARCHAR(50) NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'MAD',
    payment_method ENUM('paypal', 'credit_card', 'debit_card') NOT NULL,
    payment_status ENUM('pending', 'paid', 'failed', 'refunded') DEFAULT 'pending',
    transaction_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL,
    INDEX idx_pay_user (user_id),
    INDEX idx_pay_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- PROPERTY REVIEWS
-- ====================================================
CREATE TABLE IF NOT EXISTS property_reviews (
    id VARCHAR(50) PRIMARY KEY,
    property_id VARCHAR(50) NOT NULL,
    reservation_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    rating INT NOT NULL CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_unique_res_review (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- FAVORITES
-- ====================================================
CREATE TABLE IF NOT EXISTS favorites (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_fav_user_prop (user_id, property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- MESSAGING
-- ====================================================
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(50) PRIMARY KEY,
    property_id VARCHAR(50) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    host_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conv_client (client_id),
    INDEX idx_conv_host (host_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(50) PRIMARY KEY,
    conversation_id VARCHAR(50) NOT NULL,
    sender_id VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_msg_conv (conversation_id),
    INDEX idx_msg_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- GUIDES
-- ====================================================
CREATE TABLE IF NOT EXISTS guides (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    city_id VARCHAR(50) NOT NULL,
    photo VARCHAR(255),
    description TEXT,
    languages VARCHAR(255),
    price_per_day DECIMAL(10,2) NOT NULL,
    experience_years INT DEFAULT 0,
    specialties VARCHAR(255),
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    status ENUM('pending', 'approved', 'rejected', 'suspended') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE RESTRICT,
    INDEX idx_guide_city (city_id),
    INDEX idx_guide_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- GUIDE BOOKINGS
-- ====================================================
CREATE TABLE IF NOT EXISTS guide_bookings (
    id VARCHAR(50) PRIMARY KEY,
    guide_id VARCHAR(50) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    booking_date DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    payment_status ENUM('pending', 'paid', 'failed') DEFAULT 'pending',
    booking_status ENUM('pending', 'confirmed', 'completed', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guide_id) REFERENCES guides(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_gb_guide (guide_id),
    INDEX idx_gb_client (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- GUIDE REVIEWS
-- ====================================================
CREATE TABLE IF NOT EXISTS guide_reviews (
    id VARCHAR(50) PRIMARY KEY,
    guide_id VARCHAR(50) NOT NULL,
    guide_booking_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    rating INT NOT NULL CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guide_id) REFERENCES guides(id) ON DELETE CASCADE,
    FOREIGN KEY (guide_booking_id) REFERENCES guide_bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_unique_gb_review (guide_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- NOTIFICATIONS
-- ====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- ADMIN LOGS
-- ====================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id VARCHAR(50) PRIMARY KEY,
    admin_id VARCHAR(50) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_log_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================
-- DATA SEEDING
-- ====================================================
INSERT INTO settings (app_name, app_slogan, support_email) 
VALUES ('Zellige Stays', 'Authentic Moroccan Experience', 'support@zelligestays.com')
ON DUPLICATE KEY UPDATE app_name = app_name;

INSERT INTO users (id, full_name, email, password_hash, role) 
VALUES ('usr_admin123', 'Super Admin', 'admin@zelligestays.com', 'seeded_password_hash', 'admin');

INSERT INTO cities (id, name_ar, name_fr, name_en, name_es, is_featured) 
VALUES 
('cit_rak', 'مراكش', 'Marrakech', 'Marrakech', 'Marrakech', 1),
('cit_fez', 'فاس', 'Fès', 'Fez', 'Fez', 1);

INSERT INTO categories (id, name_ar, name_fr, name_en, name_es) 
VALUES 
('cat_riad', 'رياض', 'Riad', 'Riad', 'Riad'),
('cat_villa', 'فيلا', 'Villa', 'Villa', 'Villa');

SET FOREIGN_KEY_CHECKS = 1;
