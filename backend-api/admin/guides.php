<?php
require_once 'includes/session.php';

if (isset($_GET['action'])) {
    $id = $_GET['id'];
    if ($_GET['action'] == 'approve') {
        $conn->prepare("UPDATE guides SET status = 'approved' WHERE id = ?")->execute([$id]);
        
        $stmt = $conn->prepare("SELECT user_id FROM guides WHERE id = ?");
        $stmt->execute([$id]);
        $uid = $stmt->fetchColumn();
        if ($uid) {
            $conn->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)")->execute([$uid, "Profile Approved", "Your guide profile is now public."]);
        }
    } elseif ($_GET['action'] == 'reject') {
        $conn->prepare("UPDATE guides SET status = 'rejected' WHERE id = ?")->execute([$id]);
        
        $stmt = $conn->prepare("SELECT user_id FROM guides WHERE id = ?");
        $stmt->execute([$id]);
        $uid = $stmt->fetchColumn();
        if ($uid) {
            $conn->prepare("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)")->execute([$uid, "Profile Rejected", "Your guide profile was rejected."]);
        }
    } elseif ($_GET['action'] == 'suspend') {
        $conn->prepare("UPDATE guides SET status = 'suspended' WHERE id = ?")->execute([$id]);
    }
    header("Location: guides.php");
    exit();
}

$guides = $conn->query("SELECT g.*, u.full_name as name, u.email, c.name_en as city_name FROM guides g JOIN users u ON g.user_id = u.id LEFT JOIN cities c ON g.city_id = c.id ORDER BY g.created_at DESC")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Manage Guides</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Name</th>
                        <th>City</th>
                        <th>Languages</th>
                        <th>Price/Day</th>
                        <th>Rating</th>
                        <th>Status</th>
                        <th class="pe-4 text-end">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($guides as $g): ?>
                    <tr>
                        <td class="ps-4 fw-bold">
                            <?= htmlspecialchars($g['name']) ?><br>
                            <small class="text-muted fw-normal"><?= htmlspecialchars($g['email']) ?></small>
                        </td>
                        <td><?= htmlspecialchars($g['city_name'] ?? 'N/A') ?></td>
                        <td><?= htmlspecialchars($g['languages']) ?></td>
                        <td><?= htmlspecialchars($g['price_per_day']) ?> MAD</td>
                        <td><i class="fas fa-star text-warning"></i> <?= $g['rating'] ?> (<?= $g['total_reviews'] ?>)</td>
                        <td>
                            <?php if($g['status'] == 'approved'): ?>
                                <span class="badge bg-success">Approved</span>
                            <?php elseif($g['status'] == 'rejected'): ?>
                                <span class="badge bg-danger">Rejected</span>
                            <?php elseif($g['status'] == 'suspended'): ?>
                                <span class="badge bg-secondary">Suspended</span>
                            <?php else: ?>
                                <span class="badge bg-warning text-dark">Pending</span>
                            <?php endif; ?>
                        </td>
                        <td class="pe-4 text-end">
                            <?php if($g['status'] == 'pending'): ?>
                                <a href="?action=approve&id=<?= $g['id'] ?>" class="btn btn-sm btn-outline-success"><i class="fas fa-check"></i> Approve</a>
                                <a href="?action=reject&id=<?= $g['id'] ?>" class="btn btn-sm btn-outline-danger"><i class="fas fa-times"></i> Reject</a>
                            <?php elseif($g['status'] == 'approved'): ?>
                                <a href="?action=suspend&id=<?= $g['id'] ?>" class="btn btn-sm btn-outline-warning"><i class="fas fa-ban"></i> Suspend</a>
                            <?php elseif($g['status'] == 'suspended'): ?>
                                <a href="?action=approve&id=<?= $g['id'] ?>" class="btn btn-sm btn-outline-success"><i class="fas fa-check"></i> Reactivate</a>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
