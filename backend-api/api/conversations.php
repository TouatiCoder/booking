<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (!isset($_GET['user_id'])) {
        sendResponse(400, "Missing user_id");
    }
    
    $user_id = $_GET['user_id'];
    
    // Check user role
    $stmt = $conn->prepare("SELECT role FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        sendResponse(404, "User not found");
    }
    
    // Fetch conversations
    if ($user['role'] === 'host' || $user['role'] === 'admin') {
        // Assume hosts and admins might need slightly different fetching
        // But the requirements say Admin can view all.
        if ($user['role'] === 'admin') {
            $stmt = $conn->prepare("
                SELECT c.*, p.title as property_title, u1.full_name as client_name, u2.full_name as host_name 
                FROM conversations c
                JOIN properties p ON c.property_id = p.id
                JOIN users u1 ON c.client_id = u1.id
                JOIN users u2 ON c.host_id = u2.id
                ORDER BY c.created_at DESC
            ");
            $stmt->execute();
        } else {
            $stmt = $conn->prepare("
                SELECT c.*, p.title as property_title, u1.full_name as client_name, u2.full_name as host_name 
                FROM conversations c
                JOIN properties p ON c.property_id = p.id
                JOIN users u1 ON c.client_id = u1.id
                JOIN users u2 ON c.host_id = u2.id
                WHERE c.host_id = ?
                ORDER BY c.created_at DESC
            ");
            $stmt->execute([$user_id]);
        }
    } else {
        $stmt = $conn->prepare("
            SELECT c.*, p.title as property_title, u1.full_name as client_name, u2.full_name as host_name 
            FROM conversations c
            JOIN properties p ON c.property_id = p.id
            JOIN users u1 ON c.client_id = u1.id
            JOIN users u2 ON c.host_id = u2.id
            WHERE c.client_id = ?
            ORDER BY c.created_at DESC
        ");
        $stmt->execute([$user_id]);
    }
    
    $conversations = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(["success" => true, "data" => $conversations]);
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (!isset($data->property_id) || !isset($data->client_id) || !isset($data->host_id)) {
        sendResponse(400, "Missing parameters");
    }
    
    // Check if conversation already exists
    $stmt = $conn->prepare("SELECT id FROM conversations WHERE property_id = ? AND client_id = ? AND host_id = ?");
    $stmt->execute([$data->property_id, $data->client_id, $data->host_id]);
    $existing = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($existing) {
        echo json_encode(["success" => true, "data" => ["conversation_id" => $existing['id']]]);
        exit;
    }
    
    $id = uniqid("conv_");
    $stmt = $conn->prepare("INSERT INTO conversations (id, property_id, client_id, host_id) VALUES (?, ?, ?, ?)");
    $stmt->execute([$id, $data->property_id, $data->client_id, $data->host_id]);
    
    echo json_encode(["success" => true, "data" => ["conversation_id" => $id]]);
}
?>
