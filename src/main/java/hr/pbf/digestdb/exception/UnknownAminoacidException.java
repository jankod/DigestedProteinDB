package hr.pbf.digestdb.exception;

import lombok.Data;

@Data
public class UnknownAminoacidException extends RuntimeException {
    private final String aminoacid;
    private final String sequence;

    public UnknownAminoacidException(String aminoacid, String sequence) {
        super("Unknown aminoacid: " + aminoacid);
        this.aminoacid = aminoacid;
        this.sequence = sequence;
    }
}
