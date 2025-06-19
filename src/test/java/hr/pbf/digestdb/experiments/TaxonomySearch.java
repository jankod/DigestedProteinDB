package hr.pbf.digestdb.experiments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@UtilityClass
public class TaxonomySearch {


    public static void main(String[] args) {
        double mass1 = 1600.0;
        double mass2 = 1600.1;

        try {
            TaxonomySearchResult result = searchTaxonomyWeb(mass1, mass2);
            System.out.println("Total results: " + result.totalResult);
            System.out.println("Duration: " + result.duration);
            System.out.println("Memory: " + result.memory);

            // Process the first few results as an example
            AtomicInteger count = new AtomicInteger();

            result.result.iterator().forEachRemaining(massEntry -> {


                massEntry.keySet().forEach(mass -> {
                    System.out.println("Mass: " + mass);
                    massEntry.get(mass).forEach(peptide -> {
                        count.getAndIncrement();
                        System.out.println("  Sequence: " + peptide.seq);
                        System.out.println("  Accessions: " + peptide.accsTax.size());
                        peptide.accsTax.forEach(accTax -> {
                            System.out.println("    Accession: " + accTax.acc);
                            System.out.println("    Taxonomy IDs: " + accTax.taxIds);
                        });
                    });

                });
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
    }


    public TaxonomySearchResult searchTaxonomyWeb(double mass1, double mass2) throws IOException, InterruptedException {
        String url = "http://digestedproteindb.pbf.hr:7071/search-taxonomy?mass1=" +
                     mass1 + "&mass2=" + mass2 + "&pageSize=1000";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .GET()
              .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Request failed with status code: " + response.statusCode());
        }
        client.close();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.body(), TaxonomySearchResult.class);
    }

// Model classes for JSON deserialization

    public class TaxonomySearchResult {
        @JsonProperty("totalResult")
        public int totalResult;

        @JsonProperty("memory")
        public String memory;

        @JsonProperty("duration")
        public String duration;

        @JsonProperty("page")
        public int page;

        @JsonProperty("pageSize")
        public int pageSize;

        @JsonProperty("result")
        public List<Map<String, List<Peptide>>> result = new ArrayList<>();
    }

    public class Peptide {
        @JsonProperty("seq")
        public String seq;

        @JsonProperty("accsTax")
        public List<AccessionTaxonomy> accsTax = new ArrayList<>();
    }

    public class AccessionTaxonomy {
        @JsonProperty("acc")
        public String acc;

        @JsonProperty("taxIds")
        public List<Integer> taxIds = new ArrayList<>();
    }
}
