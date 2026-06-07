<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../../config/database.php';
require_once '../../helpers/ResponseHelper.php';
require_once '../../helpers/EmailHelper.php';

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->name) || !isset($data->email) || !isset($data->password) || !isset($data->role)) {
    sendResponse(400, "Missing required fields");
}

$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->execute([$data->email]);
if ($stmt->fetch()) {
    sendResponse(400, "Email already exists");
}

$id = uniqid('usr_');
$hashedPass = password_hash($data->password, PASSWORD_DEFAULT);

$conn->beginTransaction();
try {
    $stmt = $conn->prepare("INSERT INTO users (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$id, $data->name, $data->email, $hashedPass, $data->role]);
    
    // If Host, give a free trial subscription based on Admin Settings
    if ($data->role === 'host') {
        // Fetch free trial duration
        $setStmt = $conn->query("SELECT setting_value FROM settings WHERE setting_key = 'free_trial_duration_months'");
        $months = $setStmt->fetchColumn();
        if (!$months) $months = 2; // fallback

        $sub_id = uniqid('sub_');
        $end_date = date('Y-m-d', strtotime("+$months months"));
        $subStmt = $conn->prepare("INSERT INTO subscriptions (id, host_email, host_id, plan_name, start_date, end_date, amount, status) VALUES (?, ?, ?, 'Free Trial', CURDATE(), ?, 0, 'active')");
        $subStmt->execute([$sub_id, $data->email, $id, $end_date]);
    }

    $conn->commit();
    sendResendEmail($data->email, "Welcome to Zellige Stays", "Your account has been created successfully!");
    
    // Return standard response
    echo json_encode([
        "success" => true,
        "message" => "Registration successful",
        "user" => [
            "id" => $id,
            "name" => $data->name,
            "email" => $data->email,
            "role" => $data->role
        ],
        "token" => "mock_token_" . time()
    ]);
} catch (Exception $e) {
    $conn->rollBack();
    sendResponse(500, "Failed to register user: " . $e->getMessage());
}
?>
