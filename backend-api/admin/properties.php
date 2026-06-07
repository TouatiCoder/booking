<?php
require_once 'includes/session.php';

if (isset($_GET['action']) && isset($_GET['id'])) {
    $status = $_GET['action'] === 'approve' ? 'approved' : ($_GET['action'] === 'reject' ? 'rejected' : '');
    if ($status) {
        $stmt = $conn->prepare("UPDATE properties SET status = ? WHERE id = ?");
        $stmt->execute([$status, $_GET['id']]);
    }
    header("Location: properties.php");
    exit();
}

$properties = $conn->query("
    SELECT p.*, u.full_name as host_name, c.name_en as city_name 
    FROM properties p 
    JOIN users u ON p.host_id = u.id 
    JOIN cities c ON p.city_id = c.id
    ORDER BY p.created_at DESC
")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Properties & Approvals</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Listing Title</th>
                        <th>Host</th>
                        <th>City</th>
                        <th>Price/Night</th>
                        <th>Status</th>
                        <th class="pe-4 text-end">Review Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($properties as $prop): ?>
                    <tr>
                        <td class="ps-4 fw-bold"><?= htmlspecialchars($prop['title']) ?></td>
                        <td><i class="fas fa-user-circle text-muted me-1"></i> <?= htmlspecialchars($prop['host_name']) ?></td>
                        <td><?= htmlspecialchars($prop['city_name']) ?></td>
                        <td class="text-success fw-bold">$<?= $prop['price_per_night'] ?></td>
                        <td>
                            <?php if($prop['status'] === 'approved'): ?>
                                <span class="badge bg-success rounded-pill px-3 py-2"><i class="fas fa-check-circle me-1"></i> Approved</span>
                            <?php elseif($prop['status'] === 'pending'): ?>
                                <span class="badge bg-warning text-dark rounded-pill px-3 py-2"><i class="fas fa-clock me-1"></i> Pending</span>
                            <?php else: ?>
                                <span class="badge bg-danger rounded-pill px-3 py-2"><i class="fas fa-times-circle me-1"></i> Rejected</span>
                            <?php endif; ?>
                        </td>
                        <td class="pe-4 text-end">
                            <?php if($prop['status'] === 'pending'): ?>
                            <a href="?action=approve&id=<?= $prop['id'] ?>" class="btn btn-sm btn-success shadow-sm me-1"><i class="fas fa-check"></i> Approve</a>
                            <a href="?action=reject&id=<?= $prop['id'] ?>" class="btn btn-sm btn-danger shadow-sm"><i class="fas fa-ban"></i> Reject</a>
                            <?php elseif($prop['status'] === 'approved'): ?>
                            <a href="?action=reject&id=<?= $prop['id'] ?>" class="btn btn-sm btn-outline-danger shadow-sm"><i class="fas fa-ban"></i> Revoke</a>
                            <?php else: ?>
                            <a href="?action=approve&id=<?= $prop['id'] ?>" class="btn btn-sm btn-outline-success shadow-sm"><i class="fas fa-undo"></i> Re-Approve</a>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($properties)): ?>
                    <tr><td colspan="6" class="text-center py-4 text-muted">No properties in the system.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
