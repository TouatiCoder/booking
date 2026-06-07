<?php
require_once 'includes/session.php';

$payments = $conn->query("
    SELECT p.*, u.full_name as user_name 
    FROM payments p 
    JOIN users u ON p.user_id = u.id 
    ORDER BY p.created_at DESC
")->fetchAll();

$total_revenue = $conn->query("SELECT SUM(amount) FROM payments WHERE payment_status = 'paid'")->fetchColumn();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Payments & Revenue</h3>
    <h4 class="text-success fw-bold m-0">Total Revenue: <?= number_format($total_revenue, 2) ?> MAD</h4>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Trx ID</th>
                        <th>User</th>
                        <th>Amount</th>
                        <th>Method</th>
                        <th>Date</th>
                        <th class="pe-4 text-end">Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($payments as $pay): ?>
                    <tr>
                        <td class="ps-4 text-muted small user-select-all"><?= substr($pay['id'], 0, 10) ?>...</td>
                        <td class="fw-bold"><?= htmlspecialchars($pay['user_name']) ?></td>
                        <td class="text-success fw-bold"><?= number_format($pay['amount'], 2) ?> <?= htmlspecialchars($pay['currency']) ?></td>
                        <td><?= htmlspecialchars(str_replace('_', ' ', strtoupper($pay['payment_method']))) ?></td>
                        <td class="small text-muted"><?= date('Y-m-d H:i', strtotime($pay['created_at'])) ?></td>
                        <td class="pe-4 text-end">
                            <?php if($pay['payment_status'] === 'paid'): ?>
                                <span class="badge bg-success rounded-pill px-3 py-2"><i class="fas fa-check"></i> Paid</span>
                            <?php elseif($pay['payment_status'] === 'pending'): ?>
                                <span class="badge bg-warning text-dark rounded-pill px-3 py-2"><i class="fas fa-clock"></i> Pending</span>
                            <?php else: ?>
                                <span class="badge bg-danger rounded-pill px-3 py-2"><?= ucfirst($pay['payment_status']) ?></span>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($payments)): ?>
                    <tr><td colspan="6" class="text-center py-4 text-muted">No payments recorded yet.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
