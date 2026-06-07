<?php
require_once 'includes/session.php';

$subs = $conn->query("
    SELECT s.*, u.name as host_name 
    FROM subscriptions s 
    JOIN users u ON s.host_id = u.id 
    ORDER BY s.createdAt DESC
")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Host Subscriptions Engine</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Sub ID</th>
                        <th>Host</th>
                        <th>Plan Name</th>
                        <th>Coverage Start</th>
                        <th>Coverage End</th>
                        <th class="pe-4 text-end">Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($subs as $sub): ?>
                    <tr>
                        <td class="ps-4 text-muted small user-select-all"><?= substr($sub['id'], 0, 8) ?></td>
                        <td class="fw-bold"><i class="fas fa-crown text-warning me-1"></i> <?= htmlspecialchars($sub['host_name']) ?></td>
                        <td class="text-primary fw-bold"><?= htmlspecialchars($sub['plan_name']) ?></td>
                        <td><?= date('Y-m-d', strtotime($sub['start_date'])) ?></td>
                        <td><?= date('Y-m-d', strtotime($sub['end_date'])) ?></td>
                        <td class="pe-4 text-end">
                            <?php if($sub['status'] === 'active'): ?>
                                <span class="badge bg-success rounded-pill px-3 py-2">Active</span>
                            <?php elseif($sub['status'] === 'expired'): ?>
                                <span class="badge bg-danger rounded-pill px-3 py-2">Expired</span>
                            <?php else: ?>
                                <span class="badge bg-secondary rounded-pill px-3 py-2"><?= ucfirst($sub['status']) ?></span>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($subs)): ?>
                    <tr><td colspan="6" class="text-center py-4 text-muted">No subscriptions recorded.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
