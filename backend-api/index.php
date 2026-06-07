<?php
// Secure landing page for Zellige Stays Backend Root
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zellige Stays - Authentic Moroccan Experience</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        :root {
            --primary-blue: #0A2540;
            --primary-gold: #D4AF37;
            --white: #FFFFFF;
        }
        body {
            font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            color: var(--primary-blue);
            background-color: #f8f9fa;
        }
        .hero-section {
            background-color: var(--primary-blue);
            color: var(--white);
            padding: 100px 0;
            background-image: linear-gradient(rgba(10, 37, 64, 0.9), rgba(10, 37, 64, 0.9)), url('https://images.unsplash.com/photo-1539020140153-e479b8c22e70?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80');
            background-size: cover;
            background-position: center;
        }
        .navbar {
            background-color: var(--primary-blue);
            padding: 20px 0;
        }
        .navbar-brand {
            color: var(--primary-gold) !important;
            font-weight: bold;
            font-size: 28px;
        }
        .navbar-nav .nav-link {
            color: var(--white) !important;
            font-weight: 500;
        }
        .navbar-nav .nav-link:hover {
            color: var(--primary-gold) !important;
        }
        .btn-gold {
            background-color: var(--primary-gold);
            color: var(--white);
            border: none;
            font-weight: bold;
            padding: 10px 24px;
            border-radius: 8px;
            transition: all 0.3s;
        }
        .btn-gold:hover {
            background-color: #bfa030;
            color: var(--white);
        }
        .btn-outline-light {
            border-color: var(--white);
            padding: 10px 24px;
            border-radius: 8px;
            transition: all 0.3s;
        }
        .feature-card {
            background: var(--white);
            border-radius: 12px;
            padding: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
            height: 100%;
            transition: transform 0.3s;
            border-top: 4px solid var(--primary-gold);
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
        .feature-icon {
            color: var(--primary-gold);
            font-size: 32px;
            margin-bottom: 20px;
        }
        .stats-section {
            background-color: var(--primary-blue);
            color: var(--white);
            padding: 60px 0;
        }
        .stat-item h3 {
            color: var(--primary-gold);
            font-size: 48px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .footer {
            background-color: var(--primary-blue);
            color: var(--white);
            padding: 40px 0 20px;
            border-top: 1px solid rgba(255,255,255,0.1);
        }
        .footer-link {
            color: #ccc;
            text-decoration: none;
            transition: color 0.3s;
        }
        .footer-link:hover {
            color: var(--primary-gold);
        }
        .social-icons a {
            color: var(--white);
            font-size: 20px;
            margin-right: 15px;
            transition: color 0.3s;
        }
        .social-icons a:hover {
            color: var(--primary-gold);
        }
    </style>
</head>
<body>

    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg sticky-top">
        <div class="container">
            <a class="navbar-brand" href="/">
                <i class="fas fa-mosque me-2"></i>Zellige Stays
            </a>
            <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="fas fa-bars text-white"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto align-items-center">
                    <li class="nav-item"><a class="nav-link" href="#features">Features</a></li>
                    <li class="nav-item"><a class="nav-link" href="#stats">Impact</a></li>
                    <li class="nav-item"><a class="nav-link" href="#contact">Contact</a></li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section text-center">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <h1 class="display-4 fw-bold mb-4">Zellige Stays</h1>
                    <h2 class="h3 mb-4 font-weight-normal" style="color: var(--primary-gold);">Authentic Moroccan Experience</h2>
                    <p class="lead mb-5">
                        Zellige Stays is a modern tourism platform connecting travelers, property owners, and tour guides across Morocco.
                    </p>
                    <div class="d-flex justify-content-center gap-3">
                        <a href="#features" class="btn btn-gold btn-lg shadow-sm">Explore Platform</a>
                        <a href="#contact" class="btn btn-outline-light btn-lg">Contact Us</a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="py-5">
        <div class="container py-4">
            <div class="text-center mb-5">
                <h2 class="fw-bold" style="color: var(--primary-blue);">Platform Features</h2>
                <div style="height: 3px; width: 60px; background-color: var(--primary-gold); margin: 15px auto;"></div>
            </div>
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-bed"></i></div>
                        <h4 class="fw-bold mb-3">Accommodation Booking</h4>
                        <p class="text-muted">Discover and book unique Riads, Villas, and stays across Morocco.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-route"></i></div>
                        <h4 class="fw-bold mb-3">Tour Guides</h4>
                        <p class="text-muted">Connect with local experts to experience the authentic culture and history.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-shield-alt"></i></div>
                        <h4 class="fw-bold mb-3">Secure Payments</h4>
                        <p class="text-muted">Reliable and encrypted transaction processing for peace of mind.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-comments"></i></div>
                        <h4 class="fw-bold mb-3">Messaging System</h4>
                        <p class="text-muted">Direct communication between travelers, hosts, and guides.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-star"></i></div>
                        <h4 class="fw-bold mb-3">Verified Reviews</h4>
                        <p class="text-muted">Authentic experiences shared by our community of global travelers.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon"><i class="fas fa-mobile-alt"></i></div>
                        <h4 class="fw-bold mb-3">Mobile Application</h4>
                        <p class="text-muted">A fully functional mobile application, supporting multiple languages.</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Stats Section -->
    <section id="stats" class="stats-section text-center">
        <div class="container">
            <div class="row g-4">
                <div class="col-6 col-md-3 stat-item">
                    <h3>20+</h3>
                    <p class="mb-0 text-uppercase fw-bold" style="letter-spacing: 1px;">Cities</p>
                </div>
                <div class="col-6 col-md-3 stat-item">
                    <h3>1,500+</h3>
                    <p class="mb-0 text-uppercase fw-bold" style="letter-spacing: 1px;">Properties</p>
                </div>
                <div class="col-6 col-md-3 stat-item">
                    <h3>300+</h3>
                    <p class="mb-0 text-uppercase fw-bold" style="letter-spacing: 1px;">Guides</p>
                </div>
                <div class="col-6 col-md-3 stat-item">
                    <h3>50k+</h3>
                    <p class="mb-0 text-uppercase fw-bold" style="letter-spacing: 1px;">Reservations</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Contact & Footer -->
    <footer id="contact" class="footer pt-5">
        <div class="container">
            <div class="row mb-4">
                <div class="col-lg-4 mb-4 mb-lg-0">
                    <h4 class="mb-3" style="color: var(--primary-gold);"><i class="fas fa-mosque me-2"></i>Zellige Stays</h4>
                    <p class="text-muted" style="color: #ccc !important;">Connecting the world to the heart of Morocco with secure, authentic, and memorable experiences.</p>
                </div>
                <div class="col-lg-4 mb-4 mb-lg-0">
                    <h5 class="mb-3">Contact Us</h5>
                    <ul class="list-unstyled">
                        <li class="mb-2"><i class="fas fa-envelope me-2" style="color: var(--primary-gold);"></i> support@zelligestays.com</li>
                        <li class="mb-2"><i class="fab fa-whatsapp me-2" style="color: var(--primary-gold);"></i> +212 600 000 000</li>
                    </ul>
                    <div class="social-icons mt-3">
                        <a href="#"><i class="fab fa-facebook-f"></i></a>
                        <a href="#"><i class="fab fa-instagram"></i></a>
                        <a href="#"><i class="fab fa-twitter"></i></a>
                        <a href="#"><i class="fab fa-linkedin-in"></i></a>
                    </div>
                </div>
                <div class="col-lg-4">
                    <h5 class="mb-3">Legal</h5>
                    <ul class="list-unstyled">
                        <li class="mb-2"><a href="#" class="footer-link">Privacy Policy</a></li>
                        <li class="mb-2"><a href="#" class="footer-link">Terms & Conditions</a></li>
                    </ul>
                </div>
            </div>
            <div class="text-center pt-4" style="border-top: 1px solid rgba(255,255,255,0.1);">
                <p class="mb-0" style="color: #888;">&copy; <?php echo date("Y"); ?> Zellige Stays Platform. All rights reserved.</p>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
