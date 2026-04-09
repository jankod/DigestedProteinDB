# DigestedProteinDB – Python REST API Examples

This directory contains Python scripts demonstrating how to programmatically query the
[DigestedProteinDB](https://digestedproteindb.pbf.hr) database via its public REST API.
DigestedProteinDB is a high-performance database of theoretically digested proteins, designed
to support peptide identification in mass spectrometry (MS) experiments.

---

## Requirements

- Python 3.7+
- [`requests`](https://pypi.org/project/requests/) library

Install the dependency with:

```bash
pip install requests
```

---

## Script: `example_rest_api.py`

A self-contained example script that demonstrates the three most common query patterns
against the DigestedProteinDB REST API endpoint:

```
GET https://digestedproteindb.pbf.hr/search.php
```

### What the script does

| Example | Description |
|---------|-------------|
| **1** | Search by an explicit mass range (`mass1` / `mass2` parameters) |
| **2** | Search by a peptide amino acid sequence — the script computes the monoisotopic mass locally and queries with a configurable mass tolerance (±0.02 Da by default) |
| **3** | Retrieve a specific page of results using the `page` and `pageSize` parameters |

### API parameters

| Parameter  | Type    | Description |
|------------|---------|-------------|
| `mass1`    | float   | Lower bound of the mass range (Da, inclusive) |
| `mass2`    | float   | Upper bound of the mass range (Da, inclusive) |
| `page`     | integer | Page number (default: 1) |
| `pageSize` | integer | Results per page (default: 10, max: 1000) |

### Response format

The API returns JSON with the following structure:

```json
{
    "totalResult": 431,
    "page": 1,
    "pageSize": 10,
    "duration": "00:00:00.024",
    "memory": "2012 MB",
    "result": [
        {
            "1247.5001": [
                {
                    "seq": "SSSTSSTDSTTST",
                    "acc": ["A0A822EEP4"]
                }
            ]
        }
    ]
}
```

- **`result`** — list of objects keyed by the peptide mass; each value is a list of peptides
- **`seq`** — peptide amino acid sequence (single-letter code)
- **`acc`** — list of UniProt accession numbers of proteins containing this peptide

### Running the script

```bash
python example_rest_api.py
```

Expected output (truncated):

```
=================================================================
Example 1: search by mass range (1247.50 – 1247.52)
=================================================================
Total results : 118
Page          : 1 / pageSize=5
Duration      : 00:00:00.001
Memory        : 2012 MB
-----------------------------------------------------------------
  Mass: 1247.5001     Sequence: SSSTSSTDSTTST        Acc: A0A822EEP4
  Mass: 1247.5002     Sequence: SWGDHNCHHR           Acc: M7BXC7
  ...
```

### Adapting the script to your own analysis

The helper functions can be imported and used directly in your own scripts or Jupyter notebooks:

```python
from example_rest_api import search_by_mass, search_by_peptide

# Query a narrow mass window around a measured MS peak
results = search_by_mass(mass1=1500.70, mass2=1500.72, page_size=100)

# Or search by sequence with a custom tolerance
results = search_by_peptide("ALELFR", tolerance_da=0.01, page_size=50)

for item in results["result"]:
    for mass, peptides in item.items():
        for pep in peptides:
            print(pep["seq"], pep["acc"])
```

---

## REST API documentation

Full API documentation is available at:  
**https://digestedproteindb.pbf.hr/rest.php**