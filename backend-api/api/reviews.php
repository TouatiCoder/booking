<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (isset($_GET['action']) && $_GET['action'] == 'can_review') {
        if (!isset($_GET['user_id']) || !isset($_GET['property_id'])) {
            sendResponse(400, "Missing parameters");
        }
        $user_id = $_GET['user_id'];
        $property_id = $_GET['property_id'];

        // Check if there is a completed paid reservation that hasn't been reviewed
        $stmt = $conn->prepare("
            SELECT r.id FROM reservations r
            JOIN payments p ON r.id = p.reservation_id
            WHERE r.user_email = ? AND r.property_id = ? 
            AND r.status = 'completed' AND p.payment_status = 'paid'
            AND r.id NOT IN (SELECT reservation_id FROM reviews WHERE user_id = ?)
            LIMIT 1
        ");
        $stmt->execute([$user_id, $property_id, $user_id]);
        $reservation = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($reservation) {
            echo json_encode(["success" => true, "can_review" => true, "reservation_id" => $reservation['id']]);
        } else {
            echo json_encode(["success" => true, "can_review" => false]);
        }
        exit;
    } else if (isset($_GET['property_id'])) {
        $property_id = $_GET['property_id'];
        $stmt = $conn->prepare("
            SELECT r.*, u.name as user_name 
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            WHERE r.property_id = ? AND r.is_hidden = 0
            ORDER BY r.created_at DESC
        ");
        $stmt->execute([$property_id]);
        $reviews = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo json_encode(["success" => true, "data" => $reviews]);
        exit;
    } else {
        sendResponse(400, "Missing property_id");
    }
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (!isset($data->property_id) || !isset($data->user_id) || !isset($data->reservation_id) || !isset($data->rating) || !isset($data->comment)) {
        sendResponse(400, "Missing parameters");
    }

    // Verify reservation again just in case
    $stmt = $conn->prepare("
        SELECT r.id FROM reservations r
        JOIN payments p ON r.id = p.reservation_id
        WHERE r.id = ? AND r.user_email = ? AND r.property_id = ?
        AND r.status = 'completed' AND p.payment_status = 'paid'
    ");
    $stmt->execute([$data->reservation_id, $data->user_id, $data->property_id]);
    if (!$stmt->fetch()) {
        sendResponse(403, "Not eligible to review this property");
    }

    // Check if review already exists
    $stmt = $conn->prepare("SELECT id FROM reviews WHERE reservation_id = ?");
    $stmt->execute([$data->reservation_id]);
    if ($stmt->fetch()) {
        sendResponse(400, "Review already submitted for this reservation");
    }

    $id = uniqid("rev_");
    $stmt = $conn->prepare("INSERT INTO reviews (id, property_id, user_id, reservation_id, rating, comment) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$id, $data->property_id, $data->user_id, $data->reservation_id, $data->rating, $data->comment]);

    // Update property rating
    $stmt = $conn->prepare("SELECT AVG(rating) as avg_rating, COUNT(id) as total FROM reviews WHERE property_id = ? AND is_hidden = 0");
    $stmt->execute([$data->property_id]);
    $stats = $stmt->fetch(PDO::FETCH_ASSOC);

    $avg = round($stats['avg_rating'], 2);
    $total = $stats['total'];

    $stmt = $conn->prepare("UPDATE properties SET rating = ?, total_reviews = ? WHERE id = ?");
    $stmt->execute([$avg, $total, $data->property_id]);

    echo json_encode(["success" => true, "message" => "Review submitted successfully"]);
}
?>
