<?php
require_once 'includes/session.php';

// Fetch all conversations
$convStmt = $conn->prepare("
    SELECT c.*, p.title as property_title, u1.name as client_name, u2.name as host_name 
    FROM conversations c
    JOIN properties p ON c.property_id = p.id
    JOIN users u1 ON c.client_id = u1.id
    JOIN users u2 ON c.host_id = u2.id
    ORDER BY c.created_at DESC
");
$convStmt->execute();
$conversations = $convStmt->fetchAll(PDO::FETCH_ASSOC);

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Messages Monitoring</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Date</th>
                        <th>Property</th>
                        <th>Client</th>
                        <th>Host</th>
                        <th class="pe-4 text-end">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($conversations as $c): ?>
                    <tr>
                        <td class="ps-4 text-muted small"><?= date('Y-m-d H:i', strtotime($c['created_at'])) ?></td>
                        <td class="fw-bold"><?= htmlspecialchars($c['property_title']) ?></td>
                        <td><?= htmlspecialchars($c['client_name']) ?></td>
                        <td><?= htmlspecialchars($c['host_name']) ?></td>
                        <td class="pe-4 text-end">
                            <a href="view_conversation.php?id=<?= $c['id'] ?>" class="btn btn-sm btn-outline-primary">View Log</a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($conversations)): ?>
                    <tr><td colspan="5" class="text-center py-4 text-muted">No conversations found.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
