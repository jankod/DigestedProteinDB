# Digested Peptide Database

## Overview

This is a compact and efficient database of in-silico digested peptides, developed with Java. The primary goal is to
support rapid mass-based searches, even on less powerful desktop machines.

This compact database layout enables fast peptide mass queries with minimal resource usage. For example, a Uniprot
TrEMBL bacteria dataset (7 to 30 amino acids, 1 missed cleavage) occupies about 50 GB on disk and supports quick lookups
by mass range on standard hardware.

## Key Features

- **Minimal and Fast database**: Database of digested peptides designed for a small on\-disk footprint
- **Mass\-Oriented Search**: Stores peptide data keyed by mass to expedite lookups based on precise mass ranges.

## Created Peptide Database

- [x] Uniprot TrEMBL bacteria dataset. ~50GB , 7 to 30 amino acids, 1 miss cleavage.
    - Download ...
- [ ] Uniprot TrEMBL human dataset. (NOT FINISHED).

## Workflow for creating the database

1. **Parse Uniprot Data**: Extract sequences and relevant metadata from uniprot .xml.gz database and NCBI Taxonomy.
2. **Digest Proteins**: Cleave the protein with a specific enzyme (trypsin, for example).
3. **Organize Data**: Sort and group peptides by mass for easy storage.
4. **Build Database**: Generate a mass\-indexed key\-value engine (RocksDB) and custom solution for quick searches.

## Usage

- Run web server to access the database....
- Run database creation classes...

## TODO

- Implement a simple command line interface for querying the database.
- Create new database for additional proteins, enzymes and missed cleavages.
- Create website for the project with:
    - Documentation
    - Downloadable databases
    - Benchmarks
