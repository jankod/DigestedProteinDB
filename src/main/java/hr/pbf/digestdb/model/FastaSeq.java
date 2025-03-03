package hr.pbf.digestdb.model;

import java.io.Serial;
import java.io.Serializable;

public class FastaSeq implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private DigestInformation digestInformation;

		public String header;
	public String seq;

	public FastaSeq(String header, String seq) {
		this.header = header;
		this.seq = seq;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((seq == null) ? 0 : seq.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FastaSeq other = (FastaSeq) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (seq == null) {
            return other.seq == null;
		} else return seq.equals(other.seq);
    }

	public FastaSeq() {
	}

	@Override
	public String toString() {
		if (seq != null && seq.length() > 30) {
			return header + "\n" + seq.subSequence(0, 29) + " ...";
		}
		return header + "\n" + seq;
	}



	/**
	 * Na svakih 40 znakova dodaje novi red
	 *
	 * @param seq2
	 * @return
	 */
	public static String formatSeq(String seq, String newLineChar) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < seq.length(); i++) {
			if (i % 80 == 0 && i != 0) {
				b.append(newLineChar);
			}
			b.append(seq.charAt(i));
		}
		return b.toString();
	}

	public void setDigestInformation(DigestInformation digestInformation) {
		this.digestInformation = digestInformation;
	}

	public DigestInformation getDigestInformation() {
		return digestInformation;
	}

	/**
	 * Informacije o fitanju peptida sa masom prekursora
	 *
	 * @author ja
	 *
	 */
	public static class DigestInformation {
		/**
		 * Peptid koji se isao razlomiti i izracunati mase.
		 */
		public String peptide;

		/**
		 * Razlomljeni dio peptida koji se fita sa masom na masu prekursora
		 */
		public String peptideThatFitsMass;

		/**
		 * Masa peptida koja pase +-
		 */
		public double massFit;
	}
}
