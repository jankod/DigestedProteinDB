    <?php
    const HEAD_TITLE = "Digested Protein DB - Enterprise";
    include_once 'lib.php';
    ?>

    <!doctype html>
    <html lang="en">
    <head>
        <?php include_once "inc/html_head.php"; ?>
         <style>
        :root {
          --dpdb-bg: #0b1220;
          --dpdb-card: rgba(255,255,255,0.06);
          --dpdb-border: rgba(255,255,255,0.12);
        }
        body { background: #0a0f1a; }
        .hero {
          background: radial-gradient(1200px 600px at 20% 10%, rgba(13,110,253,0.28), transparent 60%),
                      radial-gradient(900px 500px at 80% 30%, rgba(25,135,84,0.18), transparent 55%),
                      linear-gradient(180deg, var(--dpdb-bg), #070b12 65%);
          border-bottom: 1px solid rgba(255,255,255,0.08);
        }
        .glass {
          background: var(--dpdb-card);
          border: 1px solid var(--dpdb-border);
          border-radius: 1rem;
          backdrop-filter: blur(10px);
        }
        .text-muted-2 { color: rgba(255,255,255,0.70) !important; }
        .badge-soft {
          background: rgba(13,110,253,0.18);
          border: 1px solid rgba(13,110,253,0.35);
          color: #cfe2ff;
        }
        .badge-soft-green {
          background: rgba(25,135,84,0.18);
          border: 1px solid rgba(25,135,84,0.35);
          color: #d1e7dd;
        }
        .section-title { letter-spacing: .2px; }
        .codeblock {
          background: rgba(0,0,0,0.35);
          border: 1px solid rgba(255,255,255,0.10);
          border-radius: .75rem;
          padding: 1rem;
          overflow-x: auto;
          font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
          font-size: .9rem;
          color: #e9ecef;
        }
        .nav-link, .navbar-brand { color: rgba(255,255,255,0.85) !important; }
        .nav-link:hover { color: #fff !important; }
        .footer {
          border-top: 1px solid rgba(255,255,255,0.08);
          color: rgba(255,255,255,0.65);
        }
      </style>
    </head>
    <body class="text-light">

    <?php include_once "inc/html_navbar.php"; ?>


    <!-- HERO -->
    <header class="hero py-5">
      <div class="container py-4">
        <div class="row align-items-center g-4">
          <div class="col-lg-7">
            <div class="d-flex align-items-center gap-2 mb-3">
              <span class="badge badge-soft rounded-pill px-3 py-2"><i class="bi bi-lightning-charge-fill me-1"></i>Rust Engine</span>
              <span class="badge badge-soft-green rounded-pill px-3 py-2"><i class="bi bi-hdd-network-fill me-1"></i>Out-of-core</span>
            </div>

            <h1 class="display-5 fw-bold mb-3">
              DigestedProteinDB Engine
              <span class="d-block fs-4 fw-semibold text-muted-2 mt-2">Ultra-fast peptide mass-range search at UniProt scale</span>
            </h1>

            <p class="lead text-muted-2 mb-4">
              A high-performance command-line tool and embeddable index engine for proteomics and metaproteomics.
              Build peptide databases for your organism(s), enzyme(s) and parameters — then query precursor mass ranges in milliseconds,
              even on commodity hardware.
            </p>

            <div class="d-flex flex-wrap gap-2">
              <a href="#contact" class="btn btn-primary btn-lg">
                <i class="bi bi-envelope-at-fill me-2"></i>Contact for Enterprise / Collaboration
              </a>
              <a href="#downloads" class="btn btn-outline-light btn-lg">
                <i class="bi bi-download me-2"></i>Community Edition
              </a>
            </div>

            <div class="mt-4 small text-muted-2">
              Designed for MS workflows: fast candidate filtering by precursor mass, scalable virtual digests, reproducible indexes.
            </div>
          </div>

          <div class="col-lg-5">
            <div class="glass p-4">
              <h5 class="fw-semibold mb-3"><i class="bi bi-graph-up-arrow me-2"></i>Performance snapshot</h5>

              <div class="row g-3">
                <div class="col-6">
                  <div class="glass p-3 h-100">
                    <div class="small text-muted-2">Typical query time</div>
                    <div class="fs-4 fw-bold">~2.4 ms</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="glass p-3 h-100">
                    <div class="small text-muted-2">Peptides found (example)</div>
                    <div class="fs-4 fw-bold">548</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="glass p-3 h-100">
                    <div class="small text-muted-2">Peak memory</div>
                    <div class="fs-4 fw-bold">~316 MB</div>
                  </div>
                </div>
                <div class="col-6">
                  <div class="glass p-3 h-100">
                    <div class="small text-muted-2">DB size (UniProt TrEMBL)</div>
                    <div class="fs-4 fw-bold">~380 GB</div>
                  </div>
                </div>
              </div>

              <div class="mt-3 small text-muted-2">
                Example config: trypsin, missed cleavages=2, peptide length 6–50 aa, mass window 2300–2300.0003.
              </div>

              <div class="mt-3 codeblock">
                <div class="text-muted-2 mb-2">Example command</div>
                <code>./qq 2300 2300.0003 --db-path rocksdb_peptides_4_db --tree --verbose</code>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>


    <!-- CONTENT -->
    <main class="py-5">
      <div class="container">

        <!-- WHY / PROBLEM -->
        <section class="mb-5">
          <div class="row g-4">
            <div class="col-lg-6">
              <h2 class="h3 fw-bold section-title">Why this exists</h2>
              <p class="text-muted-2">
                After in-silico digestion, large protein collections (e.g., UniProt TrEMBL) expand dramatically.
                Traditional in-memory approaches or general-purpose databases often become slow or resource-heavy for
                repeated <strong>mass-range candidate retrieval</strong>.
              </p>
              <p class="text-muted-2 mb-0">
                DigestedProteinDB Engine focuses on one thing: <strong>fast, scalable peptide retrieval by precursor mass</strong>,
                with predictable memory usage and databases that can be built for specific organisms, enzymes, and parameter sets.
              </p>
            </div>

            <div class="col-lg-6">
              <div class="glass p-4 h-100">
                <h5 class="fw-semibold mb-3"><i class="bi bi-check2-circle me-2"></i>What you get</h5>
                <ul class="mb-0 text-muted-2">
                  <li class="mb-2">Out-of-core peptide index (RocksDB-backed)</li>
                  <li class="mb-2">Millisecond mass-range queries on very large databases</li>
                  <li class="mb-2">Configurable digestion (enzyme, missed cleavages, peptide length, etc.)</li>
                  <li class="mb-2">CLI tool for pipelines and HPC environments</li>
                  <li class="mb-0">Embeddable engine for custom tools and services</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- FEATURES -->
        <section class="mb-5">
          <div class="d-flex align-items-end justify-content-between flex-wrap gap-2 mb-3">
            <h2 class="h3 fw-bold section-title mb-0">Core capabilities</h2>
            <div class="text-muted-2 small">Built for proteomics & metaproteomics search spaces</div>
          </div>

          <div class="row g-3">
            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-lightning-charge-fill"></i>
                  <h5 class="mb-0 fw-semibold">Fast mass-range search</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  Retrieve candidate peptides within a narrow precursor mass window in milliseconds,
                  enabling interactive exploration and high-throughput pipeline steps.
                </p>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-diagram-3-fill"></i>
                  <h5 class="mb-0 fw-semibold">Custom database builds</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  Build tailored peptide indexes per organism, taxonomy, enzyme, and settings to reduce noise,
                  control database inflation, and match experimental designs.
                </p>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-hdd-stack-fill"></i>
                  <h5 class="mb-0 fw-semibold">Out-of-core scalability</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  No need to load the entire digest into RAM. Suitable for large UniProt-scale builds
                  on commodity servers and SSDs.
                </p>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-terminal-fill"></i>
                  <h5 class="mb-0 fw-semibold">CLI-first</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  Designed for reproducible workflows: scriptable, automatable, pipeline-friendly.
                  Ideal for HPC batch jobs and integration in existing MS pipelines.
                </p>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-plug-fill"></i>
                  <h5 class="mb-0 fw-semibold">Embeddable engine</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  Use as a backend component inside your software. The engine can power web services,
                  lab tools, or internal search infrastructure.
                </p>
              </div>
            </div>

            <div class="col-md-6 col-lg-4">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <i class="bi bi-shield-lock-fill"></i>
                  <h5 class="mb-0 fw-semibold">Deterministic & reproducible</h5>
                </div>
                <p class="text-muted-2 mb-0">
                  Parameterized builds make it easy to track exact digestion settings and database provenance
                  for reproducible research and regulated environments.
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- USE CASES -->
        <section class="mb-5">
          <h2 class="h3 fw-bold section-title mb-3">Typical use cases</h2>
          <div class="row g-3">
            <div class="col-lg-8">
              <div class="glass p-4 h-100">
                <ul class="mb-0 text-muted-2">
                  <li class="mb-2"><strong>Candidate filtering</strong> by precursor mass range prior to downstream scoring</li>
                  <li class="mb-2"><strong>Metaproteomics</strong>: large search spaces, shared peptides, taxonomy-focused builds</li>
                  <li class="mb-2"><strong>DIA/DDA workflows</strong>: rapid peptide candidate lookup for experimental designs</li>
                  <li class="mb-2"><strong>Core facilities</strong>: standard prebuilt indexes per organism and enzyme</li>
                  <li class="mb-0"><strong>Infrastructure</strong>: embed as a backend peptide index in custom software</li>
                </ul>
              </div>
            </div>
            <div class="col-lg-4">
              <div class="glass p-4 h-100">
                <h5 class="fw-semibold mb-3"><i class="bi bi-sliders2-vertical me-2"></i>Configurable parameters</h5>
                <ul class="mb-0 text-muted-2 small">
                  <li class="mb-2">Enzyme (e.g., trypsin, chymotrypsin, custom rules)</li>
                  <li class="mb-2">Missed cleavages</li>
                  <li class="mb-2">Peptide length bounds</li>
                  <li class="mb-2">Target organisms / taxonomies</li>
                  <li class="mb-0">Build-time filtering options</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- COMMUNITY vs ENTERPRISE -->
        <section class="mb-5" id="downloads">
          <div class="d-flex align-items-end justify-content-between flex-wrap gap-2 mb-3">
            <h2 class="h3 fw-bold section-title mb-0">Community vs Enterprise</h2>
            <div class="text-muted-2 small">Start free, scale when you need production support</div>
          </div>

          <div class="row g-3">
            <div class="col-lg-6">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center justify-content-between">
                  <h4 class="fw-bold mb-0">Community Edition</h4>
                  <span class="badge bg-success-subtle text-success border border-success-subtle">Free</span>
                </div>
                <p class="text-muted-2 mt-2">
                  Ideal for evaluation, academic research, and pipeline prototyping.
                </p>
                <ul class="text-muted-2">
                  <li>CLI binary</li>
                  <li>Basic documentation</li>
                  <li>Example database(s)</li>
                  <li>Non-commercial / academic usage</li>
                </ul>
                <a class="btn btn-outline-light" href="/downloads.php">
                  <i class="bi bi-download me-2"></i>Go to Downloads
                </a>
              </div>
            </div>

            <div class="col-lg-6">
              <div class="glass p-4 h-100">
                <div class="d-flex align-items-center justify-content-between">
                  <h4 class="fw-bold mb-0">Enterprise / Collaboration</h4>
                  <span class="badge bg-primary-subtle text-primary border border-primary-subtle">Paid</span>
                </div>
                <p class="text-muted-2 mt-2">
                  For core facilities, industry users, and teams needing tailored databases, integration, and support.
                </p>
                <ul class="text-muted-2">
                  <li>Custom database generation (organism/taxonomy-specific)</li>
                  <li>Optimized builds for your enzymes and parameters</li>
                  <li>Pipeline integration & performance tuning</li>
                  <li>Embeddable/production deployment guidance</li>
                  <li>Priority support and collaboration</li>
                </ul>
                <a class="btn btn-primary" href="#contact">
                  <i class="bi bi-envelope-at-fill me-2"></i>Contact
                </a>
              </div>
            </div>
          </div>
        </section>

        <!-- EMBEDDING -->
        <section class="mb-5">
          <div class="glass p-4">
            <div class="row g-4 align-items-center">
              <div class="col-lg-8">
                <h2 class="h4 fw-bold section-title mb-2"><i class="bi bi-box-arrow-in-right me-2"></i>Embedding / Backend integration</h2>
                <p class="text-muted-2 mb-0">
                  The engine can be used as a backend peptide index inside your own applications.
                  Common patterns include running it as a fast CLI microservice, wrapping it with a lightweight API,
                  or embedding it into an internal proteomics pipeline for rapid candidate retrieval.
                </p>
              </div>
              <div class="col-lg-4">
                <div class="d-grid gap-2">
                  <a class="btn btn-outline-light" href="/api.php"><i class="bi bi-braces me-2"></i>REST API</a>
                  <a class="btn btn-primary" href="#contact"><i class="bi bi-chat-dots-fill me-2"></i>Discuss integration</a>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- BENCHMARK DETAILS (optional but credibility boost) -->
        <section class="mb-5">
          <h2 class="h3 fw-bold section-title mb-3">Benchmark excerpt</h2>
          <div class="row g-3">
            <div class="col-lg-7">
              <div class="codeblock">
                <div class="text-muted-2 mb-2">Output (example)</div>
                <pre class="mb-0"><code>Found 548 peptides in 2.44999ms
    Command being timed: "./qq 2300 2300.0003 --db-path rocksdb_peptides_4_db --tree --verbose"
    User time (seconds): 0.09
    System time (seconds): 0.18
    Percent of CPU this job got: 253%
    Elapsed (wall clock) time: 0:00.11
    Maximum resident set size (kbytes): 315760
    Major page faults: 0
    Swaps: 0</code></pre>
              </div>
            </div>
            <div class="col-lg-5">
              <div class="glass p-4 h-100">
                <h5 class="fw-semibold mb-3"><i class="bi bi-info-circle-fill me-2"></i>Notes</h5>
                <p class="text-muted-2 mb-2">
                  This excerpt is shown to communicate typical performance characteristics on commodity SSD hardware.
                  Real-world performance depends on storage, CPU, and database configuration.
                </p>
                <ul class="text-muted-2 small mb-0">
                  <li>Dataset: UniProt TrEMBL digest (~380 GB)</li>
                  <li>Trypsin, missed cleavages = 2</li>
                  <li>Peptide length bounds: 6–50 aa</li>
                  <li>Query window: very narrow (high selectivity)</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- CONTACT -->
        <section id="contact" class="mb-4">
          <div class="glass p-4 p-lg-5">
            <div class="row g-4 align-items-center">
              <div class="col-lg-8">
                <h2 class="h3 fw-bold section-title mb-2">Enterprise / Collaboration</h2>
                <p class="text-muted-2 mb-0">
                  Want a tailored peptide index (organism/taxonomy-specific), help integrating into your MS pipeline,
                  or a production-ready deployment? Get in touch and describe your dataset size, enzyme, and use case.
                </p>
              </div>
              <div class="col-lg-4">
                <div class="d-grid gap-2">
                  <!-- Replace with your email -->
                  <a class="btn btn-primary btn-lg" href="mailto:YOUR.EMAIL@domain.tld?subject=DigestedProteinDB%20Engine%20-%20Enterprise%20Inquiry">
                    <i class="bi bi-envelope-at-fill me-2"></i>Email me
                  </a>
                  <!-- Optional: link to a contact form -->
                  <a class="btn btn-outline-light btn-lg" href="/about.php">
                    <i class="bi bi-person-lines-fill me-2"></i>About / Contact
                  </a>
                </div>
              </div>
            </div>

            <hr class="border border-white border-opacity-10 my-4">

            <div class="row g-3">
              <div class="col-md-6 col-lg-3">
                <div class="glass p-3 h-100">
                  <div class="small text-muted-2">Offer</div>
                  <div class="fw-semibold">Custom DB builds</div>
                </div>
              </div>
              <div class="col-md-6 col-lg-3">
                <div class="glass p-3 h-100">
                  <div class="small text-muted-2">Offer</div>
                  <div class="fw-semibold">Pipeline integration</div>
                </div>
              </div>
              <div class="col-md-6 col-lg-3">
                <div class="glass p-3 h-100">
                  <div class="small text-muted-2">Offer</div>
                  <div class="fw-semibold">Performance tuning</div>
                </div>
              </div>
              <div class="col-md-6 col-lg-3">
                <div class="glass p-3 h-100">
                  <div class="small text-muted-2">Offer</div>
                  <div class="fw-semibold">Commercial licensing</div>
                </div>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>

    <!-- FOOTER -->
    <footer class="footer py-4">
      <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div class="small">
          © <span id="year"></span> DigestedProteinDB • Engine (Rust)
        </div>
        <div class="small">
          <a class="link-light link-underline-opacity-0 link-underline-opacity-75-hover" href="/about.php">About</a>
          <span class="mx-2">•</span>
          <a class="link-light link-underline-opacity-0 link-underline-opacity-75-hover" href="/downloads.php">Downloads</a>
        </div>
      </div>
    </footer>

    <script>
      document.getElementById('year').textContent = new Date().getFullYear();
    </script>


    <?php include_once "inc/html_footer.php"; ?>

    <script src="bootstrap.bundle.min.js"></script>
    <script src="bio-lib.js" defer></script>

    </body>
    </html>
