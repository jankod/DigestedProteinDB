# **This project is currently in active development**

# Digested Peptide Database

## Overview

This is a Java library for creating and searching a digested peptide database with RocksDB key-value store.
Digested peptides are generated from protein sequences using a specific enzyme (e.g., trypsin) and are stored in a
database for rapid mass-based searches. This library is designed to be efficient and easy to use, with a focus on
performance.
The database is designed to be a compact and efficient storage of in-silico digested peptides. The primary goal is to
support rapid mass-based searches, even on less powerful desktop machines.

https://digestedproteindb.pbf.hr


This compact database layout enables fast peptide mass queries with minimal resource usage. For example, a Uniprot
TrEMBL bacteria dataset (7 to 30 amino acids, 1 missed cleavage) occupies about 50 GB on disk and supports quick lookups
by mass range on standard hardware.

Search example: masss from: `1900` Da to `1900.3`Da gives results like:
```json
{
  "totalResult": 318,
  "results": [
    {
      "mass": 1900.0012,
      "seq": "VSGSVSARTDVVVAGPGAGSK",
      "acc": [
        "A3PJ74"
      ]
    },
    {
      "mass": 1900.0013,
      "seq": "QLTDEVLALRLSENGSR",
      "acc": [
        "Q99Q94"
      ]
    },
    {
      "mass": 1900.0013,
      "seq": "SGVTQVEDIQVQVGRTGK",
      "acc": [
        "C1F7S7"
      ]
    },
    {
      "mass": 1900.0013,
      "seq": "LTLDDGVRQDLVSAGVSR",
      "acc": [
        "Q03WY1"
      ]
    },
    ...
    
  ] }
``` 
**acc** - Uniprot accession number, **seq** - peptide sequence, **mass** - peptide mass.


## Key Features

- **Minimal and Fast database**: Database of digested peptides designed for a small on\-disk footprint
- **Mass\-Oriented Search**: Stores peptide data keyed by mass to expedite lookups based on precise mass ranges

## Download Databases

- [x] Uniprot TrEMBL bacteria dataset. ~50GB , 7 to 30 amino acids, 1 miss cleavage.
    - Download ...
- [ ] Uniprot TrEMBL human dataset. (NOT FINISHED).

## Library Integration
This database is designed as a Java library that can be easily embedded into existing software applications. 
The library provides a clean API for programmatic access to peptide data, making it ideal for integration with mass spectrometry 
analysis tools or custom bioinformatics pipelines.


#### New database creation

`java -jar DigestedProteinDB-0.1-uber.jar create-db -c -d /path/to/db_bacteria_swisprot -u /path/to/uniprot_sprot_bacteria.xml.gz -n UniprotSwot_bacteria`

#### Start web server for searching the database

`java -jar DigestedProteinDB-0.1-uber.jar server --port 8080 --db-dir /path/to/db`


###
Demo web: https://digestedproteindb.pbf.hr/


## Create database workflow

The database is created from the uniprot .xml.gz or .xml file.
Example class `hr.pbf.digestdb.CreateDatabase` is used to create the database.

Steps to create the database:
1. **Parse Uniprot Data**: Extract sequences and relevant metadata from uniprot .xml.gz database and NCBI Taxonomy.
2. **Digest Proteins**: Cleave the protein with a specific enzyme (trypsin, for example).
3. **Organize Data**: Sort and group peptides by mass for easy storage.
4. **Build Database**: Generate a mass\-indexed key\-value engine (RocksDB) and custom solution for quick searches.

### Example build database on MacOS or Linux

Prepare uniprot xml protein as source data. The database is created from the uniprot .xml.gz or .xml file.
The uniprot xml.gz file can be downloaded from
the [uniprot FTP website](https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/).

put in folder file workflow.properties

```properties
# relative path to the uniprot xml file
uniprot_xml_path=src/uniprot_sprot_bacteria.xml.gz
min_peptide_length=7
max_peptide_length=30
miss_cleavage=1
db_name=Uniprot Swis-Prot bacteria
enzyme_name=Trypsine
#sort_temp_dir=/tmp/temp_dir
```


## TODO

- Implement a simple command line interface for querying the database.
- Create new database for additional proteins, enzymes and missed cleavages.
- Create website for the project with:
    - Documentation
    - Downloadable databases
    - Benchmarks


### 
