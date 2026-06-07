<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';

$stmt = $conn->prepare("SELECT setting_key, setting_value FROM settings");
$stmt->execute();
$settings = $stmt->fetchAll(PDO::FETCH_KEY_PAIR);

if (!$settings) {
    echo json_encode(["monthly_subscription_price" => "99", "free_trial_duration_months" => "2"]);
    exit();
}
echo json_encode($settings);
?>
