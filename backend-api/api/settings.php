<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';

try {
    $stmt = $conn->prepare("SELECT * FROM settings LIMIT 1");
    $stmt->execute();
    $settings = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$settings) {
        $settings = [
            "app_name" => "Zellige Stays",
            "monthly_subscription_price" => 99,
            "free_trial_duration_months" => 2
        ];
    }

    echo json_encode([
        "success" => true,
        "settings" => $settings
    ]);
} catch (PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}
?>
