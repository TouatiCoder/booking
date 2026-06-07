<?php
require_once 'includes/session.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action']) && $_POST['action'] == 'update_branding') {
    $stmt = $conn->prepare("UPDATE settings SET app_name=?, app_logo=?, app_slogan=?, support_email=?, support_phone=?, primary_color=?, secondary_color=?");
    $stmt->execute([
        $_POST['app_display_name'], 
        $_POST['app_logo'], 
        $_POST['app_slogan'], 
        $_POST['support_email'], 
        $_POST['support_phone'], 
        $_POST['primary_color'], 
        $_POST['secondary_color']
    ]);
    header("Location: branding.php?success=1");
    exit();
}

$settingsQuery = $conn->query("SELECT * FROM settings LIMIT 1")->fetch(PDO::FETCH_ASSOC);

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Branding Settings</h3>
</div>

<?php if(isset($_GET['success'])): ?>
    <div class="alert alert-success">Branding Settings updated successfully!</div>
<?php endif; ?>

<div class="card shadow border-0" style="max-width: 600px;">
    <div class="card-body p-4">
        <form method="POST">
            <input type="hidden" name="action" value="update_branding">
            
            <div class="mb-4">
                <label class="form-label fw-bold">Application Display Name</label>
                <input type="text" name="app_display_name" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_name'] ?? 'Zellige Stays') ?>" required>
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">App Logo URL</label>
                <input type="text" name="app_logo" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_logo'] ?? '') ?>">
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">App Slogan</label>
                <input type="text" name="app_slogan" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_slogan'] ?? 'Authentic Moroccan Experience') ?>">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">Support Email</label>
                <input type="email" name="support_email" class="form-control" value="<?= htmlspecialchars($settingsQuery['support_email'] ?? 'support@zelligestays.com') ?>">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">Support Phone</label>
                <input type="text" name="support_phone" class="form-control" value="<?= htmlspecialchars($settingsQuery['support_phone'] ?? '+212 500 000 000') ?>">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">Primary Color (Hex)</label>
                <input type="text" name="primary_color" class="form-control" value="<?= htmlspecialchars($settingsQuery['primary_color'] ?? '#0A2540') ?>">
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">Secondary Color (Hex)</label>
                <input type="text" name="secondary_color" class="form-control" value="<?= htmlspecialchars($settingsQuery['secondary_color'] ?? '#D4AF37') ?>">
            </div>
            
            <button type="submit" class="btn btn-primary px-4 fw-bold">Save Branding</button>
        </form>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
