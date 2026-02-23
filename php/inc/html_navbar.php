<nav class="navbar navbar-expand-lg bg-primary mb-3" data-bs-theme="dark">
    <div class="container">
        <a class="navbar-brand" href="index.php">Digested Protein DB</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <?php
            // detect if we are running on localhost / loopback
            $host = $_SERVER['HTTP_HOST'] ?? $_SERVER['SERVER_NAME'] ?? '';
            $remote = $_SERVER['REMOTE_ADDR'] ?? '';
            $isLocal = false;
            // HTTP_HOST can include port (e.g. localhost:8080), so use stripos for localhost
            if (stripos($host, 'localhost') !== false || strpos($host, '127.') === 0 || $remote === '127.0.0.1' || $remote === '::1') {
                $isLocal = true;
            }
            ?>
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('index.php') ?>" href="index.php"
                       aria-current="page">Search</a>
                </li>

                <?php if ($isLocal): ?>
                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('analyze_ms1.php') ?>" href="analyze_ms1.php"
                       aria-current="page">Analyze</a>
                </li>
                <?php endif; ?>



                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('rest.php') ?>" href="rest.php" aria-current="page">REST
                        API</a>
                </li>

                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('tool.php') ?>" href="tool.php" aria-current="page">Digestion Tool</a>
                </li>

                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('download_db.php') ?>" href="download_db.php"
                       aria-current="page">Downloads</a>
                </li>

                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('enterprise.php') ?>" href="enterprise.php"
                       aria-current="page">Enterprise</a>
                </li>

                <li class="nav-item">
                    <a class="nav-link <?php echo isActive('about.php') ?>" href="about.php"
                       aria-current="page">About</a>
                </li>
            </ul>
        </div>
    </div>
</nav>
