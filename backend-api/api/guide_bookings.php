<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    if (isset($_GET['traveler_id'])) {
        $stmt = $conn->prepare("SELECT b.*, g.user_id as guide_user_id, u.full_name as guide_name, g.price_per_day FROM guide_reservations b JOIN guides g ON b.guide_id = g.id JOIN users u ON g.user_id = u.id WHERE b.client_id = ? ORDER BY b.tour_date DESC");
        $stmt->execute([$_GET['traveler_id']]);
        echo json_encode(["success" => true, "data" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    } else if (isset($_GET['guide_id'])) {
        $stmt = $conn->prepare("SELECT b.*, u.full_name as traveler_name, u.email as traveler_email FROM guide_reservations b JOIN users u ON b.client_id = u.id WHERE b.guide_id = ? ORDER BY b.tour_date DESC");
        $stmt->execute([$_GET['guide_id']]);
        echo json_encode(["success" => true, "data" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    } else {
        sendResponse(400, "Missing parameters");
    }
} else if ($method === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    if (!isset($data->traveler_id) || !isset($data->guide_id) || !isset($data->date) || !isset($data->total_price)) {
        sendResponse(400, "Missing parameters");
    }

    $id = uniqid("gb_");
    $stmt = $conn->prepare("INSERT INTO guide_reservations (id, client_id, guide_id, tour_date, guests, total_price, payment_status, reservation_status) VALUES (?, ?, ?, ?, 1, ?, 'paid', 'confirmed')");
    $stmt->execute([$id, $data->traveler_id, $data->guide_id, $data->date, $data->total_price]);

    // Send notification to Guide
    $guideStmt = $conn->prepare("SELECT user_id FROM guides WHERE id = ?");
    $guideStmt->execute([$data->guide_id]);
    $guideUser = $guideStmt->fetch(PDO::FETCH_ASSOC);

    if ($guideUser) {
        $notifStmt = $conn->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)");
        $notifStmt->execute([$guideUser['user_id'], "New Guide Booking", "You have a new booking for " . $data->date]);
    }

    echo json_encode(["success" => true, "message" => "Booking confirmed!", "booking_id" => $id]);
}
?>
