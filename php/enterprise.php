    <?php
    const HEAD_TITLE = "Digested Protein DB - Enterprise";
    include_once 'lib.php';
    ?>

    <!doctype html>
    <html lang="en">
    <head>
        <?php include_once "inc/html_head.php"; ?>
         <style>

      </style>
    </head>
    <body >

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

<div class="my-4 p-4 border-start border-4 border-primary bg-light shadow-sm rounded-end">
    <div class="d-flex align-items-center mb-2">
        <i class="bi bi-info-square-fill text-primary me-2 fs-4"></i>
        <h5 class="mb-0 fw-bold text-dark">Production Engine Distinction</h5>
    </div>
    <p class="mb-0 text-muted" style="font-size: 1.05rem;">
        The <strong>Enterprise Engine (Rust)</strong> is an ultra-optimized implementation of DigestedProteinDB.
        While the <span class="text-primary fw-semibold">Java version</span> serves as the open-source research reference,
        this engine is specifically engineered for high-throughput production, sub-millisecond mass-range queries,
        and seamless embedding into high-performance computing (HPC) pipelines.
    </p>
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




<div class="row g-4 mt-5">
    <div class="col-md-6">
        <h4 class="fw-bold"><i class="bi bi-gear-wide-connected text-primary"></i> Production-Ready</h4>
        <p class="text-muted small">Optimized for stability and consistent 2-3ms latency using RocksDB's Log-Structured Merge Tree architecture.</p>
    </div>
    <div class="col-md-6">
        <h4 class="fw-bold"><i class="bi bi-memory text-primary"></i> Minimal Resource Usage</h4>
        <p class="text-muted small">Search billions of entries with peak RAM under 500 MB, thanks to 5-bit sequence encoding and varint serialization.</p>
    </div>
    <div class="col-md-6">
        <h4 class="fw-bold"><i class="bi bi-diagram-3 text-primary"></i> Custom Taxonomy & Enzymes</h4>
        <p class="text-muted small">We build tailored indices for specific TaxIDs, enzymes (Trypsin, Pepsin, etc.), and custom digestion rules (missed cleavages, length).</p>
    </div>
    <div class="col-md-6">
        <h4 class="fw-bold"><i class="bi bi-box-seam text-primary"></i> Seamless Embedding</h4>
        <p class="text-muted small">Zero-dependency Rust core. Perfect for integration into HPC pipelines or as a backend engine for third-party proteomics software.</p>
    </div>
</div>
    </div>

    <div class="col-lg-5">
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">Performance snapshot</h5>

<div class="card shadow-sm">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Enterprise Engine Specification (Rust)</h5>
        <span class="badge bg-success">High Performance</span>
    </div>
    <div class="card-body p-0">
        <table class="table table-hover table-striped mb-0">
            <tbody>
                <tr>
                    <td class="fw-bold" style="width: 35%;">Database Version</td>
                    <td>UniProtKB/TrEMBL (Release 2026_01)</td>
                </tr>
                <tr>
                    <td class="fw-bold">Taxonomy & Scope</td>
                    <td><span class="text-primary fw-bold">All Organisms (Proteome-wide)</span> - Full global protein index</td>
                </tr>
                <tr>
                    <td class="fw-bold">Scale</td>
                    <td>202.55 million proteins / 17.20 billion peptides</td>
                </tr>
                <tr>
                    <td class="fw-bold">Digestion Parameters</td>
                    <td>
                        <strong>Enzyme:</strong> Trypsin<br>
                        <strong>Missed Cleavages:</strong> Up to 2 allowed<br>
                        <strong>Peptide Length:</strong> 6 to 50 amino acids
                    </td>
                </tr>
                <tr>
                    <td class="fw-bold">Search Performance</td>
                    <td><mark class="px-2">~150 ms</mark> average mass-range query time</td>
                </tr>
                <tr>
                    <td class="fw-bold">Disk Footprint</td>
                    <td>~255 GB (Optimized with Snappy|Zstd compression & 5-bit encoding)</td>
                </tr>
                <tr>
                    <td class="fw-bold">Memory Usage</td>
                    <td>~316 MB peak RAM (Out-of-core indexing)</td>
                </tr>
                <tr>
                    <td class="fw-bold">Storage Engine</td>
                    <td>RocksDB Key-Value (Optimized Rust Core)</td>
                </tr>
            </tbody>
        </table>
    </div>
    <div class="card-footer bg-light text-center">
        <small class="text-muted">Designed for ultra-fast candidate retrieval in large-scale metaproteomics.</small>
    </div>
</div>

        </div>
      </div>
    </div>
  </div>







<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="card shadow-lg border-0">
                <div class="card-header bg-secondary text-white d-flex align-items-center py-2">
                    <div class="d-flex mr-3">
                        <span class="rounded-circle bg-danger mx-1" style="width: 12px; height: 12px;"></span>
                        <span class="rounded-circle bg-warning mx-1" style="width: 12px; height: 12px;"></span>
                        <span class="rounded-circle bg-success mx-1" style="width: 12px; height: 12px;"></span>
                    </div>
                    <small class="font-weight-bold ml-2">DigestedProteinDB — bash — 120x40</small>
                </div>

                <div class="card-body p-4" style="background-color: #1e1e1e; color: #d4d4d4; font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace; font-size: 0.95rem;">
                    <pre class="mb-0" style="color: inherit; border: none; padding: 0; background: transparent;">
<span style="color: #4ec9b0;">user@proteomics-hpc:~$</span> <span style="color: #ce9178;">massq 1800.0000 1800.0002 --db-path ./uniprot_trembl_db</span>

<span style="color: #6a9955;">#       Mass      Peptide             Accession   TaxID</span>
1       1800.0001
                AGASCPICKKEIQLVIK   Q2HJ21      9913
                AGASCPICKKEIQLVIK   O15151      9606
                AVARMSVLSELCLPLAK   Q1WRS8      362948
                GGKGDLCIVLNVLLMQK   Q83NI2      218496
                GGKGDLCIVLNVLLMQK   Q83MZ4      203267
                GLMPLGITDEIRKMVK    A2RMH5      416870
                GLMPLGITDEIRKMVK    Q02XB8      272622
                ILMGASVGIPASSLCIIR  Q92275      5334
                KGQIVMTSDKPPKMLK    A0RLX8      360106
                KTMPLILSGVDVVAMAR   O49289      3702
                MIPMIVLATTNQNKVK    Q6AQD7      177439
                <span style="color: #888;">... (1118 additional entries) ...</span>

2       1800.0002
                VEEIYEDDEMNT        A0A1Y2ULM3  9606
                CHGWGGCHHIR         A0A8R8NSG9  10090
                <span style="color: #888;">... (2394 additional entries) ...</span>
<span style="color: #888;">... </span>
<span style="color: #b5cea8;">[SUCCESS] Found 239 peptides matching criteria.</span>
<span style="color: #569cd6;">Execution time:</span> <span style="color: #dcdcaa;">469.129µs</span>
<span style="color: #4ec9b0;">user@proteomics-hpc:~$</span> <span style="cursor: underline;">_</span></pre>
                </div>
            </div>
    </div>
</div>

<br>



<div class="container ">

<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentStyleType="text/css" data-diagram-type="DESCRIPTION" height="708.3333px" preserveAspectRatio="none" style="width:1255px;height:708px;" version="1.1" viewBox="0 0 1255 708" width="1255.2083px" zoomAndPan="magnify"><defs/><g><!--cluster Phase 1: Database Construction (Build Workflow)--><g class="cluster" data-qualified-name="Phase 1. Database Construction .Build Workflow." data-source-line="5" id="ent0002"><path d="M20.8333,16.6667 L377.9338,16.6667 A6.25,6.25 0 0 1 382.1004,20.8333 L389.3921,47.8841 L856.25,47.8841 A4.1667,4.1667 0 0 1 860.4167,52.0508 L860.4167,581.0521 A4.1667,4.1667 0 0 1 856.25,585.2188 L20.8333,585.2188 A4.1667,4.1667 0 0 1 16.6667,581.0521 L16.6667,20.8333 A4.1667,4.1667 0 0 1 20.8333,16.6667" fill="#F8F9FA" style="stroke:#2FA4E7;stroke-width:2.0833;"/><line style="stroke:#2FA4E7;stroke-width:2.0833;" x1="16.6667" x2="389.3921" y1="47.8841" y2="47.8841"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" font-weight="bold" lengthAdjust="spacing" textLength="348.7671" x="26.0417" y="35.5611">Phase 1: Database Construction (Build Workflow)</text></g><!--cluster Pre-Build Configuration--><g class="cluster" data-qualified-name="Phase 1. Database Construction .Build Workflow..Pre-Build Configuration" data-source-line="7" id="ent0004"><polygon fill="none" points="41.6667,79.1667,52.0833,68.75,695.8333,68.75,695.8333,184.3125,685.4167,194.7292,41.6667,194.7292,41.6667,79.1667" style="stroke:#2FA4E7;stroke-width:1.0417;"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="685.4167" x2="695.8333" y1="79.1667" y2="68.75"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="41.6667" x2="685.4167" y1="79.1667" y2="79.1667"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="685.4167" x2="685.4167" y1="79.1667" y2="194.7292"/><text fill="#343A40" font-family="'Verdana'" font-size="12.5" font-weight="bold" lengthAdjust="spacing" textLength="165.863" x="281.6518" y="99.1028">Pre-Build Configuration</text></g><!--cluster Multi-Stage Optimization--><g class="cluster" data-qualified-name="Phase 1. Database Construction .Build Workflow..Multi-Stage Optimization" data-source-line="15" id="ent0009"><polygon fill="#E3F2FD" points="179.1667,348.8542,189.5833,338.4375,835.4167,338.4375,835.4167,454,825,464.4167,179.1667,464.4167,179.1667,348.8542" style="stroke:#2FA4E7;stroke-width:1.0417;"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="825" x2="835.4167" y1="348.8542" y2="338.4375"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="179.1667" x2="825" y1="348.8542" y2="348.8542"/><line style="stroke:#2FA4E7;stroke-width:1.0417;" x1="825" x2="825" y1="348.8542" y2="464.4167"/><text fill="#343A40" font-family="'Verdana'" font-size="12.5" font-weight="bold" lengthAdjust="spacing" textLength="176.8616" x="414.6942" y="368.7903">Multi-Stage Optimization</text></g><!--cluster Phase 2: Mass-Based Search (Query Workflow)--><g class="cluster" data-qualified-name="Phase 2. Mass-Based Search .Query Workflow." data-source-line="24" id="ent0014"><path d="M889.5833,343.6458 L1228.3976,343.6458 A6.25,6.25 0 0 1 1232.5643,347.8125 L1239.856,374.8633 L1238.5417,374.8633 A4.1667,4.1667 0 0 1 1242.7083,379.0299 L1242.7083,692.4792 A4.1667,4.1667 0 0 1 1238.5417,696.6458 L889.5833,696.6458 A4.1667,4.1667 0 0 1 885.4167,692.4792 L885.4167,347.8125 A4.1667,4.1667 0 0 1 889.5833,343.6458" fill="#F1F8E9" style="stroke:#2FA4E7;stroke-width:2.0833;"/><line style="stroke:#2FA4E7;stroke-width:2.0833;" x1="885.4167" x2="1239.856" y1="374.8633" y2="374.8633"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" font-weight="bold" lengthAdjust="spacing" textLength="330.481" x="894.7917" y="362.5403">Phase 2: Mass-Based Search (Query Workflow)</text></g><!--entity Source--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Source" data-source-line="6" id="ent0003"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="127.6123" x="716.4063" y="124.9792"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="96.3623" x="732.0313" y="152.2069">UniProtKB Data</text></g><!--entity Digester--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Digester" data-source-line="13" id="ent0008"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="189.6729" x="376" y="243.6875"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="158.4229" x="391.625" y="270.9153">In Silico Digestion Engine</text></g><!--entity DB--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..DB" data-source-line="21" id="ent0013"><path d="M526.0313,523.7917 C526.0313,513.375 588.5374,513.375 588.5374,513.375 C588.5374,513.375 651.0435,513.375 651.0435,523.7917 L651.0435,558.1341 C651.0435,568.5508 588.5374,568.5508 588.5374,568.5508 C588.5374,568.5508 526.0313,568.5508 526.0313,558.1341 L526.0313,523.7917" fill="#FFF9C4" style="stroke:#2FA4E7;stroke-width:1.0417;"/><path d="M526.0313,523.7917 C526.0313,534.2083 588.5374,534.2083 588.5374,534.2083 C588.5374,534.2083 651.0435,534.2083 651.0435,523.7917" fill="none" style="stroke:#2FA4E7;stroke-width:1.0417;"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="93.7622" x="541.6563" y="555.1861">RocksDB Index</text></g><!--entity Tax--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Pre-Build Configuration.Tax" data-source-line="8" id="ent0005"><rect fill="none" height="60.3516" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="192.627" x="57.8542" y="117.7083"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="161.377" x="73.4792" y="144.9361">NCBI Taxonomy Selection</text><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="107.4402" x="73.4792" y="159.4869">(TaxID / Division)</text></g><!--entity Enzyme--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Pre-Build Configuration.Enzyme" data-source-line="9" id="ent0006"><rect fill="none" height="60.3516" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="178.3813" x="286.8542" y="117.7083"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="111.2244" x="302.4792" y="144.9361">Enzyme Selection</text><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="147.1313" x="302.4792" y="159.4869">(Trypsin/Chymotrypsin)</text></g><!--entity Params--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Pre-Build Configuration.Params" data-source-line="10" id="ent0007"><rect fill="none" height="60.3516" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="177.887" x="501.6771" y="117.7083"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="135.26" x="517.3021" y="144.9361">Digestion Parameters</text><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="146.637" x="517.3021" y="159.4869">(Length 6-50aa, MC=2)</text></g><!--entity Disc--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Multi-Stage Optimization.Disc" data-source-line="16" id="ent0010"><rect fill="none" height="60.3516" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="152.5452" x="195.6042" y="387.3958"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="121.2952" x="211.2292" y="414.6236">Mass Discretization</text><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="116.3696" x="211.2292" y="429.1744">(4 Decimal Places)</text></g><!--entity Encode--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Multi-Stage Optimization.Encode" data-source-line="17" id="ent0011"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="172.7356" x="384.4688" y="394.6771"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="141.4856" x="400.0938" y="421.9049">5-bit Peptide Encoding</text></g><!--entity Accession--><g class="entity" data-qualified-name="Phase 1. Database Construction .Build Workflow..Multi-Stage Optimization.Accession" data-source-line="18" id="ent0012"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="224.6033" x="593.9479" y="394.6771"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="193.3533" x="609.5729" y="421.9049">Base36 Accession Serialization</text></g><!--entity Input--><g class="entity" data-qualified-name="Phase 2. Mass-Based Search .Query Workflow..Input" data-source-line="25" id="ent0015"><rect fill="none" height="60.3516" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="186.3892" x="928.6771" y="387.3958"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="155.1392" x="944.3021" y="414.6236">Experimental Mass Input</text><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="121.3745" x="944.3021" y="429.1744">(Mass + Tolerance)</text></g><!--entity Search--><g class="entity" data-qualified-name="Phase 2. Mass-Based Search .Query Workflow..Search" data-source-line="26" id="ent0016"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="196.2097" x="923.7708" y="518.0625"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="164.9597" x="939.3958" y="545.2903">Search Engine (Rust Core)</text></g><!--entity Output--><g class="entity" data-qualified-name="Phase 2. Mass-Based Search .Query Workflow..Output" data-source-line="27" id="ent0017"><rect fill="none" height="45.8008" rx="4.1667" ry="4.1667" style="stroke:#2FA4E7;stroke-width:1.0417;" width="284.4543" x="902.5625" y="634.1771"/><text fill="#2FA4E7" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="253.2043" x="918.1875" y="661.4049">Output: Peptides + Accessions + TaxIDs</text></g><!--link Source to Digester--><g class="link" data-entity-1="ent0003" data-entity-2="ent0008" data-link-type="dependency" data-source-line="30" id="lnk18"><path d="M750.6042,171.0312 C734.9583,181.8958 715.0521,194.4583 695.8333,203.0625 C654.7604,221.4375 613.0652,234.1164 572.1485,244.2935" fill="none" id="Source-to-Digester" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="566.0833,245.8021,576.1869,247.5827,571.1377,244.5449,574.1754,239.4957,566.0833,245.8021" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Tax to Digester--><g class="link" data-entity-1="ent0005" data-entity-2="ent0008" data-link-type="dependency" data-source-line="31" id="lnk19"><path d="M212.6458,178.5 C230.4375,186.9687 250.1875,195.8438 268.75,203.0625 C307.7708,218.2187 346.1176,230.5104 383.3051,241.4271" fill="none" id="Tax-to-Digester" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="389.3021,243.1875,381.4803,236.5489,384.3046,241.7205,379.133,244.5448,389.3021,243.1875" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Enzyme to Digester--><g class="link" data-entity-1="ent0006" data-entity-2="ent0008" data-link-type="dependency" data-source-line="32" id="lnk20"><path d="M399.9583,178.3542 C416.4167,198.5938 433.974,220.2025 448.9636,238.64" fill="none" id="Enzyme-to-Digester" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="452.9063,243.4896,450.2253,233.5868,449.6207,239.4483,443.7593,238.8437,452.9063,243.4896" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Params to Digester--><g class="link" data-entity-1="ent0007" data-entity-2="ent0008" data-link-type="dependency" data-source-line="33" id="lnk21"><path d="M560.3958,178.3542 C539.6042,198.5938 516.9052,220.6922 497.9677,239.1297" fill="none" id="Params-to-Digester" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="493.4896,243.4896,503.1134,239.9352,497.2214,239.8563,497.3002,233.9643,493.4896,243.4896" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Digester to Disc--><g class="link" data-entity-1="ent0008" data-entity-2="ent0010" data-link-type="dependency" data-source-line="35" id="lnk22"><path d="M429.5625,289.8542 C409.9167,301.0625 386.4583,315.375 366.6667,330.1042 C343.4375,347.3958 323.8571,365.0612 305.7738,382.5508" fill="none" id="Digester-to-Disc" style="stroke:#2FA4E7;stroke-width:3.125;"/>
<polygon fill="#2FA4E7" points="301.2813,386.8958,310.9168,383.3733,305.025,383.275,305.1234,377.3832,301.2813,386.8958" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Digester to Encode--><g class="link" data-entity-1="ent0008" data-entity-2="ent0011" data-link-type="dependency" data-source-line="36" id="lnk23"><path d="M470.8333,289.9167 C470.8333,318.1562 470.8333,360.25 470.8333,388.3229" fill="none" id="Digester-to-Encode" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="470.8333,394.5729,475,385.1979,470.8333,389.3646,466.6667,385.1979,470.8333,394.5729" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Digester to Accession--><g class="link" data-entity-1="ent0008" data-entity-2="ent0012" data-link-type="dependency" data-source-line="37" id="lnk24"><path d="M508.6771,289.8021 C528.5417,301.5833 553.2188,316.4167 575,330.1042 C608.8333,351.3854 641.735,373.1354 668.1308,390.8229" fill="none" id="Digester-to-Accession" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="673.3229,394.3021,667.8542,385.6219,668.9962,391.4028,663.2153,392.5447,673.3229,394.3021" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Disc to DB--><g class="link" data-entity-1="ent0010" data-entity-2="ent0013" data-link-type="dependency" data-source-line="39" id="lnk25"><path d="M318.5313,448.0313 C333.5417,456.7396 350.4792,465.7813 366.6667,472.75 C418.6042,495.1042 474.3157,511.5837 519.4719,523.3233" fill="none" id="Disc-to-DB" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="525.5208,524.8958,517.4958,518.5043,520.4801,523.5853,515.3991,526.5696,525.5208,524.8958" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Encode to DB--><g class="link" data-entity-1="ent0011" data-entity-2="ent0013" data-link-type="dependency" data-source-line="40" id="lnk26"><path d="M492.4271,440.8646 C512.1979,461.25 537.1687,486.9727 558.2208,508.6706" fill="none" id="Encode-to-DB" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="562.5729,513.1563,559.0351,503.5263,558.9461,509.4182,553.0542,509.3292,562.5729,513.1563" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Accession to DB--><g class="link" data-entity-1="ent0012" data-entity-2="ent0013" data-link-type="dependency" data-source-line="41" id="lnk27"><path d="M684.6563,440.8646 C664.8854,461.25 639.9147,486.9727 618.8626,508.6706" fill="none" id="Accession-to-DB" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="614.5104,513.1563,624.0291,509.3292,618.1372,509.4182,618.0482,503.5263,614.5104,513.1563" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link Input to Search--><g class="link" data-entity-1="ent0015" data-entity-2="ent0016" data-link-type="dependency" data-source-line="43" id="lnk28"><path d="M1021.875,447.9792 C1021.875,469.4375 1021.875,491.8438 1021.875,511.4167" fill="none" id="Input-to-Search" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="1021.875,517.6667,1026.0417,508.2917,1021.875,512.4583,1017.7083,508.2917,1021.875,517.6667" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><!--link DB to Search--><g class="link" data-entity-1="ent0013" data-entity-2="ent0016" data-link-type="dependency" data-source-line="44" id="lnk29"><path d="M651.5521,540.9688 C722.9063,540.9688 833.8333,540.9688 917.2604,540.9688" fill="none" id="DB-to-Search" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="923.5104,540.9688,914.1354,536.8021,918.3021,540.9688,914.1354,545.1354,923.5104,540.9688" style="stroke:#2FA4E7;stroke-width:3.125;"/><text fill="#343A40" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="101.0803" x="736.8854" y="515.0715">Binary Search &amp;</text><text fill="#343A40" font-family="'Verdana'" font-size="12.5" lengthAdjust="spacing" textLength="100.9644" x="736.9434" y="529.6223">Sequential Scan</text></g><!--link Search to Output--><g class="link" data-entity-1="ent0016" data-entity-2="ent0017" data-link-type="dependency" data-source-line="45" id="lnk30"><path d="M1026.3438,564.2604 C1030.3854,584.3542 1035.058,607.6334 1039.0997,627.7063" fill="none" id="Search-to-Output" style="stroke:#2FA4E7;stroke-width:3.125;"/><polygon fill="#2FA4E7" points="1040.3333,633.8333,1042.5675,623.8203,1039.3053,628.7275,1034.3981,625.4652,1040.3333,633.8333" style="stroke:#2FA4E7;stroke-width:3.125;"/></g><?plantuml-src PLDTRzGm47pFhrXI2Lq5KQ5VQay2gRbSf1EK7aqH3vG7rxVDMPVOmNQWAUf_PstdBat9GnIFTtRisSahkyOQWQDkAsGoKQsjXCJ8R8Hic6OrX8AcOa8-Gxcg6oLHsi9s5O96RfaiAuoYXl4DAn7YvPePXFTJo9XbT-vxfgInkkLMA0cJj1NL2huhlRclr9_z6FRoi_mylu2_4T3puviKIwtifzGt-0dCGA5QpT5lIxLo6XgJq8TwtukorSmrZ_iUli-NMRg06_QWfAexAB12Vu1REI5maS41PEAtC0JjUn521_9SFdPq_H5BTuqHyc2sxcfbmo8mG_50paI9nbzswKn3YzfGWyyeIxk6q-Ja7MDluNBsuJ3mVPNn_2V_zg-TrH0l91IY4bp1q7ekInfOxFW1H3rmWqkNRML5KbWtbgl6YbeyzZRjpO_ompnxRjSbCwwJuHfjNqTdFeOCkQXP1SkASJJXmAviO9uaTyB24ac1LEUIgvMGPUyCBPuvar8SZaxXWbChvpmPh0MhUZrFsUszz6ErZLDyhVZ6P2aiv0eVWWCffIZFpsV7qTD_KJoSWhjSuiHNfCWqNyFaQukw6yVmVNusFz_6SFxGqDbgyfykxyrPoAQrv8bVl84RLQ5cacFmnE--75ml5cO5a-lMM4gindr_wh0RvAvQI_pfraOdi7F2gxdC6gyKIjr5mty1IV9no00LZe4-oYCi96wCHTkl7gOHZv1-bYDi65Ra286PHg5qkngctGEHzyglUWjeVeaMvThk89X2d0h9TBS5Nj_A0d-rv2ib1WhEP1prMwvJSEKV?></g></svg>

</div>


<div class="container my-5">
    <div class="row justify-content-center text-center mb-4">
        <div class="col-lg-8">
            <h2 class="fw-bold text-dark">Accessing the Engine</h2>
            <p class="text-muted">The DigestedProteinDB Enterprise Engine (Rust) is optimized for high-throughput production environments and large-scale metaproteomics.</p>
        </div>
    </div>

    <div class="row g-4 justify-content-center">
        <div class="col-md-5">
            <div class="card h-100 shadow-sm border-0 bg-light">
                <div class="card-body p-4 d-flex flex-column align-items-center justify-content-center text-center">
                    <div class="mb-3 text-primary">
                        <i class="bi bi-cpu fs-1"></i>
                    </div>
                    <h4 class="fw-bold">High-Performance CLI</h4>
                    <p class="small text-muted mb-4">Optimized Rust binary for Linux, macOS, and Windows. Features sub-millisecond query resolution and ultra-low memory footprint.</p>

                    <div class="d-grid w-100">
                        <button class="btn btn-secondary btn-lg disabled shadow-sm">
                            <i class="bi bi-clock-history me-2"></i> Available Soon
                        </button>
                    </div>
                    <small class="mt-3 text-primary fw-bold">Pre-release Testing in Progress</small>
                </div>
            </div>
        </div>

        <div class="col-md-5">
            <div class="card h-100 shadow-sm border-0 bg-light text-center">
                <div class="card-body p-4 d-flex flex-column align-items-center justify-content-center">
                    <div class="mb-3 text-success">
                        <i class="bi bi-database-down fs-1"></i>
                    </div>
                    <h4 class="fw-bold">Pre-built Indices</h4>
                    <p class="small text-muted mb-4">Download the complete UniProtKB/TrEMBL 2026 digest (Trypsin, MC=2). Approximately 380 GB of optimized RocksDB data.</p>

                    <div class="d-grid w-100">
                        <button class="btn btn-outline-success btn-lg disabled">
                            <i class="bi bi-cloud-download me-2"></i> Coming Q1 2026
                        </button>
                    </div>
                    <small class="mt-3 text-muted">Direct high-speed mirror links will be provided.</small>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-5 justify-content-center">
        <div class="col-lg-8 text-center p-4 bg-white border rounded shadow-sm">
            <h5 class="fw-bold">Need Early Access?</h5>
            <p class="mb-0">For custom database builds, pipeline integration support, or early access to the Rust CLI engine, please contact the development team at <a href="mailto:jdiminic@pbf.hr" class="text-decoration-none fw-bold">jdiminic@pbf.hr</a>.</p>
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
  <div class="row mb-4 ">
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

    <div class="col-lg-6 mb-3 ">
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
