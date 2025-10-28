package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.LongCounter;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class CalculateUniqePeptideInTax {

    @SneakyThrows
    public static void main(String[] args) throws NcbiTaxonomyException {
        // /disk4/janko/trembl_5_60_2_db/gen/mass_pep_acc_sorted.csv
//        String ncbiTaxonomyPath = "";
        //NcbiTaksonomyRelations ncbiTaksonomyRelations = NcbiTaksonomyRelations.loadTaxonomyNodes(ncbiTaxonomyPath);


        // Stavio sam na Biopro u /home/tag/sheep_butorka_taxonomy_hitst.zip


        System.out.println("Loading accessionTaxDb ...");
        AccTaxDB accessionTaxDb = AccTaxDB.loadFromDiskCsv("/disk4/janko/trembl_5_60_2_db/acc_taxid.csv");

        // taxid, taxName,  protein_count, peptide_count
        // 2026735,Deltaproteobacteria bacterium,1122121,7541735
        //1978231,Acidobacteriota bacterium,1238839,7277737
        //1913989,Gammaproteobacteria bacterium,1461246,7254585
        //2026724,Chloroflexota bacterium,1529952,7082667
        //2026780,Planctomycetota bacterium,685039,5377336
        //1913988,Alphaproteobacteria bacterium,748684,4281512

 //       String rezultatiButorka = "/Users/tag/Downloads/sheep_hits.duckdb_result.csv";


        String mass_pep_acc = "/disk4/janko/trembl_5_60_2_db/gen/mass_pep_acc_sorted.csv";

        //mass,peptide,accession
        // -------------------------
        //360.1393,GGGGGG,A0A059LG35
        //360.1393,GGGGGG,A0A061H1N9
        //360.1393,GGGGGG,A0A078F6K7
        //360.1393,GGGGGG,A0A087UKN9
        //360.1393,GGGGGG,A0A094AQL6
        //360.1393,GGGGGG,A0A0A9EDP8
        //360.1393,GGGGGG,A0A0A9F498
        //360.1393,GGGGGG,A0A0A9XMY7


        // napravi CSV sa kolonama taxID, peptide_count

        Int2IntMap taxIdPeptideCount = new it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap();

        try (BufferedReader in = new BufferedReader(new FileReader(mass_pep_acc))) {
            String line = null;
            while ((line = in.readLine()) != null) {

                String accession = line.split(",")[2];
             //   long accLong = hr.pbf.digestdb.util.MyUtil.toAccessionLong36(accession);
                int taxId = accessionTaxDb.getTaxonomyId(accession);

                taxIdPeptideCount.put(taxId, taxIdPeptideCount.getOrDefault(taxId, 0) + 1);

            }

        }
        // write to disk
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter("/disk4/janko/trembl_5_60_2_db/peptide_count_per_taxid.csv"))) {
            writer.write("taxId,peptide_count\n");
            for (Int2IntMap.Entry entry : taxIdPeptideCount.int2IntEntrySet()) {
                writer.write(entry.getIntKey() + "," + entry.getIntValue() + "\n");
            }

        }
// janko ./jdk-24.0.2/bin/java -jar  ./DigestedProteinDB-tools-jar-with-dependencies.jar
        System.out.println("Done");

    }
}
