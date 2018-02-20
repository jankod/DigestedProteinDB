package hr.pbf.digestdb.test.probe.uniprot;

import java.util.ArrayList;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * UniProtKB accession numbers consist of 6 or 10 alphanumerical characters in the format:
 */
@SuppressWarnings("all")
public class EntryUniprot {
  @Accessors
  private final ArrayList<String> accessions = new ArrayList<String>();
  
  @Accessors
  private int tax;
  
  @Accessors
  private StringBuilder seq = new StringBuilder();
  
  @Accessors
  private String protName;
  
  @Pure
  public ArrayList<String> getAccessions() {
    return this.accessions;
  }
  
  @Pure
  public int getTax() {
    return this.tax;
  }
  
  public void setTax(final int tax) {
    this.tax = tax;
  }
  
  @Pure
  public StringBuilder getSeq() {
    return this.seq;
  }
  
  public void setSeq(final StringBuilder seq) {
    this.seq = seq;
  }
  
  @Pure
  public String getProtName() {
    return this.protName;
  }
  
  public void setProtName(final String protName) {
    this.protName = protName;
  }
}
