<?php
require_once 'includes/session.php';

if (!isset($_GET['id'])) {
    header('Location: messages.php');
    exit;
}

$conv_id = $_GET['id'];

// Get Conversation Info
$convStmt = $conn->prepare("
    SELECT c.*, p.title as property_title, u1.full_name as client_name, u2.full_name as host_name 
    FROM conversations c
    JOIN properties p ON c.property_id = p.id
    JOIN users u1 ON c.client_id = u1.id
    JOIN users u2 ON c.host_id = u2.id
    WHERE c.id = ?
");
$convStmt->execute([$conv_id]);
$conversation = $convStmt->fetch(PDO::FETCH_ASSOC);

if (!$conversation) {
    header('Location: messages.php');
    exit;
}

// Get Messages
$msgStmt = $conn->prepare("
    SELECT m.*, u.full_name as sender_name 
    FROM messages m
    JOIN users u ON m.sender_id = u.id
    WHERE m.conversation_id = ?
    ORDER BY m.created_at ASC
");
$msgStmt->execute([$conv_id]);
$messages = $msgStmt->fetchAll(PDO::FETCH_ASSOC);

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Conversation Log</h3>
    <a href="messages.php" class="btn btn-outline-secondary">Back to List</a>
</div>

<div class="card shadow border-0 mb-4">
    <div class="card-body">
        <h5 class="card-title fw-bold">Context</h5>
        <div class="row">
            <div class="col-md-4">
                <span class="text-muted">Property:</span> <strong><?= htmlspecialchars($conversation['property_title']) ?></strong>
            </div>
            <div class="col-md-4">
                <span class="text-muted">Client:</span> <strong><?= htmlspecialchars($conversation['client_name']) ?></strong>
            </div>
            <div class="col-md-4">
                <span class="text-muted">Host:</span> <strong><?= htmlspecialchars($conversation['host_name']) ?></strong>
            </div>
        </div>
    </div>
</div>

<div class="card shadow border-0">
    <div class="card-body bg-light" style="max-height: 500px; overflow-y: auto;">
        <?php if(empty($messages)): ?>
            <p class="text-center text-muted my-4">No messages in this conversation yet.</p>
        <?php else: ?>
            <div class="d-flex flex-column gap-3">
                <?php foreach($messages as $m): ?>
                    <div class="p-3 bg-white border rounded shadow-sm">
                        <div class="d-flex justify-content-between mb-2 border-bottom pb-2">
                            <span class="fw-bold <?= ($m['sender_id'] == $conversation['host_id']) ? 'text-primary' : 'text-success' ?>"><?= htmlspecialchars($m['sender_name']) ?> <small class="text-muted fw-normal">(<?= ($m['sender_id'] == $conversation['host_id']) ? 'Host' : 'Client' ?>)</small></span>
                            <span class="text-muted small"><?= date('Y-m-d H:i', strtotime($m['created_at'])) ?></span>
                        </div>
                        <div class="text-dark" style="white-space: pre-wrap;"><?= htmlspecialchars($m['message']) ?></div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
