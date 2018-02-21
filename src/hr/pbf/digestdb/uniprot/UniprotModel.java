package hr.pbf.digestdb.uniprot;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
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
	public static class EntryUniprot {
		private String accession;
		private int tax;
		private String protName;
		private StringBuilder seq = new StringBuilder(200);

	}

	public static class KryoFloatHolder implements KryoSerializable {

		private List<PeptideAccTax> data = new ArrayList<>();

		public KryoFloatHolder() {
			
		}
		public KryoFloatHolder(List<PeptideAccTax> data) {
			this.data = data;
		}

		@Override
		public void write(Kryo kryo, Output o) {
			o.writeInt(data.size());
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

	// @Accessors
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PeptideAccTax implements KryoSerializable {
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

}
