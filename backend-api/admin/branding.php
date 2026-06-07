<?php
require_once 'includes/session.php';

$branding_keys = [
    'app_display_name',
    'app_logo',
    'app_slogan',
    'support_email',
    'support_phone',
    'primary_color',
    'secondary_color',
    'welcome_banner_image'
];

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action']) && $_POST['action'] == 'update_branding') {
    foreach ($branding_keys as $key) {
        if (isset($_POST[$key])) {
            $stmt = $conn->prepare("INSERT INTO settings (setting_key, setting_value, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE setting_value = ?");
            $stmt->execute([$key, $_POST[$key], "Branding setting for $key", $_POST[$key]]);
        }
    }
    header("Location: branding.php?success=1");
    exit();
}

$settingsQuery = $conn->query("SELECT * FROM settings")->fetchAll(PDO::FETCH_KEY_PAIR);

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
                <input type="text" name="app_display_name" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_display_name'] ?? 'Zellige Stays') ?>" required>
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">App Logo URL</label>
                <input type="text" name="app_logo" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_logo'] ?? '') ?>">
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">App Slogan</label>
                <input type="text" name="app_slogan" class="form-control" value="<?= htmlspecialchars($settingsQuery['app_slogan'] ?? 'Marhaban • Welcome') ?>">
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
                <input type="text" name="primary_color" class="form-control" value="<?= htmlspecialchars($settingsQuery['primary_color'] ?? '#AB8749') ?>">
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">Secondary Color (Hex)</label>
                <input type="text" name="secondary_color" class="form-control" value="<?= htmlspecialchars($settingsQuery['secondary_color'] ?? '#0D2133') ?>">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">Welcome Banner Image URL</label>
                <input type="text" name="welcome_banner_image" class="form-control" value="<?= htmlspecialchars($settingsQuery['welcome_banner_image'] ?? '') ?>">
            </div>
            
            <button type="submit" class="btn btn-primary px-4 fw-bold">Save Branding</button>
        </form>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
