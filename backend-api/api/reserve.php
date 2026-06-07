<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';
require_once '../config/jwt.php';

$data = json_decode(file_get_contents("php://input"));
if(!isset($data->property_id) || !isset($data->traveler_id) || !isset($data->start_date) || !isset($data->end_date)) {
    sendResponse(400, "Missing reservation details");
}

$payment_method = isset($data->payment_method) ? $data->payment_method : 'credit_card';

// Check if property is visible (approved and host has active sub)
$propStmt = $conn->prepare("SELECT p.host_id FROM properties p 
    JOIN subscriptions s ON p.host_id = s.host_email 
    WHERE p.id = ? AND p.status = 'approved' AND s.status = 'active'");
$propStmt->execute([$data->property_id]);
$property = $propStmt->fetch(PDO::FETCH_ASSOC);

if (!$property) {
    sendResponse(403, "Property is not available for reservation");
}

$host_id = $property['host_id'];
$status = 'pending_payment'; // Initial status

// Insert Reservation
$conn->beginTransaction();
try {
    $stmt = $conn->prepare("INSERT INTO reservations (id, property_id, traveler_id, host_id, start_date, end_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$data->id, $data->property_id, $data->traveler_id, $host_id, $data->start_date, $data->end_date, $data->total_price, $status]);

    // Insert Payment
    $payment_id = uniqid('pay_');
    // Assuming payment logic succeeds instantly for the mock
    $payment_status = 'paid';
    $res_status = 'confirmed';

    $payStmt = $conn->prepare("INSERT INTO payments (id, user_id, reservation_id, amount, currency, payment_method, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $payStmt->execute([$payment_id, $data->traveler_id, $data->id, $data->total_price, 'MAD', $payment_method, $payment_status]);

    // Update reservation to confirmed
    $updStmt = $conn->prepare("UPDATE reservations SET status = 'confirmed' WHERE id = ?");
    $updStmt->execute([$data->id]);

    $conn->commit();

    // Trigger Email logic (placeholder/mock in a real app this would send Resend emails)
    // require_once '../helpers/EmailHelper.php';
    // sendResendEmail($data->traveler_id, "Reservation Confirmed", "Your payment is processed and reservation is confirmed.");
    // sendResendEmail($host_id, "New Reservation", "You have a new reservation confirmed.");

    sendResponse(200, "Reservation and payment successful");
} catch (Exception $e) {
    $conn->rollBack();
    sendResponse(500, "Transaction failed: " . $e->getMessage());
}
?>
