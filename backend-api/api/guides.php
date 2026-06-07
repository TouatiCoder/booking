<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (isset($_GET['action']) && $_GET['action'] == 'my_profile') {
        if (!isset($_GET['user_id'])) sendResponse(400, "Missing user_id");
        $stmt = $conn->prepare("SELECT g.*, c.name_en as city_name, u.full_name as name, u.email FROM guides g LEFT JOIN cities c ON g.city_id = c.id JOIN users u ON g.user_id = u.id WHERE g.user_id = ?");
        $stmt->execute([$_GET['user_id']]);
        $guide = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($guide) {
            echo json_encode(["success" => true, "data" => $guide]);
        } else {
            echo json_encode(["success" => true, "data" => null]);
        }
    } else if (isset($_GET['id'])) {
        $stmt = $conn->prepare("SELECT g.*, c.name_en as city_name, u.full_name as name, u.email FROM guides g LEFT JOIN cities c ON g.city_id = c.id JOIN users u ON g.user_id = u.id WHERE g.id = ?");
        $stmt->execute([$_GET['id']]);
        $guide = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode(["success" => true, "data" => $guide]);
    } else {
        $query = "SELECT g.*, c.name_en as city_name, u.full_name as name, u.email FROM guides g LEFT JOIN cities c ON g.city_id = c.id JOIN users u ON g.user_id = u.id WHERE g.status = 'approved'";
        $params = [];
        
        if (isset($_GET['city_id']) && !empty($_GET['city_id'])) {
            $query .= " AND g.city_id = ?";
            $params[] = $_GET['city_id'];
        }
        
        $query .= " ORDER BY g.rating DESC, g.created_at DESC";
        $stmt = $conn->prepare($query);
        $stmt->execute($params);
        $guides = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo json_encode(["success" => true, "data" => $guides]);
    }
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (isset($data->action) && $data->action == 'create_profile') {
        if (!isset($data->user_id) || !isset($data->price_per_day)) {
            sendResponse(400, "Missing user_id or price_per_day");
        }
        $id = uniqid("gd_");
        $stmt = $conn->prepare("INSERT INTO guides (id, user_id, description, languages, city_id, phone, whatsapp, price_per_day, experience_years, specialties) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->execute([
            $id, $data->user_id, $data->description ?? '', $data->languages ?? '', 
            $data->city_id ?? null, $data->phone ?? '', $data->whatsapp ?? '',
            $data->price_per_day, $data->experience_years ?? 0, $data->specialties ?? ''
        ]);
        
        // Update user role to guide
        $conn->prepare("UPDATE users SET role = 'guide' WHERE id = ? AND role != 'admin'")->execute([$data->user_id]);
        
        echo json_encode(["success" => true, "message" => "Profile created and pending admin approval"]);
    }
}
?>
