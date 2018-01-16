package hr.pbf.digestdb.test.probe;

import java.io.File;
import java.io.IOException;

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
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		MyHandler handler = new MyHandler();
		saxParser.parse(new File("F:\\Downloads\\uniprot_sprot.xml\\uniprot_sprot.xml"), handler);

	}
}

//@Data
class Row {

	private static final Logger log = LoggerFactory.getLogger(Row.class);
	
	int numRow = -1;
	private String seq;
	private String accession;
	private int taxid;
	private String proteinName;
	
	
	public void setTaxid(int taxid) {
		
		this.taxid = taxid;
	}
	
	public int getTaxid() {
		return taxid;
	}
	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		if(this.accession != null) {
			log.debug("accession exist "+ accession);
		}
		this.accession = accession;
	}
	
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		if(this.seq != null) {
			log.debug("seq exisst "+ accession);
		}
		this.seq = seq;
	}
}

class MyHandler extends DefaultHandler {
	private static final Logger log = LoggerFactory.getLogger(MyHandler.class);

	boolean sequence = false;
	boolean accession = false;
	boolean organism = false;

	int countRow = 0;

	Row row = new Row();

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (sequence) {
			// System.out.println("seq: " + new String(ch, start, length));
			sequence = false;
			row.setSeq(new String(ch, start, length));
		} else if (accession) {
			row.setAccession(new String(ch, start, length));

		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO: pukni zadnji row negdje jos
		System.out.println("Finish, zapisa ima: " + countRow);
		processRow(row);
	}

	private void processRow(Row r) {
		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("entry")) {
			countRow++;
			row = new Row();
			// TODO: pukni row negdje

			// String dataset = attributes.getValue("dataset");
			// System.out.println("dataset " + dataset);
		} else if (qName.equals("sequence")) {
			sequence = true;
		} else if (qName.equals("accession")) {
			accession = true;
		} else if (organism && qName.equals("dbReference")) {
			if (attributes.getValue("type").equals("NCBI Taxonomy")) {
				int taxId = Integer.parseInt(attributes.getValue("id"));
				if (row.getTaxid() != 0) {
					log.debug("Sadrzi vec taxid " + row);
				}
				row.setTaxid(taxId);
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
	}
}
