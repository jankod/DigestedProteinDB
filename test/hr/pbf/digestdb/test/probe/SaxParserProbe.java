package hr.pbf.digestdb.test.probe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lombok.Data;

public class SaxParserProbe {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		parse("F:\\Downloads\\uniprot_sprot.xml\\uniprot_sprot.xml");
	}

	public static void parse(String sprotXmlPath) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		MyHandler handler = new MyHandler();
		saxParser.parse(new File(sprotXmlPath), handler);
	}
}

// @Data
class EntrySax {

	private static final Logger log = LoggerFactory.getLogger(EntrySax.class);

	int numRow = -1;
	private String seq;
	private String accession;
	//private int taxid;
	private ArrayList<Integer> taxids = new ArrayList<>(); 
	private String proteinName;


	
	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		if (this.accession != null) {
			log.debug("accession exist " + accession);
		}
		this.accession = accession;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		if (this.seq != null) {
			log.debug("seq exisst " + accession);
		}
		this.seq = seq;
	}

	public ArrayList<Integer> getTaxids() {
		return taxids;
	}

	public void setTaxids(ArrayList<Integer> taxids) {
		this.taxids = taxids;
	}

	public void addTaxid(int taxId) {
		this.taxids.add(taxId);
	}
	
	
}

class MyHandler extends DefaultHandler {
	private static final Logger log = LoggerFactory.getLogger(MyHandler.class);

	boolean sequence = false;
	boolean accession = false;
	boolean organism = false;
	
	// ncbi  <dbReference id="654924" type="NCBI Taxonomy"/> moze
	// biti u unutar <organismHost> a to je virus kao
	boolean organismHost=false;

	boolean dbReference = false;

	int countRow = 0;

	EntrySax row = new EntrySax();

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (sequence) {
			// System.out.println("seq: " + new String(ch, start, length));
			sequence = false;
			row.setSeq(new String(ch, start, length));
		} else if (accession) {
			row.setAccession(new String(ch, start, length));
//		}else if (organism) {
//			row.set
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO: pukni zadnji row negdje jos
		System.out.println("Finish, zapisa ima: " + countRow);
		processRow(row);
	}

	private void processRow(EntrySax r) {
		log.debug(r.toString());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("entry")) {
			countRow++;
			row = new EntrySax();
			// TODO: pukni row negdje
			if(countRow > 10) {
				throw new RuntimeException("Finish dosta");
			}

			// String dataset = attributes.getValue("dataset");
			// System.out.println("dataset " + dataset);
		} else if (qName.equals("sequence")) {
			sequence = true;
		} else if (qName.equals("accession")) {
			accession = true;
		} else if (organism && qName.equals("dbReference")) {
			if (attributes.getValue("type").equals("NCBI Taxonomy")) {
				int taxId = Integer.parseInt(attributes.getValue("id"));
				row.addTaxid(taxId);
			}
		} else if (qName.equals("organism")) {
			organism = true;
		}

	}

	@Override

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("organism")) {
			organism = false;
		}
		if (qName.equals("dbReference")) {
			dbReference = false;
		}
		if (qName.equals("accession")) {
			dbReference = false;
		}
		if("sequence".equals(qName)) {
			sequence = false;
		}
	}
}
