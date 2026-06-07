<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (!isset($_GET['conversation_id'])) {
        sendResponse(400, "Missing conversation_id");
    }
    
    $stmt = $conn->prepare("SELECT * FROM messages WHERE conversation_id = ? ORDER BY created_at ASC");
    $stmt->execute([$_GET['conversation_id']]);
    $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode(["success" => true, "data" => $messages]);
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (!isset($data->conversation_id) || !isset($data->sender_id) || !isset($data->message)) {
        sendResponse(400, "Missing parameters");
    }
    
    $id = uniqid("msg_");
    $stmt = $conn->prepare("INSERT INTO messages (id, conversation_id, sender_id, message) VALUES (?, ?, ?, ?)");
    $stmt->execute([$id, $data->conversation_id, $data->sender_id, $data->message]);
    
    // Send Notification (Simulated FCM via Notifications table)
    $stmt = $conn->prepare("SELECT client_id, host_id FROM conversations WHERE id = ?");
    $stmt->execute([$data->conversation_id]);
    $conv = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($conv) {
        $receiver_id = ($conv['client_id'] === $data->sender_id) ? $conv['host_id'] : $conv['client_id'];
        
        $notifStmt = $conn->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
        $notifStmt->execute([$receiver_id, "New Message Received", substr($data->message, 0, 50)]);
    }
    
    echo json_encode(["success" => true, "data" => ["message_id" => $id, "created_at" => date('Y-m-d H:i:s')]]);
}
?>
