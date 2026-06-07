<?php
require_once __DIR__ . '/../config/database.php';

// Automatic Subscription Check
// Current date > subscription_end_date -> status = expired

$stmt = $conn->prepare("UPDATE subscriptions SET status = 'expired' WHERE status = 'active' AND end_date < CURDATE()");
$stmt->execute();
$expiredCount = $stmt->rowCount();

if ($expiredCount > 0) {
    // Optionally: fetch expired subscriptions and trigger email notifications
    // require_once '../helpers/EmailHelper.php';
    echo "Expired $expiredCount subscriptions automatically.\n";
} else {
    echo "No subscriptions expired today.\n";
}
?>
