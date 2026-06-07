<?php
require_once 'includes/session.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action']) && $_POST['action'] == 'add') {
    $id = uniqid('cat_');
    $stmt = $conn->prepare("INSERT INTO categories (id, name_en, name_ar, name_fr, name_es, icon_url) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$id, $_POST['name_en'], $_POST['name_ar'], $_POST['name_fr'], $_POST['name_es'], $_POST['icon_url']]);
    header("Location: categories.php");
    exit();
}

if (isset($_GET['delete'])) {
    $stmt = $conn->prepare("DELETE FROM categories WHERE id = ?");
    $stmt->execute([$_GET['delete']]);
    header("Location: categories.php");
    exit();
}

$categories = $conn->query("SELECT * FROM categories")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Property Categories</h3>
    <button class="btn btn-primary fw-bold px-4" data-bs-toggle="modal" data-bs-target="#addModal"><i class="fas fa-plus me-1"></i> Add Category</button>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Icon URL</th>
                        <th>Name (EN)</th>
                        <th>Name (AR)</th>
                        <th class="pe-4 text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($categories as $cat): ?>
                    <tr>
                        <td class="ps-4 text-muted small"><a href="<?= htmlspecialchars($cat['icon_url'] ?? '#') ?>" target="_blank" class="text-truncate d-inline-block" style="max-width: 150px;"><?= htmlspecialchars($cat['icon_url'] ?? 'No Icon') ?></a></td>
                        <td class="fw-bold"><?= htmlspecialchars($cat['name_en']) ?></td>
                        <td><?= htmlspecialchars($cat['name_ar']) ?></td>
                        <td class="pe-4 text-end">
                            <a href="?delete=<?= $cat['id'] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete this category?')"><i class="fas fa-trash"></i></a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add Modal -->
<div class="modal fade" id="addModal" tabindex="-1">
    <div class="modal-dialog">
        <form method="POST" class="modal-content shadow border-0">
            <div class="modal-header bg-light">
                <h5 class="modal-title fw-bold">Add Property Category</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="add">
                <div class="mb-3"><label class="form-label">English Name</label><input type="text" name="name_en" class="form-control" placeholder="Riad" required></div>
                <div class="mb-3"><label class="form-label">Arabic Name</label><input type="text" name="name_ar" class="form-control" placeholder="رياض" required></div>
                <div class="mb-3"><label class="form-label">French Name</label><input type="text" name="name_fr" class="form-control" placeholder="Riad" required></div>
                <div class="mb-3"><label class="form-label">Spanish Name</label><input type="text" name="name_es" class="form-control" placeholder="Riad" required></div>
                <div class="mb-3"><label class="form-label">Icon URL</label><input type="url" name="icon_url" class="form-control" placeholder="https://..." required></div>
            </div>
            <div class="modal-footer bg-light">
                <button type="submit" class="btn btn-primary px-4 fw-bold">Add Category</button>
            </div>
        </form>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
