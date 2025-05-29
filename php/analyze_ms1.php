<?php
const HEAD_TITLE = "Digested Protein DB";
include_once 'lib.php';
?>

<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/7.8.5/d3.min.js"></script>
    <link  href="analyze_ms1.css" rel="stylesheet">
</head>
<body>
<?php include_once "inc/html_navbar.php"; ?>


<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="main-container">
                <!-- Header -->
                <div class="header-section">
                    <h1><i class="fas fa-wave-square"></i> MS Spektar Viewer</h1>
                    <p class="lead mb-0">Interaktivni prikaz masenih spektra s mogućnošću filtriranja po intenzitetu
                        pikova</p>
                </div>

                <!-- Control Panel -->
                <div class="control-panel">
                    <div class="row align-items-center">
                        <div class="col-md-6">
                            <label for="peakSlider" class="form-label">
                                <i class="fas fa-filter"></i> Prikaži top <span id="peakCount">50</span> pikova
                            </label>
                            <input type="range" class="form-range" id="peakSlider" min="10" max="200" value="50"
                                   step="10">
                        </div>
                        <div class="col-md-2">
                            <button class="btn btn-primary" id="refreshData">
                                <i class="fas fa-sync-alt"></i> Generiraj nove podatke
                            </button>
                        </div>
                        <div class="col-md-2">
                            <input type="file" class="d-none" id="fileInput" accept=".txt,.csv,.mzml,.mgf,.msp">
                            <button class="btn btn-info" id="uploadBtn">
                                <i class="fas fa-upload"></i> Upload MS spektar
                            </button>
                        </div>
                        <div class="col-md-2">
                            <button class="btn btn-success" id="exportData">
                                <i class="fas fa-download"></i> Izvezi podatke
                            </button>
                        </div>
                    </div>

                    <div class="row align-items-center mt-3">
                        <div class="col-md-6">
                            <label for="deltaInput" class="form-label">
                                <i class="fas fa-equals"></i> Delta (m/z margina, Da):
                            </label>
                            <input type="number" class="form-control" id="deltaInput" value="0.5" min="0" max="10"
                                   step="0.01" style="max-width: 200px;">
                        </div>
                        <div class="col-md-6">
                            <button class="btn btn-warning" id="analyzeBtn">
                                <i class="fas fa-search"></i> Pošalji na analizu
                            </button>
                        </div>
                    </div>

                </div>

                <!-- Upload Info -->
                <div class="upload-info" id="uploadInfo" style="display: none;">
                    <h6><i class="fas fa-info-circle"></i> Podržani formati MS spektra:</h6>
                    <ul class="mb-0">
                        <li><strong>TXT/CSV:</strong> Dvije kolone (masa, intenzitet) odvojene razmakom, tabom ili
                            zarezom
                        </li>
                        <li><strong>MGF:</strong> Mascot Generic Format datoteke</li>
                        <li><strong>MSP:</strong> NIST MSP format datoteke</li>
                        <li><strong>Primjer TXT formata:</strong> 100.0526 1234.5</li>
                    </ul>
                </div>

                <!-- File Info -->
                <div class="file-info" id="fileInfo" style="display: none;">
                    <h6><i class="fas fa-file-alt"></i> Informacije o datoteci:</h6>
                    <p id="fileDetails" class="mb-0"></p>
                </div>
                <div class="row mb-4">
                    <div class="col-md-3">
                        <div class="stats-card">
                            <i class="fas fa-chart-line icon-large"></i>
                            <h4 id="totalPeaks">0</h4>
                            <p>Ukupno pikova</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stats-card">
                            <i class="fas fa-eye icon-large"></i>
                            <h4 id="visiblePeaks">0</h4>
                            <p>Prikazano pikova</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stats-card">
                            <i class="fas fa-arrow-up icon-large"></i>
                            <h4 id="maxIntensity">0</h4>
                            <p>Max intenzitet</p>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stats-card">
                            <i class="fas fa-weight icon-large"></i>
                            <h4 id="massRange">0-0</h4>
                            <p>Raspon masa (m/z)</p>
                        </div>
                    </div>
                </div>

                <!-- Chart Container -->
                <div class="chart-container">
                    <div id="msChart"></div>
                </div>

                <!-- Peak Details Table -->
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h5><i class="fas fa-table"></i> Detalji prikazanih pikova</h5>
                    </div>
                    <div class="card-body" style="max-height: 300px; overflow-y: auto;">
                        <table class="table table-striped table-hover">
                            <thead>
                            <tr>
                                <th>Redni broj</th>
                                <th>Masa (m/z)</th>
                                <th>Intenzitet</th>
                                <th>Relativni intenzitet (%)</th>
                            </tr>
                            </thead>
                            <tbody id="peakTable">
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Footer -->
                <div class="text-center mt-4 pt-3 border-top">
                    <p class="text-muted">
                        <i class="fas fa-info-circle"></i>
                        MS Spektar Viewer - Powered by D3.js |
                        Zadnje ažuriranje: <span id="currentDate"></span>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Tooltip -->
<div class="tooltip" id="tooltip"></div>

<script>
    // Globalne varijable
    let allPeaks = [];
    let filteredPeaks = [];
    let svg, xScale, yScale, tooltip;
    let currentFileName = '';
    let isUploadedData = false;

    // Dimenzije grafa
    const margin = {top: 30, right: 50, bottom: 60, left: 80};
    const width = 1000 - margin.left - margin.right;
    const height = 500 - margin.top - margin.bottom;

    // Inicijalizacija
    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('currentDate').textContent = new Date().toLocaleDateString('hr-HR');
        tooltip = d3.select("#tooltip");

        generateSampleData();
        initializeChart();
        updateChart();

        // Event listeneri
        document.getElementById('peakSlider').addEventListener('input', function () {
            const value = this.value;
            document.getElementById('peakCount').textContent = value;
            filterPeaks(parseInt(value));
            updateChart();
        });

        document.getElementById('refreshData').addEventListener('click', function () {
            generateSampleData();
            const currentFilter = parseInt(document.getElementById('peakSlider').value);
            filterPeaks(currentFilter);
            updateChart();
        });

        document.getElementById('uploadBtn').addEventListener('click', function () {
            document.getElementById('fileInput').click();
        });

        document.getElementById('fileInput').addEventListener('change', handleFileUpload);

        document.getElementById('exportData').addEventListener('click', exportData);
    });

    // Generiranje uzornih podataka
    function generateSampleData() {
        allPeaks = [];
        const numPeaks = 150 + Math.floor(Math.random() * 100); // 150-250 pikova

        for (let i = 0; i < numPeaks; i++) {
            const mass = 100 + Math.random() * 1900; // masa između 100-2000 m/z
            const intensity = Math.random() * 100000; // intenzitet 0-100000

            allPeaks.push({
                id: i + 1,
                mass: parseFloat(mass.toFixed(2)),
                intensity: parseFloat(intensity.toFixed(0))
            });
        }

        // Sortiranje po intenzitetu (silazno)
        allPeaks.sort((a, b) => b.intensity - a.intensity);

        isUploadedData = false;
        currentFileName = '';
        document.getElementById('fileInfo').style.display = 'none';
        updateStatistics();
    }


    document.getElementById('analyzeBtn').addEventListener('click', function () {
        const delta = parseFloat(document.getElementById('deltaInput').value);
        const topN = parseInt(document.getElementById('peakSlider').value);
        let info = `Broj pikova: ${topN}\nDelta (m/z): ${delta}\n`;
        if (isUploadedData && currentFileName) {
            info += `Datoteka: ${currentFileName}\n`;
        }
        info += `Pikovi za analizu: ${filteredPeaks.length}`;
        alert("Analiza parametri:\n" + info);

        // Ovdje možeš poslati AJAX na backend, npr.:
        /*
        fetch('/analyze', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                delta: delta,
                peaks: filteredPeaks
            })
        }).then(...)
        */
    });

    // Rukovanje upload-om datoteke
    function handleFileUpload(event) {
        const file = event.target.files[0];
        if (!file) return;

        currentFileName = file.name;
        const fileExtension = file.name.split('.').pop().toLowerCase();

        const reader = new FileReader();
        reader.onload = function (e) {
            const content = e.target.result;

            try {
                parseSpectrumFile(content, fileExtension);
                isUploadedData = true;

                // Prikaži informacije o datoteci
                document.getElementById('fileDetails').innerHTML =
                    `<strong>Datoteka:</strong> ${file.name}<br>
                         <strong>Veličina:</strong> ${(file.size / 1024).toFixed(1)} KB<br>
                         <strong>Format:</strong> ${fileExtension.toUpperCase()}<br>
                         <strong>Učitano:</strong> ${allPeaks.length} pikova`;
                document.getElementById('fileInfo').style.display = 'block';
                document.getElementById('uploadInfo').style.display = 'none';

                const currentFilter = parseInt(document.getElementById('peakSlider').value);
                filterPeaks(currentFilter);
                updateChart();

            } catch (error) {
                alert('Greška pri parsiranju datoteke: ' + error.message);
                console.error('Parse error:', error);
            }
        };

        reader.readAsText(file);
    }

    // Parsiranje različitih formata spektra
    function parseSpectrumFile(content, fileExtension) {
        allPeaks = [];

        switch (fileExtension) {
            case 'txt':
            case 'csv':
                parseTxtCsv(content);
                break;
            case 'mgf':
                parseMGF(content);
                break;
            case 'msp':
                parseMSP(content);
                break;
            default:
                // Pokušaj parsirati kao TXT
                parseTxtCsv(content);
        }

        if (allPeaks.length === 0) {
            throw new Error('Nisu pronađeni validni pikovi u datoteci');
        }

        // Sortiranje po intenzitetu (silazno)
        allPeaks.sort((a, b) => b.intensity - a.intensity);
        updateStatistics();
    }

    // Parsiranje TXT/CSV formata
    function parseTxtCsv(content) {
        const lines = content.split('\n');
        let peakId = 1;

        for (let line of lines) {
            line = line.trim();
            if (!line || line.startsWith('#') || line.startsWith('//')) continue;

            // Podržava razmak, tab ili zarez kao separator
            const parts = line.split(/[\s,\t]+/);

            if (parts.length >= 2) {
                const mass = parseFloat(parts[0]);
                const intensity = parseFloat(parts[1]);

                if (!isNaN(mass) && !isNaN(intensity) && intensity > 0) {
                    allPeaks.push({
                        id: peakId++,
                        mass: parseFloat(mass.toFixed(4)),
                        intensity: parseFloat(intensity.toFixed(1))
                    });
                }
            }
        }
    }

    // Parsiranje MGF formata
    function parseMGF(content) {
        const lines = content.split('\n');
        let inSpectrum = false;
        let peakId = 1;

        for (let line of lines) {
            line = line.trim();

            if (line === 'BEGIN IONS') {
                inSpectrum = true;
                continue;
            } else if (line === 'END IONS') {
                inSpectrum = false;
                continue;
            }

            if (inSpectrum && !line.startsWith('TITLE=') &&
                !line.startsWith('PEPMASS=') && !line.startsWith('CHARGE=') &&
                !line.startsWith('RTINSECONDS=') && line.includes(' ')) {

                const parts = line.split(/\s+/);
                if (parts.length >= 2) {
                    const mass = parseFloat(parts[0]);
                    const intensity = parseFloat(parts[1]);

                    if (!isNaN(mass) && !isNaN(intensity) && intensity > 0) {
                        allPeaks.push({
                            id: peakId++,
                            mass: parseFloat(mass.toFixed(4)),
                            intensity: parseFloat(intensity.toFixed(1))
                        });
                    }
                }
            }
        }
    }

    // Parsiranje MSP formata
    function parseMSP(content) {
        const lines = content.split('\n');
        let inPeaks = false;
        let peakId = 1;

        for (let line of lines) {
            line = line.trim();

            if (line.startsWith('Num peaks:')) {
                inPeaks = true;
                continue;
            }

            if (inPeaks && line && !line.startsWith('Name:') && !line.startsWith('MW:')) {
                // MSP format: "mass intensity; mass intensity; ..."
                const peakPairs = line.split(';');

                for (let pair of peakPairs) {
                    const parts = pair.trim().split(/\s+/);
                    if (parts.length >= 2) {
                        const mass = parseFloat(parts[0]);
                        const intensity = parseFloat(parts[1]);

                        if (!isNaN(mass) && !isNaN(intensity) && intensity > 0) {
                            allPeaks.push({
                                id: peakId++,
                                mass: parseFloat(mass.toFixed(4)),
                                intensity: parseFloat(intensity.toFixed(1))
                            });
                        }
                    }
                }
            }
        }
    }

    // Filtriranje pikova po broju
    function filterPeaks(topN) {
        filteredPeaks = allPeaks.slice(0, topN);
        // Sortiranje filtiranih pikova po masi za prikaz
        filteredPeaks.sort((a, b) => a.mass - b.mass);

        document.getElementById('visiblePeaks').textContent = filteredPeaks.length;
        updatePeakTable();
    }

    // Ažuriranje statistika
    function updateStatistics() {
        document.getElementById('totalPeaks').textContent = allPeaks.length;

        if (allPeaks.length > 0) {
            const maxInt = Math.max(...allPeaks.map(p => p.intensity));
            const minMass = Math.min(...allPeaks.map(p => p.mass));
            const maxMass = Math.max(...allPeaks.map(p => p.mass));

            document.getElementById('maxIntensity').textContent = maxInt.toLocaleString();
            document.getElementById('massRange').textContent =
                `${minMass.toFixed(1)}-${maxMass.toFixed(1)}`;
        }
    }

    // Inicijalizacija grafa
    function initializeChart() {
        // Uklanjanje postojećeg SVG-a
        d3.select("#msChart").selectAll("*").remove();

        // Kreiranje SVG elementa
        svg = d3.select("#msChart")
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Kreiranje skala
        xScale = d3.scaleLinear().range([0, width]);
        yScale = d3.scaleLinear().range([height, 0]);

        // Kreiranje osi
        svg.append("g")
            .attr("class", "axis x-axis")
            .attr("transform", `translate(0,${height})`);

        svg.append("g")
            .attr("class", "axis y-axis");

        // Grid lines
        svg.append("g")
            .attr("class", "grid x-grid")
            .attr("transform", `translate(0,${height})`);

        svg.append("g")
            .attr("class", "grid y-grid");

        // Labeli osi
        svg.append("text")
            .attr("class", "axis-label")
            .attr("text-anchor", "middle")
            .attr("x", width / 2)
            .attr("y", height + 50)
            .style("font-size", "14px")
            .style("font-weight", "bold")
            .text("Masa (m/z)");

        svg.append("text")
            .attr("class", "axis-label")
            .attr("text-anchor", "middle")
            .attr("transform", "rotate(-90)")
            .attr("x", -height / 2)
            .attr("y", -50)
            .style("font-size", "14px")
            .style("font-weight", "bold")
            .text("Intenzitet");
    }

    // Ažuriranje grafa
    function updateChart() {
        if (filteredPeaks.length === 0) return;

        // Ažuriranje skala
        xScale.domain(d3.extent(filteredPeaks, d => d.mass));
        yScale.domain([0, d3.max(filteredPeaks, d => d.intensity) * 1.1]);

        // Ažuriranje osi
        const xAxis = d3.axisBottom(xScale).tickFormat(d3.format(".1f"));
        const yAxis = d3.axisLeft(yScale).tickFormat(d3.format(".2s"));

        svg.select(".x-axis")
            .transition()
            .duration(750)
            .call(xAxis);

        svg.select(".y-axis")
            .transition()
            .duration(750)
            .call(yAxis);

        // Grid lines
        svg.select(".x-grid")
            .transition()
            .duration(750)
            .call(d3.axisBottom(xScale)
                .tickSize(-height)
                .tickFormat("")
            );

        svg.select(".y-grid")
            .transition()
            .duration(750)
            .call(d3.axisLeft(yScale)
                .tickSize(-width)
                .tickFormat("")
            );

        // Kreiranje linija za pikove
        const lines = svg.selectAll(".peak-line")
            .data(filteredPeaks, d => d.id);

        lines.enter()
            .append("line")
            .attr("class", "peak-line")
            .attr("x1", d => xScale(d.mass))
            .attr("x2", d => xScale(d.mass))
            .attr("y1", height)
            .attr("y2", height)
            .transition()
            .duration(750)
            .attr("y2", d => yScale(d.intensity));

        lines.transition()
            .duration(750)
            .attr("x1", d => xScale(d.mass))
            .attr("x2", d => xScale(d.mass))
            .attr("y2", d => yScale(d.intensity));

        lines.exit()
            .transition()
            .duration(750)
            .attr("y2", height)
            .remove();

        // Kreiranje krugova za pikove
        const circles = svg.selectAll(".peak-circle")
            .data(filteredPeaks, d => d.id);

        circles.enter()
            .append("circle")
            .attr("class", "peak-circle")
            .attr("cx", d => xScale(d.mass))
            .attr("cy", height)
            .attr("r", 4)
            .on("mouseover", function (event, d) {
                showTooltip(event, d);
            })
            .on("mouseout", hideTooltip)
            .transition()
            .duration(750)
            .attr("cy", d => yScale(d.intensity));

        circles.transition()
            .duration(750)
            .attr("cx", d => xScale(d.mass))
            .attr("cy", d => yScale(d.intensity));

        circles.exit()
            .transition()
            .duration(750)
            .attr("cy", height)
            .remove();
    }

    // Prikaz tooltip-a
    function showTooltip(event, d) {
        const maxIntensity = Math.max(...filteredPeaks.map(p => p.intensity));
        const relativeIntensity = ((d.intensity / maxIntensity) * 100).toFixed(1);

        tooltip.style("opacity", 1)
            .html(`
                    <strong>Pik #${d.id}</strong><br/>
                    Masa: ${d.mass} m/z<br/>
                    Intenzitet: ${d.intensity.toLocaleString()}<br/>
                    Relativni: ${relativeIntensity}%
                `)
            .style("left", (event.pageX + 10) + "px")
            .style("top", (event.pageY - 10) + "px");
    }

    // Sakrivanje tooltip-a
    function hideTooltip() {
        tooltip.style("opacity", 0);
    }

    // Ažuriranje tabele pikova
    function updatePeakTable() {
        const tbody = document.getElementById('peakTable');
        tbody.innerHTML = '';

        const maxIntensity = Math.max(...filteredPeaks.map(p => p.intensity));

        filteredPeaks.forEach((peak, index) => {
            const relativeIntensity = ((peak.intensity / maxIntensity) * 100).toFixed(1);
            const row = tbody.insertRow();
            row.innerHTML = `
                    <td>${index + 1}</td>
                    <td>${peak.mass}</td>
                    <td>${peak.intensity.toLocaleString()}</td>
                    <td>${relativeIntensity}%</td>
                `;
        });
    }

    // Export podataka
    function exportData() {
        const fileName = isUploadedData && currentFileName ?
            `${currentFileName.split('.')[0]}_filtered.csv` :
            'ms_spektar_podaci.csv';

        const csvContent = "data:text/csv;charset=utf-8,"
            + "ID,Masa(m/z),Intenzitet,Relativni_Intenzitet(%)\n"
            + filteredPeaks.map((peak, index) => {
                const maxIntensity = Math.max(...filteredPeaks.map(p => p.intensity));
                const relativeIntensity = ((peak.intensity / maxIntensity) * 100).toFixed(1);
                return `${index + 1},${peak.mass},${peak.intensity},${relativeIntensity}`;
            }).join("\n");

        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", fileName);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // Prikaži upload info na hover
    document.getElementById('uploadBtn').addEventListener('mouseenter', function () {
        if (!isUploadedData) {
            document.getElementById('uploadInfo').style.display = 'block';
        }
    });

    document.getElementById('uploadBtn').addEventListener('mouseleave', function () {
        setTimeout(() => {
            if (!document.querySelector('#uploadInfo:hover') && !isUploadedData) {
                document.getElementById('uploadInfo').style.display = 'none';
            }
        }, 100);
    });

    // Inicijalno filtriranje
    filterPeaks(50);
</script>


<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>
</body>
</html>



