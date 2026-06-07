<?php
$host = "localhost";
$db_name = "zellige_stays";
$username = "root";
$password = "";

try {
    $conn = new PDO("mysql:host=" . $host . ";dbname=" . $db_name, $username, $password);
    $conn->exec("set names utf8");
    
    // Add Settings and Payments tables if not exists
    $conn->exec("CREATE TABLE IF NOT EXISTS settings (
        setting_key VARCHAR(50) PRIMARY KEY,
        setting_value VARCHAR(255) NOT NULL,
        description TEXT
    )");
    $conn->exec("INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES ('monthly_subscription_price', '99', 'Monthly Subscription Price in MAD')");
    $conn->exec("INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES ('free_trial_duration_months', '2', 'Free Trial Duration in Months')");

    $conn->exec("CREATE TABLE IF NOT EXISTS payments (
        id VARCHAR(50) PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        reservation_id VARCHAR(50) NULL,
        subscription_id VARCHAR(50) NULL,
        amount DECIMAL(10, 2) NOT NULL,
        currency VARCHAR(10) DEFAULT 'MAD',
        payment_method VARCHAR(50) NOT NULL,
        payment_status VARCHAR(50) NOT NULL,
        transaction_id VARCHAR(100) NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS conversations (
        id VARCHAR(50) PRIMARY KEY,
        property_id VARCHAR(50) NOT NULL,
        client_id VARCHAR(50) NOT NULL,
        host_id VARCHAR(50) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS messages (
        id VARCHAR(50) PRIMARY KEY,
        conversation_id VARCHAR(50) NOT NULL,
        sender_id VARCHAR(50) NOT NULL,
        message TEXT NOT NULL,
        is_read TINYINT(1) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS reviews (
        id VARCHAR(50) PRIMARY KEY,
        property_id VARCHAR(50) NOT NULL,
        user_id VARCHAR(50) NOT NULL,
        reservation_id VARCHAR(50) NOT NULL,
        rating INT NOT NULL,
        comment TEXT NOT NULL,
        is_hidden TINYINT(1) DEFAULT 0,
        is_reported TINYINT(1) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS guides (
        id VARCHAR(50) PRIMARY KEY,
        user_id VARCHAR(50) NOT NULL,
        photo VARCHAR(255),
        description TEXT,
        languages VARCHAR(255),
        city_id VARCHAR(50),
        phone VARCHAR(50),
        whatsapp VARCHAR(50),
        price_per_day DECIMAL(10,2) NOT NULL,
        experience_years INT DEFAULT 0,
        specialties VARCHAR(255),
        rating DECIMAL(3,2) DEFAULT 0.0,
        total_reviews INT DEFAULT 0,
        status ENUM('pending', 'approved', 'rejected', 'suspended') DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS guide_bookings (
        id VARCHAR(50) PRIMARY KEY,
        traveler_id VARCHAR(50) NOT NULL,
        guide_id VARCHAR(50) NOT NULL,
        date DATE NOT NULL,
        total_price DECIMAL(10,2) NOT NULL,
        payment_status ENUM('pending', 'paid', 'failed') DEFAULT 'pending',
        status ENUM('pending', 'confirmed', 'completed', 'cancelled') DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    $conn->exec("CREATE TABLE IF NOT EXISTS guide_reviews (
        id VARCHAR(50) PRIMARY KEY,
        guide_id VARCHAR(50) NOT NULL,
        user_id VARCHAR(50) NOT NULL,
        booking_id VARCHAR(50) NOT NULL,
        rating INT NOT NULL,
        comment TEXT NOT NULL,
        is_hidden TINYINT(1) DEFAULT 0,
        is_reported TINYINT(1) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    try {
        $conn->query("SELECT rating FROM properties LIMIT 1");
    } catch (PDOException $e) {
        $conn->exec("ALTER TABLE properties ADD COLUMN rating DECIMAL(3,2) DEFAULT 0.0");
        $conn->exec("ALTER TABLE properties ADD COLUMN total_reviews INT DEFAULT 0");
    }

    try {
        $conn->query("SELECT deleted_at FROM users LIMIT 1");
    } catch (PDOException $e) {
        $conn->exec("ALTER TABLE users ADD COLUMN deleted_at DATETIME NULL");
    }

} catch(PDOException $exception) {
    echo "Connection error: " . $exception->getMessage();
}
?>
