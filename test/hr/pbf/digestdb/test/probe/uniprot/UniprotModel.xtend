package hr.pbf.digestdb.test.probe.uniprot

import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors

class UniprotModel {

	def static void main(String[] args) {
	}
}

interface CallbackUniprotReader {
	def void readEntry(EntryUniprot e)
}


/**
 * UniProtKB accession numbers consist of 6 or 10 alphanumerical characters in the format:
 
 */
class EntryUniprot {
	@Accessors val accessions = new ArrayList<String>()
	@Accessors int tax;
	@Accessors StringBuilder seq = new StringBuilder()
	@Accessors String protName

}

class Tax {
	
	new(int taxId, String desc) {
		this.taxId = taxId
		this.desc = desc
	}
	
	@Accessors int taxId
	@Accessors String desc
	
	override toString() {
		return taxId + " "+ desc
	}
	
}
