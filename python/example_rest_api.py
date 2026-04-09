"""
Example usage of the DigestedProteinDB REST API
https://digestedproteindb.pbf.hr/rest.php
"""

import requests

BASE_URL = "https://digestedproteindb.pbf.hr/search.php"

# Monoisotopic masses of amino acid residues (Da)
AA_MASS = {
    'A': 71.03711,  'R': 156.10111, 'N': 114.04293, 'D': 115.02694,
    'C': 103.00919, 'E': 129.04259, 'Q': 128.05858, 'G': 57.02146,
    'H': 137.05891, 'I': 113.08406, 'L': 113.08406, 'K': 128.09496,
    'M': 131.04049, 'F': 147.06841, 'P': 97.05276,  'S': 87.03203,
    'T': 101.04768, 'W': 186.07931, 'Y': 163.06333, 'V': 99.06841,
}
WATER = 18.01056  # H2O added to complete the peptide chain


def peptide_mass(sequence: str) -> float:
    """Calculate the monoisotopic mass of a peptide sequence."""
    return sum(AA_MASS[aa] for aa in sequence.upper()) + WATER


def search_by_mass(mass1: float, mass2: float, page: int = 1, page_size: int = 10) -> dict:
    """Search peptides by mass range."""
    params = {
        "mass1": mass1,
        "mass2": mass2,
        "page": page,
        "pageSize": page_size,
    }
    response = requests.get(BASE_URL, params=params)
    response.raise_for_status()
    return response.json()


def search_by_peptide(sequence: str, tolerance_da: float = 0.02, page_size: int = 10) -> dict:
    """Search by peptide sequence: compute its mass locally, then query with ± tolerance."""
    mass = peptide_mass(sequence)
    print(f"  Calculated mass for '{sequence}': {mass:.5f} Da  (±{tolerance_da} Da)")
    return search_by_mass(mass - tolerance_da, mass + tolerance_da, page_size=page_size)


def print_results(data: dict):
    """Print a summary of search results."""
    print(f"Total results : {data.get('totalResult', 0)}")
    print(f"Page          : {data.get('page')} / pageSize={data.get('pageSize')}")
    print(f"Duration      : {data.get('duration')}")
    print(f"Memory        : {data.get('memory')}")
    print("-" * 65)

    for item in data.get("result", []):
        for mass, peptides in item.items():
            for pep in peptides:
                seq = pep.get("seq", "?")
                accs = ", ".join(pep.get("acc", []))
                print(f"  Mass: {mass:<12}  Sequence: {seq:<20}  Acc: {accs}")


if __name__ == "__main__":

    # --- Example 1: search by mass range ---
    print("=" * 65)
    print("Example 1: search by mass range (1247.50 – 1247.52)")
    print("=" * 65)
    data = search_by_mass(mass1=1247.50, mass2=1247.52, page_size=5)
    print_results(data)

    print()

    # --- Example 2: search by peptide sequence ---
    peptide = "SYTFHFKYR"
    print("=" * 65)
    print(f"Example 2: search by peptide sequence '{peptide}'")
    print("=" * 65)
    data = search_by_peptide(peptide, tolerance_da=0.02, page_size=5)
    print_results(data)

    print()

    # --- Example 3: pagination – fetch page 2 ---
    print("=" * 65)
    print("Example 3: pagination – page 2, mass range 1800.00 – 1800.02")
    print("=" * 65)
    data = search_by_mass(mass1=1800.00, mass2=1800.02, page=2, page_size=5)
    print_results(data)