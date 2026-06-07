<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (isset($_GET['action']) && $_GET['action'] == 'can_review') {
        if (!isset($_GET['user_id']) || !isset($_GET['guide_id'])) sendResponse(400, "Missing parameters");
        $stmt = $conn->prepare("
            SELECT b.id FROM guide_bookings b
            WHERE b.traveler_id = ? AND b.guide_id = ? AND b.status = 'completed' AND b.payment_status = 'paid'
            AND b.id NOT IN (SELECT booking_id FROM guide_reviews WHERE user_id = ?)
            LIMIT 1
        ");
        $stmt->execute([$_GET['user_id'], $_GET['guide_id'], $_GET['user_id']]);
        $booking = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($booking) echo json_encode(["success" => true, "can_review" => true, "reservation_id" => $booking['id']]);
        else echo json_encode(["success" => true, "can_review" => false]);
        exit;
    } else if (isset($_GET['guide_id'])) {
        $stmt = $conn->prepare("SELECT r.*, u.name as user_name FROM guide_reviews r JOIN users u ON r.user_id = u.id WHERE r.guide_id = ? AND r.is_hidden = 0 ORDER BY r.created_at DESC");
        $stmt->execute([$_GET['guide_id']]);
        echo json_encode(["success" => true, "data" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    } else {
        sendResponse(400, "Missing guide_id");
    }
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (!isset($data->guide_id) || !isset($data->user_id) || !isset($data->booking_id) || !isset($data->rating) || !isset($data->comment)) {
        sendResponse(400, "Missing parameters");
    }

    $id = uniqid("gr_");
    $stmt = $conn->prepare("INSERT INTO guide_reviews (id, guide_id, user_id, booking_id, rating, comment) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$id, $data->guide_id, $data->user_id, $data->booking_id, $data->rating, $data->comment]);

    // Recalculate guide rating
    $stmt = $conn->prepare("SELECT AVG(rating) as avg_rating, COUNT(id) as total FROM guide_reviews WHERE guide_id = ? AND is_hidden = 0");
    $stmt->execute([$data->guide_id]);
    $stats = $stmt->fetch(PDO::FETCH_ASSOC);

    $avg = round($stats['avg_rating'], 2);
    $total = $stats['total'];
    $conn->prepare("UPDATE guides SET rating = ?, total_reviews = ? WHERE id = ?")->execute([$avg, $total, $data->guide_id]);

    echo json_encode(["success" => true, "message" => "Review submitted"]);
}
?>
