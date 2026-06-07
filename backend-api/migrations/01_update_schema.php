<?php
require_once '../config/database.php';

try {
    // Settings Table
    $conn->exec("CREATE TABLE IF NOT EXISTS settings (
        setting_key VARCHAR(50) PRIMARY KEY,
        setting_value VARCHAR(255) NOT NULL,
        description TEXT
    )");
    
    // Insert defaults if not exists
    $conn->exec("INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES ('monthly_subscription_price', '99', 'Monthly Subscription Price in MAD')");
    $conn->exec("INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES ('free_trial_duration_months', '2', 'Free Trial Duration in Months')");

    // Payments Table
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

    echo "Migrations executed successfully.";
} catch(PDOException $e) {
    echo "Error: " . $e->getMessage();
}
?>
