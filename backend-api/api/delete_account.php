<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';
// require_once '../config/jwt.php';

$data = json_decode(file_get_contents("php://input"));
if(!isset($data->email)) {
    sendResponse(400, "Missing email");
}

$email = $data->email;

$conn->beginTransaction();
try {
    // Check user role
    $stmt = $conn->prepare("SELECT id, role, email FROM users WHERE email = ? AND deleted_at IS NULL");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        sendResponse(404, "User not found or already deleted");
    }
    
    $user_id = $user['id'];

    // Soft delete user
    $updStmt = $conn->prepare("UPDATE users SET deleted_at = NOW() WHERE email = ?");
    $updStmt->execute([$email]);

    if ($user['role'] === 'traveler') {
        // Clear favorites
        $favStmt = $conn->prepare("DELETE FROM favorites WHERE userEmail = ?");
        $favStmt->execute([$user['email']]);
        
        // Disable notifications
        $notifStmt = $conn->prepare("DELETE FROM notifications WHERE user_id = ?");
        $notifStmt->execute([$user_id]);
    } else if ($user['role'] === 'host') {
        // Hide properties
        $propStmt = $conn->prepare("UPDATE properties SET status = 'deleted' WHERE host_id = ?");
        $propStmt->execute([$user_id]);

        // Cancel subscriptions
        $subStmt = $conn->prepare("UPDATE subscriptions SET status = 'cancelled' WHERE host_id = ? AND status = 'active'");
        $subStmt->execute([$user_id]);

        // Disable notifications
        $notifStmt = $conn->prepare("DELETE FROM notifications WHERE user_id = ?");
        $notifStmt->execute([$user_id]);
    }

    $conn->commit();
    echo json_encode(["success" => true, "message" => "Account successfully deleted"]);
} catch (Exception $e) {
    $conn->rollBack();
    sendResponse(500, "Failed to delete account: " . $e->getMessage());
}
?>
