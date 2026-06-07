<?php
require_once 'includes/session.php';

if (isset($_GET['delete'])) {
    $stmt = $conn->prepare("DELETE FROM users WHERE id = ? AND role = 'host'");
    $stmt->execute([$_GET['delete']]);
    header("Location: hosts.php");
    exit();
}

$hosts = $conn->query("SELECT * FROM users WHERE role = 'host' AND deleted_at IS NULL ORDER BY createdAt DESC")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Hosts Management</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Joined Date</th>
                        <th class="pe-4 text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($hosts as $host): ?>
                    <tr>
                        <td class="ps-4 text-muted small"><?= htmlspecialchars($host['id']) ?></td>
                        <td class="fw-bold text-primary"><i class="fas fa-house-user me-1"></i> <?= htmlspecialchars($host['name']) ?></td>
                        <td><?= htmlspecialchars($host['email']) ?></td>
                        <td><?= date('Y-m-d H:i', strtotime($host['createdAt'])) ?></td>
                        <td class="pe-4 text-end">
                            <a href="?delete=<?= $host['id'] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete host account and all their properties?')"><i class="fas fa-trash"></i> Ban</a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($hosts)): ?>
                    <tr><td colspan="5" class="text-center py-4 text-muted">No hosts found.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
