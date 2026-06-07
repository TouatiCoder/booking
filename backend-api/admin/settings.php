<?php
require_once 'includes/session.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action']) && $_POST['action'] == 'update_settings') {
    $stmt = $conn->prepare("UPDATE settings SET monthly_subscription_price = ?, free_trial_duration_months = ?");
    $stmt->execute([$_POST['monthly_subscription_price'], $_POST['free_trial_duration_months']]);
    
    header("Location: settings.php?success=1");
    exit();
}

$settingsQuery = $conn->query("SELECT * FROM settings LIMIT 1")->fetch(PDO::FETCH_ASSOC);

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Platform Settings</h3>
</div>

<?php if(isset($_GET['success'])): ?>
    <div class="alert alert-success">Settings updated successfully!</div>
<?php endif; ?>

<div class="card shadow border-0" style="max-width: 600px;">
    <div class="card-body p-4">
        <form method="POST">
            <input type="hidden" name="action" value="update_settings">
            
            <div class="mb-4">
                <label class="form-label fw-bold">Monthly Subscription Price (MAD)</label>
                <input type="number" step="0.01" name="monthly_subscription_price" class="form-control" value="<?= htmlspecialchars($settingsQuery['monthly_subscription_price'] ?? '99') ?>" required>
                <small class="text-muted">This price will be applied to all host subscription renewals.</small>
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">Free Trial Duration (Months)</label>
                <input type="number" name="free_trial_duration_months" class="form-control" value="<?= htmlspecialchars($settingsQuery['free_trial_duration_months'] ?? '2') ?>" required>
                <small class="text-muted">The duration new hosts can use the platform for free before being charged.</small>
            </div>
            
            <button type="submit" class="btn btn-primary px-4 fw-bold">Save Settings</button>
        </form>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
