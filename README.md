# Digested Peptide Database

## Overview

DigestedProteinDB is a database that enables researchers in the field of proteomics to search for enzymatically digested
peptides by mass, sequence, and m/z values.

A Java library for creating and searching a digested peptide database with RocksDB key-value store.
Digested peptides are generated from protein sequences using a specific enzyme (e.g., trypsin) and are stored in a
database for rapid mass-based searches. This library is designed to be a compact and efficient storage of
in-silico digested peptides.

The database is designed to be a compact and efficient storage of in-silico digested peptides. The primary goal is to
support rapid mass-based searches, even on less powerful desktop machines.

https://digestedproteindb.pbf.hr

This compact database layout enables fast peptide mass queries with minimal resource usage. For example, a Uniprot
TrEMBL bacteria dataset (7 to 30 amino acids, 1 missed cleavage) occupies about 50 GB on disk and supports quick lookups
by mass range on standard hardware.

For example, searching for masses from: `1500.6` Da to `1500.8` Da gives JSON ptmSearchResults like:

**acc** — Uniprot accession number
**seq** - peptide sequence,
**mass** - peptide mass.

```json
{
  "totalResult": 1543,
  "memory": "2016 MB",
  "duration": "00:00:00.031",
  "page": 1,
  "pageSize": 10,
  "result": [
    {
      "1500.6": [
        {
          "seq": "SSNESGSSGSMTNEK",
          "acc": [
            "A0A085MEL6"
          ]
        },
        {
          "seq": "CSGSTSESEGSQTNK",
          "acc": [
            "A0AAD6LB27",
            "A0AAD6L9P6",
            "A0A4U5R0T2",
            "A0A8X8BWN4",
            "A0A8X8C0W2"
          ]
        },
        {
          "seq": "NSCSASESQSDTGTK",
          "acc": [
            "A0A3P9Q6N9"
          ]
        },
        {
          "seq": "EDNSCSSSAGSTQTK",
          "acc": [
            "A0AAV4GC55"
          ]
        }
      ]
    },
    {
      "1500.6002": [
        {
          "seq": "VEIEYDDEDMMI",
          "acc": [
            "A0A1Y2LUM3"
          ]
        },
        {
          "seq": "CHGWGGSQCHHHR",
          "acc": [
            "A0A8B9NSG9"
          ]
        }
      ]
    },
    ... 
``` 

## Key Features

**Minimal and fast database**: Database of digested peptides designed for a small on\-disk footprint
**Mass\-oriented search**: Stores peptide data keyed by mass to expedite lookups based on precise mass ranges

## Library Integration

This database is designed as a Java library that can be easily embedded into existing software applications.
The library provides a clean API for programmatic access to peptide data, making it ideal for integration with mass
spectrometry
analysis tools or custom bioinformatics pipelines.

## Installation

The project can be built using Maven and Java 24.
To build the project, navigate to the root directory of the project and run:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory.

## Requirements

- Java 24 or higher
- Maven 3.8 or higher for building the project from source
- Linux, MacOS for database building
- Linux, MacOS, Windows for running the database search server

## Command-Line Parameters

The application provides two main commands: `create-db` for creating the database and `server` for running the web
server. Below is a detailed explanation of the parameters for each command.

### `create-db` Command

This command is used to create a digested peptide database.
The following table describes the available parameters:

| Parameter                       | Description                                                                                            | Required | Default Value |
|---------------------------------|--------------------------------------------------------------------------------------------------------|----------|---------------|
| `-d`, `--db-dir`                | Path to the database directory.                                                                        | Yes      | None          |
| `-u`, `--uniprot-xml`           | Path to the Uniprot XML file (`.xml.gz` or `.xml`).                                                    | Yes      | None          |
| `-n`, `--db-name`               | Name of the database.                                                                                  | Yes      | None          |
| `-c`, `--clean`                 | Clean all files in the database directory before creating.                                             | No       | `false`       |
| `-m`, `--min-length`            | Minimum peptide length.                                                                                | No       | `7`           |
| `-M`, `--max-length`            | Maximum peptide length.                                                                                | No       | `30`          |
| `-mc`, `--missed-cleavage`      | Number of missed cleavages.                                                                            | No       | `1`           |
| `-s`, `--sort-temp-dir`         | Path to the temporary directory used for sorting.                                                      | No       | None          |
| `-ncbi`, `--ncbi-taxonomy-path` | Path to the NCBI taxonomy file (`nodes.dmp`). https://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz | No       | None          |
| `-p`, `--taxonomy-parents`      | NCBI Taxonomy parent (ancestor) IDs.                                                                   | No       | All           |
| `-e`, `--enzyme`                | Enzyme used for digestion (e.g., Trypsin, Chymotrypsin).                                               | No       | `Trypsin`     |
| `-t`, `--taxonomy-division`     | NCBI Taxonomy division to filter proteins (e.g., Bacteria, Viruses, etc.).                             | No       | `ALL`         |

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
-p 10239 -p 33090 
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

Demonstration search web: https://digestedproteindb.pbf.hr

## Create database steps

The database creation process starts with the uniprot.xml.gz (or .xml) file.
Example class `hr.pbf.digestdb.CreateDatabase` is used to create the database.

The process consists of the following steps:

1. **Parse Uniprot Data**: Extract sequences and relevant metadata from uniprot .xml.gz database and NCBI Taxonomy.
2. **Digest Proteins**: Cleave the protein with a specific enzyme (trypsin, for example).
3. **Organize Data**: Sort and group peptides by mass for easy storage.
4. **Build Database**: Generate a mass\-indexed key\-value engine (RocksDB) and custom array list index for quick
   searches.

Many optimizations are used to speed up the process, including:

- **Custom Data Structures**: Use specialized data structures for storing data. Sequences are stored as 5-bit encoded
  strings, masses are stored as 4-byte integers, and accession numbers are 36-base encoded strings stored in 64-bit
  data (long) and mapped to 4-byte integers.

## TODO

- Python code for searching the database
- Expand and improve documentation
- Create more downloadable databases with different enzymes and taxonomy units
- Develop native version for Windows, MacOS, and Linux without Java and RockDB dependencies
