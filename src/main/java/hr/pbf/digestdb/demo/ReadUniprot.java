package hr.pbf.digestdb.demo;

import hr.pbf.digestdb.util.UniprotXMLParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class ReadUniprot {
	public static void main(String[] args) {
		String path = "/media/tag/D/digested-db/uniprot_sprot.xml.gz";
		UniprotXMLParser parser = new UniprotXMLParser();
		Set<String> divisions = new HashSet<>();
		parser.parseProteinsFromXMLstream(path, new UniprotXMLParser.ProteinHandler() {
			@Override
			public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
				UniprotXMLParser.Division div = UniprotXMLParser.Division.fromId(p.getDivisionId());
				divisions.add(div.getName());
			//	System.out.println("Got protein: " + p.getAccession() + " " + p.getDivisionId() + " " + UniprotXMLParser.Division.fromId(p.getDivisionId()).getName());
			}
		});
		System.out.println("Divisions: " + divisions);
	}
}
