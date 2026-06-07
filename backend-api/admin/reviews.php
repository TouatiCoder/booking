<?php
require_once 'includes/session.php';

// We should ideally rebuild the `average_rating` for property here
function recalculatePropertyRating($conn, $property_id) {
    if (!$property_id) return;
    $stmt = $conn->prepare("SELECT AVG(rating) as avg_rating, COUNT(id) as total FROM property_reviews WHERE property_id = ?");
    $stmt->execute([$property_id]);
    $stats = $stmt->fetch();
    $avg = $stats['avg_rating'] ? round($stats['avg_rating'], 2) : 0;
    $stmt = $conn->prepare("UPDATE properties SET rating = ?, total_reviews = ? WHERE id = ?");
    $stmt->execute([$avg, $stats['total'], $property_id]);
}

if (isset($_GET['action'])) {
    $id = $_GET['id'];
    $propStmt = $conn->prepare("SELECT property_id FROM property_reviews WHERE id = ?");
    $propStmt->execute([$id]);
    $pid = $propStmt->fetchColumn();

    if ($_GET['action'] == 'delete') {
        $conn->prepare("DELETE FROM property_reviews WHERE id = ?")->execute([$id]);
        recalculatePropertyRating($conn, $pid);
    }
    header("Location: reviews.php");
    exit();
}

$reviews = $conn->query("
    SELECT r.*, p.title as property_title, u.full_name as traveler_name 
    FROM property_reviews r 
    JOIN properties p ON r.property_id = p.id 
    JOIN users u ON r.user_id = u.id 
    ORDER BY r.created_at DESC
")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Guest Reviews Moderation</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Date</th>
                        <th>Property</th>
                        <th>Guest</th>
                        <th>Rating</th>
                        <th style="width: 30%;">Comment</th>
                        <th class="pe-4 text-end">Moderation</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($reviews as $r): ?>
                    <tr>
                        <td class="ps-4 text-muted small"><?= date('Y-m-d', strtotime($r['created_at'])) ?></td>
                        <td class="fw-bold text-truncate" style="max-width: 150px;"><?= htmlspecialchars($r['property_title']) ?></td>
                        <td><?= htmlspecialchars($r['traveler_name']) ?></td>
                        <td class="text-warning">
                            <?php for($i=1; $i<=5; $i++): ?>
                                <i class="fas fa-star <?= $i <= $r['rating'] ? '' : 'text-light' ?>"></i>
                            <?php endfor; ?>
                        </td>
                        <td><small class="fst-italic">"<?= htmlspecialchars($r['comment']) ?>"</small></td>
                        <td class="pe-4 text-end">
                            <a href="?action=delete&id=<?= $r['id'] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('Remove this review permanently?')"><i class="fas fa-trash"></i> Drop</a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($reviews)): ?>
                    <tr><td colspan="6" class="text-center py-4 text-muted">No reviews posted yet.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
