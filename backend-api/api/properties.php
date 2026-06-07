<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';

// Visibility Rules: Property status = approved AND Host subscription_status = active
$stmt = $conn->prepare("
    SELECT p.* FROM properties p
    JOIN subscriptions s ON p.host_id = s.host_email
    JOIN users u ON p.host_id = u.id
    WHERE p.status = 'approved' AND s.status = 'active' AND u.deleted_at IS NULL
");
$stmt->execute();
$properties = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Map images field
$result = [];
foreach ($properties as $p) {
    if (isset($p['imageUrls'])) {
        $p['images'] = explode(',', $p['imageUrls']);
    } else {
        $p['images'] = [];
    }
    $p['price_per_night'] = (float)$p['price'];
    $p['city_id'] = $p['city'];
    $p['property_type'] = $p['propertyType'];
    $result[] = $p;
}

echo json_encode($result);
?>
