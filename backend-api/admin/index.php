<?php
require_once 'includes/session.php';

$users_count = $conn->query("SELECT COUNT(*) FROM users WHERE role = 'traveler'")->fetchColumn();
$hosts_count = $conn->query("SELECT COUNT(*) FROM users WHERE role = 'host'")->fetchColumn();
$properties_count = $conn->query("SELECT COUNT(*) FROM properties")->fetchColumn();
$pending_properties = $conn->query("SELECT COUNT(*) FROM properties WHERE status = 'pending'")->fetchColumn();
$reservations_count = $conn->query("SELECT COUNT(*) FROM reservations")->fetchColumn();

require_once 'includes/header.php';
?>
<h3 class="mb-4 fw-bold">Dashboard Overview</h3>
<div class="row">
    <div class="col-md-3">
        <div class="card text-white bg-primary mb-4 shadow border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase fw-bold text-white-50">Travelers</h6>
                        <h2 class="mb-0 fw-bold"><?= $users_count ?></h2>
                    </div>
                    <i class="fas fa-users fa-3x text-white-50"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-info mb-4 shadow border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase fw-bold text-white-50">Hosts</h6>
                        <h2 class="mb-0 fw-bold"><?= $hosts_count ?></h2>
                    </div>
                    <i class="fas fa-house-user fa-3x text-white-50"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-success mb-4 shadow border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase fw-bold text-white-50">Total Properties</h6>
                        <h2 class="mb-0 fw-bold"><?= $properties_count ?></h2>
                    </div>
                    <i class="fas fa-building fa-3x text-white-50"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-warning mb-4 shadow border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase fw-bold text-white-50">Pending Approvals</h6>
                        <h2 class="mb-0 fw-bold"><?= $pending_properties ?></h2>
                    </div>
                    <i class="fas fa-clock fa-3x text-white-50"></i>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-danger mb-4 shadow border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase fw-bold text-white-50">Reservations</h6>
                        <h2 class="mb-0 fw-bold"><?= $reservations_count ?></h2>
                    </div>
                    <i class="fas fa-calendar-check fa-3x text-white-50"></i>
                </div>
            </div>
        </div>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
