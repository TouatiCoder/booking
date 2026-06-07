<?php
require_once 'includes/session.php';

$notifs = $conn->query("
    SELECT n.*, u.full_name as user_name 
    FROM notifications n 
    JOIN users u ON n.user_id = u.id 
    ORDER BY n.created_at DESC LIMIT 150
")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">System Notifications Feed</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Timestamp</th>
                        <th>Target User</th>
                        <th>Title</th>
                        <th>Payload Context Message</th>
                        <th class="pe-4 text-end">Read Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($notifs as $n): ?>
                    <tr>
                        <td class="ps-4 text-muted small"><?= date('Y-m-d H:i', strtotime($n['created_at'])) ?></td>
                        <td class="fw-bold"><?= htmlspecialchars($n['user_name']) ?></td>
                        <td><?= htmlspecialchars($n['title']) ?></td>
                        <td><small class="text-muted"><?= htmlspecialchars($n['message']) ?></small></td>
                        <td class="pe-4 text-end">
                            <?php if($n['is_read']): ?>
                                <span class="text-success"><i class="fas fa-check-double"></i> Read</span>
                            <?php else: ?>
                                <span class="text-secondary"><i class="fas fa-envelope"></i> Unread</span>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($notifs)): ?>
                    <tr><td colspan="5" class="text-center py-4 text-muted">No system notifications dispatched.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
