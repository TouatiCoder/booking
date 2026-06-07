<?php
require_once 'includes/session.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action']) && $_POST['action'] == 'add') {
    $id = uniqid('city_');
    $stmt = $conn->prepare("INSERT INTO cities (id, name_en, name_ar, name_fr, name_es, image_url, is_active, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$id, $_POST['name_en'], $_POST['name_ar'], $_POST['name_fr'], $_POST['name_es'], $_POST['image_url'], isset($_POST['is_active']) ? 1 : 0, isset($_POST['is_featured']) ? 1 : 0]);
    header("Location: cities.php");
    exit();
}

if (isset($_GET['delete'])) {
    $stmt = $conn->prepare("DELETE FROM cities WHERE id = ?");
    $stmt->execute([$_GET['delete']]);
    header("Location: cities.php");
    exit();
}

$cities = $conn->query("SELECT * FROM cities")->fetchAll();

require_once 'includes/header.php';
?>
<div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="fw-bold m-0">Destinations & Cities</h3>
    <button class="btn btn-primary fw-bold px-4" data-bs-toggle="modal" data-bs-target="#addModal"><i class="fas fa-plus me-1"></i> Add Destination</button>
</div>
<div class="card shadow border-0">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle m-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Image</th>
                        <th>City Name (EN)</th>
                        <th>Name (AR)</th>
                        <th>Featured</th>
                        <th>Active</th>
                        <th class="pe-4 text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach($cities as $city): ?>
                    <tr>
                        <td class="ps-4">
                            <?php if($city['image_url']): ?>
                                <img src="<?= htmlspecialchars($city['image_url']) ?>" style="height: 45px; width: 45px; object-fit: cover; border-radius: 8px;">
                            <?php else: ?>
                                <div class="bg-light text-muted d-flex align-items-center justify-content-center" style="height: 45px; width: 45px; border-radius: 8px;"><i class="fas fa-image"></i></div>
                            <?php endif; ?>
                        </td>
                        <td class="fw-bold"><?= htmlspecialchars($city['name_en']) ?></td>
                        <td><?= htmlspecialchars($city['name_ar']) ?></td>
                        <td>
                            <?php if($city['is_featured']): ?>
                                <span class="badge bg-warning text-dark"><i class="fas fa-star"></i> Featured</span>
                            <?php else: ?>
                                -
                            <?php endif; ?>
                        </td>
                        <td>
                            <?php if($city['is_active']): ?>
                                <span class="badge bg-success">Active</span>
                            <?php else: ?>
                                <span class="badge bg-secondary">Disabled</span>
                            <?php endif; ?>
                        </td>
                        <td class="pe-4 text-end">
                            <a href="?delete=<?= $city['id'] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete this destination? It may break attached properties.')"><i class="fas fa-trash"></i></a>
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
                <h5 class="modal-title fw-bold">Add New Destination</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="add">
                <div class="mb-3"><label class="form-label">English Name</label><input type="text" name="name_en" class="form-control" placeholder="Marrakech" required></div>
                <div class="mb-3"><label class="form-label">Arabic Name</label><input type="text" name="name_ar" class="form-control" placeholder="مراكش" required></div>
                <div class="mb-3"><label class="form-label">French Name</label><input type="text" name="name_fr" class="form-control" placeholder="Marrakech" required></div>
                <div class="mb-3"><label class="form-label">Spanish Name</label><input type="text" name="name_es" class="form-control" placeholder="Marrakech" required></div>
                <div class="mb-3"><label class="form-label">Image URL</label><input type="url" name="image_url" class="form-control" placeholder="https://..." required></div>
                
                <div class="form-check form-switch mb-2">
                    <input class="form-check-input" type="checkbox" name="is_featured" id="featuredCheck" checked>
                    <label class="form-check-label" for="featuredCheck">Highlight as Featured City</label>
                </div>
                <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" name="is_active" id="activeCheck" checked>
                    <label class="form-check-label" for="activeCheck">Active (Visible)</label>
                </div>
            </div>
            <div class="modal-footer bg-light">
                <button type="submit" class="btn btn-primary px-4 fw-bold">Add City</button>
            </div>
        </form>
    </div>
</div>
<?php require_once 'includes/footer.php'; ?>
