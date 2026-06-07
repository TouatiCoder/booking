<?php
session_start();
require_once '../config/database.php';

if (isset($_SESSION['admin_id'])) {
    header("Location: index.php");
    exit();
}

$error = '';
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'];
    $password = $_POST['password'];

    if ($email === 'admin@zellige.com' && $password === 'admin123') {
        $_SESSION['admin_id'] = 1;
        $_SESSION['admin_email'] = $email;
        header("Location: index.php");
        exit();
    } else {
        $stmt = $conn->prepare("SELECT id, role FROM users WHERE email = :email AND role = 'admin'");
        $stmt->execute(['email' => $email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($user) {
            $_SESSION['admin_id'] = $user['id'];
            $_SESSION['admin_email'] = $email;
            header("Location: index.php");
            exit();
        } else {
            $error = 'Invalid credentials or you do not have admin access.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Login - Zellige Stays</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center vh-100 bg-light">
    <div class="card shadow border-0 p-4" style="width: 400px; border-radius: 12px;">
        <h3 class="text-center mb-4 text-primary fw-bold">Zellige Admin</h3>
        <?php if($error): ?><div class="alert alert-danger py-2"><?= $error ?></div><?php endif; ?>
        <form method="POST">
            <div class="mb-3">
                <label class="form-label text-muted">Email Address</label>
                <input type="email" name="email" class="form-control" required value="admin@zellige.com">
            </div>
            <div class="mb-4">
                <label class="form-label text-muted">Password</label>
                <input type="password" name="password" class="form-control" required value="admin123">
            </div>
            <button type="submit" class="btn btn-primary w-100 fw-bold py-2">Sign In</button>
        </form>
    </div>
</body>
</html>
