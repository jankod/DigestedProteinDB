package hr.pbf.digestdb.util;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import hr.pbf.digested.proto.Peptides;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@UtilityClass
public class BinaryPeptideDbUtil {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024); // 1MB, prilagodite po potrebi

    public void writeVarint(ByteBuffer buffer, int value) {
        while (value >= 128) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }

    public static int readVarint2(ByteBuffer buffer) throws IOException {
        CodedInputStream cis = CodedInputStream.newInstance(buffer);
        return cis.readRawVarint32(); // Za 32-bitni Varint
        // Ili koristite cis.readRawVarint64() za 64-bitni Varint ako je potrebno
    }

    public int readVarint(ByteBuffer buffer) {
        int result = 0;
        int shift = 0;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            result |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                return result;
            }
            if (shift >= 32) {
                throw new IllegalStateException("Varint to large!");
            }
        }
        throw new IllegalStateException("Incomplete varint in buffer!");
    }

    public Set<PeptideAcc> readGroupedRow(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        Set<PeptideAcc> peptides = new HashSet<>();

        while (buffer.hasRemaining()) {
            // Čitanje dužine sekvence
            int seqLength = readVarint(buffer);
            byte[] seqBytes = new byte[seqLength];
            buffer.get(seqBytes);
            //String sequence =AminoAcidCoder.decodePeptideByteBuffer(seqBytes, seqLength);
            String sequence = new String(seqBytes, StandardCharsets.UTF_8);

            // Čitanje broja akcesija
            int accessionCount = readVarint(buffer);
            List<Integer> accessions = new ArrayList<>(accessionCount);
            for (int i = 0; i < accessionCount; i++) {
                int accession = readVarint(buffer);
                accessions.add(accession);
            }

            // Kreiranje PeptideAcc objekta i dodavanje u listu
            PeptideAcc acc = new PeptideAcc();
            acc.seq = sequence;
            acc.accessions = new int[accessions.size()];
            for (int i = 0; i < accessions.size(); i++) {
                acc.accessions[i] = accessions.get(i);
            }
            peptides.add(acc);
        }

        return peptides;
    }

    public byte[] writeGroupedRow(String value) {
        buffer.clear();
        int start = 0;
        while (start < value.length()) {
            int colonIndex = value.indexOf(':', start);
            String seq = value.substring(start, colonIndex);
            int dashIndex = value.indexOf('-', colonIndex);
            if (dashIndex == -1) dashIndex = value.length();
            String[] accessions = value.substring(colonIndex + 1, dashIndex).split(";");


            // sequence
            // byte[] seqBytes = AminoAcidCoder.encodePeptideByteBuffer(seq);
            writeVarint(buffer, seq.length()); // 4 bajta za dužinu sekvence
            buffer.put(seq.getBytes(StandardCharsets.UTF_8));      // Sekvenca


            buffer.put((byte) accessions.length); // 1 bajt za broj access
            for (String acc : accessions) {
                writeVarint(buffer, Integer.parseInt(acc));
            }
            start = dashIndex + 1;
        }
        return Arrays.copyOf(buffer.array(), buffer.position());
    }


    public static class PeptideAcc implements Comparable<PeptideAcc> {
        String seq;
        int[] accessions;

        @Override
        public int compareTo(@NotNull PeptideAcc o) {
            if (seq.equals(o.seq)) {
                return 0;
            }
            return seq.compareTo(o.seq);
        }
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();

        BinaryPeptideDbUtil db = new BinaryPeptideDbUtil();
        String s = "ASELTGEKDLANSSLR:1292347-ASSSLSGGADTLEALGVR:1402905;1402906-LTVTDNNGGINTESKK:209275-NTVISTGGGIVETEASR:107327-KLSDTEINEQISGTR:221885-NGATSGLTSEEELRVK:274454-TIETRNGEVVTESQK:725806-TAEIEGISAAGATKESR:1557621-STANSVKSELEQELR:1198065-EREEELASSTATVIR:574058-DNDVLLASSSRDATVK:276611-TNELAGDGTTTATVLAR:283725-NGDGTITGKELSETLR:2346334-DDVTGATKALLTGASDR:58722-DIATSLSQASGEKIDR:257619-EVSVNTGATDGAITSIR:212964-VAVSSGEDGSDTVLKAR:361048-ASASASVIVPSNQGTSSK:535175-DSSIIGKNNVNSDLSK:233125-NATQAIDEAISSTLTR:1291643;1291594-LVTTDSESIREDIGR:99998";
        StopWatch watch = new StopWatch();
        watch.start();
        byte[] bytes = db.writeGroupedRow(s);
        watch.stop();
        log.debug("write Optimized Time: " + watch.getNanoTime());

        log.debug("Bytes length : " + bytes.length);
        log.debug("String length:" + s.length());

        int maxCompressedLength = compressor.maxCompressedLength(bytes.length);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(bytes, 0, bytes.length, compressed, 0, maxCompressedLength);
        log.debug("Compressed length: " + compressedLength);


        byte[] compressString = compressor.compress(s.getBytes(StandardCharsets.UTF_8));
        log.debug("Compressed string length: " + compressString.length);

        watch.reset();
        watch.start();
        Set<PeptideAcc> result = db.readGroupedRow(bytes);
        watch.stop();
        log.debug("Read Optimized Time: " + watch.getNanoTime());

        for (PeptideAcc peptideAcc : result) {
            System.out.println(peptideAcc.seq + " " + Arrays.toString(peptideAcc.accessions));
        }

        Peptides.SeqAccessionList.Builder builder = Peptides.SeqAccessionList.newBuilder();
        for (PeptideAcc peptideAcc : result) {
            Peptides.SeqAccessions.Builder seqAccession = Peptides.SeqAccessions.newBuilder();
            seqAccession.setSeq(peptideAcc.seq);
            for (int accession : peptideAcc.accessions) {
                seqAccession.addAccessions(accession);
            }
            builder.addSequences(seqAccession);

        }
        watch.reset();
        watch.start();
        byte[] byteArray = builder.build().toByteArray();
        watch.stop();
        log.debug("Protobuf Time: " + watch.getNanoTime());
        log.debug("Protobuf size: " + byteArray.length);
        Peptides.SeqAccessionList.parseFrom(byteArray).getSequencesList().forEach(System.out::println);

    }
}
