<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - Zellige Stays</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        .sidebar { min-height: 100vh; background: #2c3e50; }
        .sidebar a { color: #ecf0f1; text-decoration: none; padding: 12px 20px; display: block; font-weight: 500; transition: 0.2s; }
        .sidebar a:hover { background: #34495e; padding-left: 25px; }
        .sidebar .active { background: #3498db; color: #fff; }
        .sidebar .brand { padding: 20px; font-size: 1.5rem; font-weight: bold; background: #1a252f; color: white; text-align: center; }
        body { background: #f4f6f9; }
        .content-area { padding: 30px; }
    </style>
</head>
<body>
<div class="d-flex">
    <!-- Sidebar -->
    <div class="sidebar shadow" style="width: 260px; flex-shrink: 0;">
        <div class="brand"><i class="fas fa-mosque me-2"></i> Zellige Stays</div>
        <div class="mt-3">
            <a href="index.php"><i class="fas fa-home me-2"></i> Dashboard</a>
            <a href="users.php"><i class="fas fa-users me-2"></i> Travelers</a>
            <a href="hosts.php"><i class="fas fa-house-user me-2"></i> Hosts</a>
            <a href="guides.php"><i class="fas fa-route me-2"></i> Guides</a>
            <a href="properties.php"><i class="fas fa-building me-2"></i> Properties</a>
            <a href="messages.php"><i class="fas fa-comments me-2"></i> Messages</a>
            <a href="cities.php"><i class="fas fa-city me-2"></i> Cities</a>
            <a href="categories.php"><i class="fas fa-tags me-2"></i> Categories</a>
            <a href="reservations.php"><i class="fas fa-calendar-check me-2"></i> Reservations</a>
            <a href="payments.php"><i class="fas fa-money-bill-wave me-2"></i> Payments</a>
            <a href="reviews.php"><i class="fas fa-star me-2"></i> Reviews</a>
            <a href="subscriptions.php"><i class="fas fa-credit-card me-2"></i> Subscriptions</a>
            <a href="notifications.php"><i class="fas fa-bell me-2"></i> Notifications</a>
            <a href="settings.php"><i class="fas fa-cog me-2"></i> Settings</a>
            <a href="branding.php"><i class="fas fa-paint-brush me-2"></i> Branding</a>
            <a href="logout.php" class="text-danger mt-4 border-top border-secondary"><i class="fas fa-sign-out-alt me-2"></i> Logout</a>
        </div>
    </div>
    <!-- Content -->
    <div class="flex-grow-1">
        <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4 py-3">
            <span class="navbar-brand mb-0 h5 fw-bold text-secondary">Control Panel</span>
            <div class="ms-auto text-muted">Signed in as <b><?= htmlspecialchars($_SESSION['admin_email']) ?></b></div>
        </nav>
        <div class="content-area">
