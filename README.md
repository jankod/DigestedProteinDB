# Digested Peptide Database

## Overview
This is a scientific project aiming to create a compact and efficient database of in\-silico digested peptides. The primary goal is to support rapid mass\-based searches, even on less powerful desktop machines.

## Key Features
- **Minimal and Fast**: Designed for a small on\-disk footprint \(e\.g\. ~50GB for a Uniprot TrEMBL bacteria dataset\) and quick lookups by mass only.  
- **Digesting Proteins**: Uses trypsin with customizable peptide length \(e\.g\. 7 to 30 amino acids\) and a single missed cleavage.  
- **Mass\-Oriented Search**: Stores peptide data keyed by mass to expedite lookups based on precise mass ranges.  

## Workflow Outline
1. **Parse Uniprot Data**: Extract sequences and relevant metadata from uniprot .xml.gz database and NCBI Taxonomy.  
2. **Digest Proteins**: Cleave the protein with a specific enzyme (trypsin, for example).
3. **Organize Data**: Sort and group peptides by mass for easy storage.  
4. **Build Database**: Generate a mass\-indexed key\-value engine for quick searches.  

## Usage
- Run the main classes after adjusting paths in the source files.
- Create directory `db_name` and place the input files in it.
- Update configuration in `db_name/workflow.properties` for your inputs.
- Final databases can be queried by specifying a mass range to retrieve matching peptides.

## Notes
This compact database layout enables rapid peptide mass queries with lightweight resource usage. 
For example, a Uniprot TrEMBL bacteria dataset \(7 to 30 amino acids, 1 miss cleavage\) occupies about 50GB on disk and supports fast lookups on standard hardware.
