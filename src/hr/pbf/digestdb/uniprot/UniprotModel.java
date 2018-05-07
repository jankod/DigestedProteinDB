package hr.pbf.digestdb.uniprot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public class UniprotModel {

    public static interface CallbackUniprotReader {
        void readEntry(EntryUniprot e);
    }

    @Data
    @Accessors
    public static class Tax {
        private int taxId;
        private String desc;

    }

    @Data
    @Accessors
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntryUniprot {
        private String accession;
        private int tax;
        private String protName;
        private StringBuilder seq = new StringBuilder(200);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public final static class AccTax {
        public String acc;
        public int tax;

        public String toString() {
            return acc + ":" + tax;
        }

    }

    /**
     * CSV line
     *
     * @author tag
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public final static class PeptideMassAccTaxList {
        private String peptide;
        private float mass;
        private List<AccTax> accTaxs;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public final static class PeptideAccTaxMass {
        private String peptide;
        private String acc;
        private int tax;
        private float mass;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public final static class PeptideAccTaxNames {
        private float mass;
        private String peptide;
        private String acc;
        private String protName;
        private String taxName;
        private int tax;

        @Override
        public String toString() {
            return "[mass=" + mass + ", peptide=" + peptide + ", acc=" + acc + ", protName=" + protName + ", taxName="
                    + taxName + ", tax=" + tax + "]";
        }

    }

    // @Accessors
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public final static class PeptideAccTax implements KryoSerializable {
        private String peptide;
        private String acc;
        private int tax;

        @Override
        public void write(Kryo kryo, Output o) {
            o.writeAscii(peptide);
            o.writeAscii(acc);
            o.writeInt(tax);
        }

        @Override
        public void read(Kryo kryo, Input i) {
            peptide = i.readString();
            acc = i.readString();
            tax = i.readInt();
        }

    }


    /**
     * ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/README
     *
     * Entries are attributed to these files as follows:
     * Every entry is present in exactly one file.
     * *human.dat         contains all human entries
     * *mammals.dat       contains all mammalian entries except those from human and rodents
     * *vertebrates.dat   contains all vertebrate entries except those from mammals
     * *invertebrates.dat contains all eukaryotic entries except those from vertebrates, fungi and plants
     * <code>
     * 15. Archaea
     * 0. Bacteria
     * 16. Fungi  -> plants
     * 17. Human
     * 18. Invertebrates
     * 2. Mammals
     * 4. Plants
     * 6. Rodents
     * 8. Unclassified
     * 10. Vertebrates
     * 9. Viruses
     * </code>
     */
    public enum DIVISION {
        Archaea(15),
        Bacteria(0),
        Fungi(16),
        Human(17),
        Invertebrates(18),
        Mammals(2),
        Plants(4),
        Rodents(6),
        Unclassified(8),
        Vertebrates(10),
        Viruses(9);

        private int idDB;

        /**
         * ID from database
         *
         * @param idDB
         */
        DIVISION(int idDB) {
            this.idDB = idDB;
        }

        public int getIdDB() {
            return idDB;
        }
    }

    public static class KryoFloatHolder implements KryoSerializable {
        private static final Logger log = LoggerFactory.getLogger(UniprotModel.KryoFloatHolder.class);

        private List<PeptideAccTax> data = new ArrayList<>();

        public KryoFloatHolder() {

        }

        public KryoFloatHolder(List<PeptideAccTax> data) {
            this.data = data;
        }

        @Override
        public void write(Kryo kryo, Output o) {
            o.writeInt(data.size());
            // TODO: sort by mass before

            for (PeptideAccTax p : data) {

                o.writeAscii(p.getPeptide());
            }
            for (PeptideAccTax p : data) {
                o.writeAscii(p.getAcc());
            }
            for (PeptideAccTax p : data) {
                o.writeInt(p.getTax());
            }
        }

        @Override
        public void read(Kryo kryo, Input i) {
            int howMany = i.readInt();
            data = new ArrayList<>(howMany);
            for (int j = 0; j < howMany; j++) {
                // log.debug("reqad");
                PeptideAccTax p = new PeptideAccTax();
                data.add(j, p);
                p.setPeptide(i.readString());
            }

            for (int j = 0; j < howMany; j++) {
                data.get(j).setAcc(i.readString());
            }
            for (int j = 0; j < howMany; j++) {
                data.get(j).setTax(i.readInt());
            }
        }

        public List<PeptideAccTax> getData() {
            return data;
        }

        public void setData(List<PeptideAccTax> data) {
            this.data = data;
        }

    }

}
