<?php
require_once 'includes/session.php';

$reservations = $conn->query("
    SELECT r.*, p.title as property_title, u.full_name as traveler_name, h.full_name as host_name 
    FROM reservations r 
    JOIN properties p ON r.property_id = p.id 
    JOIN users u ON r.traveler_id = u.id 
    JOIN users h ON r.host_id = h.id
    ORDER BY r.created_at DESC
")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Live Reservations Center</h3>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Trx ID</th>
                        <th>Property</th>
                        <th>Traveler</th>
                        <th>Host</th>
                        <th>Duration</th>
                        <th>Amount</th>
                        <th class="pe-4 text-end">Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($reservations as $res): ?>
                    <tr>
                        <td class="ps-4 text-muted small user-select-all"><?= substr($res['id'], 0, 8) ?>...</td>
                        <td class="fw-bold"><?= htmlspecialchars(substr($res['property_title'], 0, 30)) ?><?= strlen($res['property_title']) > 30 ? '...' : '' ?></td>
                        <td><?= htmlspecialchars($res['traveler_name']) ?></td>
                        <td><?= htmlspecialchars($res['host_name']) ?></td>
                        <td class="small text-muted"><?= $res['start_date'] ?> <br>to <?= $res['end_date'] ?></td>
                        <td class="text-success fw-bold">$<?= $res['total_price'] ?></td>
                        <td class="pe-4 text-end">
                            <?php if($res['status'] === 'confirmed'): ?>
                                <span class="badge bg-success rounded-pill px-3 py-2"><i class="fas fa-check"></i> Confirmed</span>
                            <?php elseif($res['status'] === 'pending'): ?>
                                <span class="badge bg-warning text-dark rounded-pill px-3 py-2"><i class="fas fa-clock"></i> Pending</span>
                            <?php else: ?>
                                <span class="badge bg-secondary rounded-pill px-3 py-2"><?= ucfirst($res['status']) ?></span>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if(empty($reservations)): ?>
                    <tr><td colspan="7" class="text-center py-4 text-muted">No reservations found.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
