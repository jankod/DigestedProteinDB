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

Search example: masses from: `1900` Da to `1900.3`Da gives JSON results like:

```json
{
  "totalResult": 645,
  "memory": "300 MB",
  "duration": "00:00:00.000",
  "page": 1,
  "pageSize": 10,
  "result": [
    {
      "1500.6014": [
        {
          "seq": "QADNFPFAMCDDK",
          "acc": [
            "Q1RIV2"
          ]
        }
      ]
    },
    {
      "1500.6038": [
        {
          "seq": "DSEDMDDTLASFR",
          "acc": [
            "F4JLS1"
          ]
        }
      ]
    },
    {
      "1500.6039": [
        {
          "seq": "GDSCFEESESGNLK",
          "acc": [
            "B4NSS7",
            "B4IMH3"
          ]
        }
      ]
    },
    {
      "1500.6046": [
        {
          "seq": "TCDDECCPVNFKK",
          "acc": [
            "P03313",
            "Q9YLG5"
          ]
        },
        {
          "seq": "TCDEDCCPVNFKK",
          "acc": [
            "O91734"
          ]
        }
      ]
    },
...
``` 

**acc** - Uniprot accession number, **seq** - peptide sequence, **mass** - peptide mass.

## Key Features

- **Minimal and Fast database**: Database of digested peptides designed for a small on\-disk footprint
- **Mass\-Oriented Search**: Stores peptide data keyed by mass to expedite lookups based on precise mass ranges

## Library Integration

This database is designed as a Java library that can be easily embedded into existing software applications.
The library provides a clean API for programmatic access to peptide data, making it ideal for integration with mass
spectrometry
analysis tools or custom bioinformatics pipelines.

#### New database creation

How to Run
Build the Project

Use Maven:
mvn clean package
Create the Database

## Command-Line Parameters

The application provides two main commands: `create-db` for creating the database and `server` for running the web
server. Below is a detailed explanation of the parameters for each command.

### `create-db` Command

This command is used to create a digested peptide database.

| Parameter                       | Description                                                           | Required | Default Value |
|---------------------------------|-----------------------------------------------------------------------|----------|---------------|
| `-d`, `--db-dir`                | Path to the database directory.                                       | Yes      | None          |
| `-u`, `--uniprot-xml`           | Path to the Uniprot XML file (`.xml.gz` or `.xml`).                   | Yes      | None          |
| `-n`, `--db-name`               | Name of the database.                                                 | Yes      | None          |
| `-c`, `--clean`                 | Clean all files in the database directory before creating.            | No       | `false`       |
| `-m`, `--min-length`            | Minimum peptide length.                                               | No       | `7`           |
| `-M`, `--max-length`            | Maximum peptide length.                                               | No       | `30`          |
| `-mc`, `--missed-cleavage`      | Number of missed cleavages.                                           | No       | `1`           |
| `-s`, `--sort-temp-dir`         | Path to the temporary directory used for sorting.                     | No       | None          |
| `-ncbi`, `--ncbi-taxonomy-path` | Path to the NCBI taxonomy file (`nodes.dmp`).                         | No       | None          |
| `-p`, `--taxonomy-parents`      | Taxonomy parent (ancestor) IDs.                                       | No       | All           |
| `-e`, `--enzyme`                | Enzyme used for digestion (e.g., Trypsin).                            | No       | `Trypsin`     |
| `-t`, `--taxonomy-division`     | Taxonomy division to filter proteins (e.g., Bacteria, Viruses, etc.). | No       | `ALL`         |

### Example Usage 
Run the compiled JAR with the create-db subcommand:

```bash
java -jar digestdb.jar create-db 
-d "path/to/db/dir" 
-u "path/to/uniprot/xml/file.xml.gz" 
-n myDatabaseName
-c 
-m 7 
-M 30 
-s path/to/sort/temp/dir 
-ncbi "path/to/ncbi/taxonomy/file" 
-p 10239,33090 
-e Trypsin 
-t ALL
```

### Run the Web Server
Start a simple web server for searching:
```bash
java -jar digestdb.jar server 
-p 8080 
-d "/path/to/db/dir"
```

Demoonstration search web: https://digestedproteindb.pbf.hr/

## Create database workflow

The database is created from the uniprot .xml.gz or .xml file.
Example class `hr.pbf.digestdb.CreateDatabase` is used to create the database.

Steps to create the database:

1. **Parse Uniprot Data**: Extract sequences and relevant metadata from uniprot .xml.gz database and NCBI Taxonomy.
2. **Digest Proteins**: Cleave the protein with a specific enzyme (trypsin, for example).
3. **Organize Data**: Sort and group peptides by mass for easy storage.
4. **Build Database**: Generate a mass\-indexed key\-value engine (RocksDB) and custom solution for quick searches.



## TODO

- Implement a simple command line interface for querying the database.
- Create new database for additional proteins, enzymes and missed cleavages.
- Create website for the project with:
    - Documentation
    - Downloadable databases
    - Benchmarks

###  
