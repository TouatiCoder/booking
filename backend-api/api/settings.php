<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';

$stmt = $conn->prepare("SELECT * FROM settings LIMIT 1");
$stmt->execute();
$settings = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$settings) {
    echo json_encode(["app_name" => "Zellige Stays", "monthly_subscription_price" => 99, "free_trial_duration_months" => 2]);
    exit();
}
echo json_encode($settings);
?>
