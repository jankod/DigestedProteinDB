package hr.pbf.digestdb.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class EntrezUtil {
    private static final Logger log = LoggerFactory.getLogger(EntrezUtil.class);

    public static String getTaxid(String acc) throws MalformedURLException, IOException {
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&rettype=fasta&retmode=xml&id="
                + acc;

        String xml = org.apache.commons.io.IOUtils.toString(new URL(url), Charsets.UTF_8);
        String taxid;
        try {
            taxid = xml.substring(xml.indexOf("<TSeq_taxid>") + 12, xml.indexOf("</TSeq_taxid>"));

            return taxid;
        } catch (Throwable e) {
            log.debug("Nisam nasao za: '" + acc + "' " + e.getMessage());
            //e.printStackTrace();
            return "-1";
        }
    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        String tax = getTaxid("4HE8_C");
        System.out.println(tax);
    }
}
