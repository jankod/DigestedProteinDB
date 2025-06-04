import requests
import json
from collections import Counter
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def parse_peaks_data(file):
    """Parse the peaks data from the text
    :param file:
    """
    # Data is already in the file as provided
    data = []
    with open(file, 'r') as f:
        for line in f:
            parts = line.strip().split('\t')
            if len(parts) == 2:
                mass = float(parts[0])
                intensity = float(parts[1])
                data.append((mass, intensity))
    return data

def query_peptide_by_mass(mass, tolerance=0.02):
    """Query the REST API for peptides matching a given mass"""
    mass1 = mass - tolerance
    mass2 = mass + tolerance
    url = f"http://localhost:7071/search-taxonomy?mass1={mass1}&mass2={mass2}&pageSize=10000000"
    #url = f"https://api.example.com/peptide?mass={mass}&tolerance={tolerance}"
    try:
        response = requests.get(url)
        if response.status_code == 200:
            # Return just the 'result' part which contains the actual peptide data
            data = response.json()
            if 'result' in data and isinstance(data['result'], list):
                return data['result']
            return []
        else:
            print(f"Error querying API for mass {mass}: {response.status_code}")
    except Exception as e:
        print(f"Exception while querying API for mass {mass}: {e}")
    return None

def get_species_name(tax_id):
    """Get species name from taxonomy ID using NCBI API"""
    url = f"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&id={tax_id}&retmode=json"
    try:
        response = requests.get(url)
        if response.status_code == 200:
            data = response.json()
            #print(data)
            if 'result' in data and str(tax_id) in data['result']:
                return data['result'][str(tax_id)]['scientificname']
    except Exception as e:
        print(f"Error getting species name for taxID {tax_id}: {e}")
    return f"Unknown (TaxID: {tax_id})"

def analyze_ms_data(file, top_n=10):
    # Učitavanje i priprema podataka
    peaks_data = parse_peaks_data(file)
    print(f"Učitano {len(peaks_data)} pikova.")

    # Sortiranje po intenzitetu i odabir top 100
    sorted_peaks = sorted(peaks_data, key=lambda x: x[1], reverse=True)
    top_n_peaks = sorted_peaks[:top_n]
    print(f"Odabrano top {top_n} pikova s najvećim intenzitetom.")

    # Prikupljanje rezultata za svaki pik
    all_results = []
    tax_to_species = {}  # Cache za taxID -> species mappings

    for i, (mass, intensity) in enumerate(top_n_peaks):
        print(f"Obrada pika {i+1}/{top_n}: m/z = {mass:.4f}, intenzitet = {intensity:.2e}")

        # REST API upit
        results = query_peptide_by_mass(mass)
        if results:
            all_results.append({
                'mass': mass,
                'intensity': intensity,
                'peptides': results
            })

    # Analiza rezultata
    taxa_counts = Counter()
    protein_counts = Counter()
    peptide_counts = Counter()

    for result in all_results:
        # Each result is a dictionary with mass, intensity, and a list of peptides
        for mass_dict in result['peptides']:
            # Each mass_dict has a key (mass value) that points to a list of peptides
            for mass_key, peptide_list in mass_dict.items():
                # Now peptide_list is properly a list of peptide info dictionaries
                for peptide_info in peptide_list:
                    sequence = peptide_info.get('seq', '')
                    peptide_counts[sequence] += 1

                    for acc_tax in peptide_info.get('accsTax', []):
                        acc = acc_tax.get('acc', '')
                        if acc:
                            protein_counts[acc] += 1

                        for tax_id in acc_tax.get('taxIds', []):
                            taxa_counts[tax_id] += 1
                            if tax_id not in tax_to_species:
                                tax_to_species[tax_id] = get_species_name(tax_id)

    # Stvaranje statistike po vrstama
    species_counts = Counter()
    for tax_id, count in taxa_counts.items():
        species_name = tax_to_species.get(tax_id, f"Unknown (TaxID: {tax_id})")
        species_counts[species_name] += count

    # Prikaz rezultata
    print("\n=== REZULTATI ANALIZE ===\n")

    print("Top 10 vrsta (prema broju pojavljivanja):")
    for species, count in species_counts.most_common(10):
        print(f"  {species}: {count}")

    print("\nTop 10 proteina:")
    for protein, count in protein_counts.most_common(10):
        print(f"  {protein}: {count}")

    print("\nTop 10 peptidnih sekvenci:")
    for peptide, count in peptide_counts.most_common(10):
        print(f"  {peptide}: {count}")

    # Vizualizacija rezultata
    create_visualizations(species_counts, protein_counts, peptide_counts)

def create_visualizations(species_counts, protein_counts, peptide_counts):
    # 1. Graf distribucije vrsta
    plt.figure(figsize=(12, 8))
    top_species = species_counts.most_common(10)
    labels = [name[:20] for name, _ in top_species]  # Skraćeni nazivi vrsta
    sizes = [count for _, count in top_species]
    plt.pie(sizes, labels=labels, autopct='%1.1f%%', startangle=90)
    plt.axis('equal')
    plt.title('Distribucija top 10 vrsta')
    plt.tight_layout()
    plt.savefig('species_distribution.png')

    # 2. Graf distribucije proteina
    plt.figure(figsize=(14, 7))
    top_proteins = protein_counts.most_common(15)
    proteins = [protein for protein, _ in top_proteins]
    counts = [count for _, count in top_proteins]
    plt.bar(proteins, counts)
    plt.xlabel('Proteinski accession broj')
    plt.ylabel('Broj pojavljivanja')
    plt.title('Top 15 najzastupljenijih proteina')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig('protein_distribution.png')

    # 3. Graf distribucije peptida
    plt.figure(figsize=(14, 7))
    top_peptides = peptide_counts.most_common(10)
    peptides = [peptide[:10] + '...' if len(peptide) > 10 else peptide
                for peptide, _ in top_peptides]
    counts = [count for _, count in top_peptides]
    plt.bar(peptides, counts)
    plt.xlabel('Peptidna sekvenca')
    plt.ylabel('Broj pojavljivanja')
    plt.title('Top 10 najčešćih peptidnih sekvenci')
    plt.tight_layout()
    plt.savefig('peptide_distribution.png')

if __name__ == "__main__":
    pik_file = '/Users/tag/IdeaProjects/DigestedProteinDB/python/misc/Trypsin_HTXdigest-imaging ana.txt'
    analyze_ms_data(pik_file, 10)
