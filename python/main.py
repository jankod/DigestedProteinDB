# ete3

from ete3 import NCBITaxa
ncbi = NCBITaxa()
ncbi.update_taxonomy_database()


# npr. želimo info za taxid 9606 (human)
print(ncbi.get_taxid_translator([9606]))
print(ncbi.get_lineage(9606))
print(ncbi.get_rank(ncbi.get_lineage(9606)))

# https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping_selected.tab.gz

