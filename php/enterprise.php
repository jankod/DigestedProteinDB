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


<div class="container mt-4 mb-5">

  <!-- HEADER -->
  <div class="row mb-4">
    <div class="col">
      <h2>DigestedProteinDB Engine</h2>
      <p class="text-muted mb-0">
        Enterprise-grade high-performance peptide index and search engine
      </p>
      <hr>
    </div>
  </div>

  <!-- IMPORTANT NOTE -->
  <div class="alert alert-primary">
    <strong>Note:</strong> This is the optimized Rust-based enterprise version of DigestedProteinDB.
    It is separate from the Java open-source research implementation and is designed for
    high-performance production workflows, embedding, and custom deployments.
  </div>

  <!-- OVERVIEW -->
  <div class="row mb-4">
    <div class="col-lg-7">
      <h4>Overview</h4>
      <p>
        The <strong>DigestedProteinDB Engine (Rust)</strong> is a high-performance command-line
        peptide indexing and search tool designed for large-scale proteomics and metaproteomics.
        It enables millisecond-level peptide mass range queries even on very large databases.
      </p>

      <p>
        Unlike the original Java research version, this engine is optimized for:
      </p>

      <ul>
        <li>production environments</li>
        <li>low memory footprint</li>
        <li>high-throughput pipelines</li>
        <li>easy embedding into external software</li>
        <li>custom enterprise deployments</li>
      </ul>
    </div>

    <div class="col-lg-5">
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">Performance snapshot</h5>

          <table class="table table-sm mb-0">
            <tr>
              <td><strong>Database</strong></td>
              <td>UniProt TrEMBL digest</td>
            </tr>
            <tr>
              <td><strong>Size</strong></td>
              <td>~380 GB</td>
            </tr>
            <tr>
              <td><strong>Typical query</strong></td>
              <td>~2–3 ms</td>
            </tr>
            <tr>
              <td><strong>Peak memory</strong></td>
              <td>~316 MB</td>
            </tr>
            <tr>
              <td><strong>Enzyme</strong></td>
              <td>Trypsin (MC=2)</td>
            </tr>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- KEY FEATURES -->
  <div class="row mb-4">
    <div class="col">
      <h4>Key capabilities</h4>
      <div class="row">

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">Ultra-fast mass search</h6>
              <p class="card-text small">
                Retrieve candidate peptides within narrow precursor mass windows
                in milliseconds, even for very large databases.
              </p>
            </div>
          </div>
        </div>

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">Custom database generation</h6>
              <p class="card-text small">
                Build tailored peptide indexes for specific organisms,
                taxonomies, enzymes, and digestion parameters.
              </p>
            </div>
          </div>
        </div>

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">Out-of-core design</h6>
              <p class="card-text small">
                No need to load the full digest into memory. Works efficiently
                with very large UniProt-scale datasets.
              </p>
            </div>
          </div>
        </div>

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">CLI-first architecture</h6>
              <p class="card-text small">
                Scriptable and pipeline-friendly command-line interface suitable
                for HPC and automated workflows.
              </p>
            </div>
          </div>
        </div>

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">Embeddable engine</h6>
              <p class="card-text small">
                Designed to be integrated into external software, services,
                or internal proteomics pipelines.
              </p>
            </div>
          </div>
        </div>

        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              <h6 class="card-title">Enterprise customization</h6>
              <p class="card-text small">
                Supports custom builds, organism-specific databases,
                and performance tuning for production environments.
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>

  <!-- COMMUNITY VS ENTERPRISE -->
  <div class="row mb-4">
    <div class="col-lg-6 mb-3">
      <div class="card h-100 border-success">
        <div class="card-body">
          <h5 class="card-title text-success">Community / Research</h5>
          <ul class="mb-0">
            <li>Java open-source implementation</li>
            <li>Academic and research use</li>
            <li>Basic digestion and search</li>
            <li>Evaluation and prototyping</li>
          </ul>
        </div>
      </div>
    </div>

    <div class="col-lg-6 mb-3">
      <div class="card h-100 border-primary">
        <div class="card-body">
          <h5 class="card-title text-primary">Enterprise Engine (Rust)</h5>
          <ul class="mb-0">
            <li>High-performance optimized core</li>
            <li>Low-overhead CLI execution</li>
            <li>Designed for embedding</li>
            <li>Custom database builds</li>
            <li>Production and HPC ready</li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <!-- USE CASES -->
  <div class="row mb-4">
    <div class="col">
      <h4>Typical use cases</h4>
      <ul>
        <li>large-scale proteomics database preparation</li>
        <li>metaproteomics search space reduction</li>
        <li>DIA/DDA candidate filtering by precursor mass</li>
        <li>core facility peptide indexing infrastructure</li>
        <li>backend engine for custom proteomics software</li>
      </ul>
    </div>
  </div>

  <!-- CONTACT -->
  <div class="row">
    <div class="col">
      <div class="alert alert-secondary">
        <h5>Enterprise access and collaboration</h5>
        <p class="mb-2">
          For custom database builds, pipeline integration, embedding support,
          or commercial licensing, please get in touch.
        </p>
        <a href="mailto:jdiminic@pbf.hr" class="btn btn-primary">
          Contact
        </a>
      </div>
    </div>
  </div>

</div>




    <?php include_once "inc/html_footer.php"; ?>

    <script src="bootstrap.bundle.min.js"></script>
    <script src="bio-lib.js" defer></script>

    </body>
    </html>
