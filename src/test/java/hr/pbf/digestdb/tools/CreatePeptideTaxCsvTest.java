package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.SearchWeb;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CreatePeptideTaxCsvTest {


    @org.junit.jupiter.api.Test
    public void testUniqueTax() {
        ArrayList<SearchWeb.AccTaxs> accsTax = new ArrayList<>();
        accsTax.add(new SearchWeb.AccTaxs("P12345", 343));
        accsTax.add(new SearchWeb.AccTaxs("P12345", 343));
        accsTax.add(new SearchWeb.AccTaxs("P12345", 9606));
        accsTax.add(new SearchWeb.AccTaxs("Q12345", 9606));
        accsTax.add(new SearchWeb.AccTaxs("Q12345", 10090));
        accsTax.add(new SearchWeb.AccTaxs("Q12345", 10090));
        String taxList = CreatePeptideTaxCsv.toTaxList(accsTax);
        assertEquals("343;9606;10090", taxList);
    }

}
