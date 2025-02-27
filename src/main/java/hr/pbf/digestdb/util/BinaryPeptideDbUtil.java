package hr.pbf.digestdb.util;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import hr.pbf.digested.proto.Peptides;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@UtilityClass
public class BinaryPeptideDbUtil {

    private static ByteBuffer bufferCache = ByteBuffer.allocate(1024 * 1024 * 32); // 32MB, prilagodite po potrebi


    public static int readVarint2(ByteBuffer buffer) throws IOException {
        CodedInputStream cis = CodedInputStream.newInstance(buffer);
        return cis.readRawVarint32(); // Za 32-bitni Varint
        // Ili koristite cis.readRawVarint64() za 64-bitni Varint ako je potrebno
    }

    public void writeVarint(ByteBuffer buffer, int value) {
        while (value >= 128) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }

    public void writeVarInt(DataOutputStream dos, int value) throws IOException {
         while (value >= 128) {
            dos.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        dos.writeByte((byte) value);
    }

    public int readVarInt(DataInputStream din) throws IOException {
        int result = 0;
        int shift = 0;
        while (true) {
            byte b = din.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                return result;
            }
            if (shift >= 32) {
                throw new IllegalStateException("Varint to large!");
            }
        }
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
        throw new IllegalStateException("Incomplete varint in buffer! Buffer: "+ buffer);
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
        try {
            bufferCache.clear();
            int start = 0;
            while (start < value.length()) {
                int colonIndex = value.indexOf(':', start);
                String seq = value.substring(start, colonIndex);
                int dashIndex = value.indexOf('-', colonIndex);
                if (dashIndex == -1) dashIndex = value.length();
                String[] accessions = value.substring(colonIndex + 1, dashIndex).split(";");


                // sequence
                // byte[] seqBytes = AminoAcidCoder.encodePeptideByteBuffer(seq);
                byte[] seqBytes = seq.getBytes(StandardCharsets.UTF_8);
                // large: TVDRPTK
                ensureCapacity(bufferCache, seqBytes.length + 5); // 4 bytes for length + 1 byte for data
                writeVarint(bufferCache, seq.length()); // 4 bajta za dužinu sekvence

                bufferCache.put(seqBytes);


                //buffer.put((byte) accessions.length); // 1 bajt za broj access
                writeVarint(bufferCache, accessions.length); // 4 bajta za broj access
                for (String acc : accessions) {
                    writeVarint(bufferCache, Integer.parseInt(acc));
                }
                start = dashIndex + 1;
            }
            return Arrays.copyOf(bufferCache.array(), bufferCache.position());
        } catch (Exception e) {
            log.error("Error on line: " + StringUtils.truncate(value, 20_000), e);
            throw e;
        }
    }

    private void ensureCapacity(ByteBuffer buff, int additionalCapacity) {
        if (buff.remaining() < additionalCapacity) {
            ByteBuffer newBuffer = ByteBuffer.allocate((int) (buff.capacity() * 1.2 + additionalCapacity));
            buff.flip();
            newBuffer.put(buff);
            bufferCache = newBuffer;
        }
    }

    @Data
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

        @Override
        public String toString() {
            return seq + " " + Arrays.toString(accessions);
        }
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();

        String s = "ASELTGEKDLANSSLR:1292347-ASSSLSGGADTLEALGVR:1402905;1402906-LTVTDNNGGINTESKK:209275-NTVISTGGGIVETEASR:107327-KLSDTEINEQISGTR:221885-NGATSGLTSEEELRVK:274454-TIETRNGEVVTESQK:725806-TAEIEGISAAGATKESR:1557621-STANSVKSELEQELR:1198065-EREEELASSTATVIR:574058-DNDVLLASSSRDATVK:276611-TNELAGDGTTTATVLAR:283725-NGDGTITGKELSETLR:2346334-DDVTGATKALLTGASDR:58722-DIATSLSQASGEKIDR:257619-EVSVNTGATDGAITSIR:212964-VAVSSGEDGSDTVLKAR:361048-ASASASVIVPSNQGTSSK:535175-DSSIIGKNNVNSDLSK:233125-NATQAIDEAISSTLTR:1291643;1291594-LVTTDSESIREDIGR:99998";
        StopWatch watch = new StopWatch();
        watch.start();
        byte[] bytes = writeGroupedRow(s);
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
        Set<PeptideAcc> result = readGroupedRow(bytes);
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
