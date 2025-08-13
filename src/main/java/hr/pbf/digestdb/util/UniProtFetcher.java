package hr.pbf.digestdb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UniProtFetcher {

    public static String getProteinNameFromUniProt(String accession) {
        String urlString = String.format(
            "https://rest.uniprot.org/uniprotkb/%s.tsv?fields=protein_name",
            accession
        );

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000); // 10 sekundi
            conn.setReadTimeout(10_000);

            int status = conn.getResponseCode();
            if (status == 404) {
                return String.format("Accession '%s' not found in UniProt.", accession);
            }
            if (status != 200) {
                return String.format("HTTP error: %d for accession: %s", status, accession);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String header = reader.readLine();
                String line = reader.readLine();
                if (line != null) {
                    return line;
                } else {
                    return String.format("Not find protein name for accession: %s", accession);
                }
            }

        } catch (IOException e) {
            return String.format("Error connecting to UniProt: %s", e.getMessage());
        }
    }

    public static void main(String[] args) {
        String accession1 = "P0DTC2"; // Spike glycoprotein from SARS-CoV-2
        String name1 = getProteinNameFromUniProt(accession1);
        System.out.println("Accession: " + accession1);
        System.out.println("Ime proteina: " + name1);
        System.out.println("--------------------");

        String accession2 = "P02769"; // Beta-lactoglobulin from Ovis aries (Ovca)
        String name2 = getProteinNameFromUniProt(accession2);
        System.out.println("Accession: " + accession2);
        System.out.println("Ime proteina: " + name2);
    }
}
