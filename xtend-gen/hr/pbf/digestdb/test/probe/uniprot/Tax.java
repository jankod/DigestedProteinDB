package hr.pbf.digestdb.test.probe.uniprot;

import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class Tax {
  public Tax(final int taxId, final String desc) {
    this.taxId = taxId;
    this.desc = desc;
  }
  
  @Accessors
  private int taxId;
  
  @Accessors
  private String desc;
  
  @Override
  public String toString() {
    String _plus = (Integer.valueOf(this.taxId) + " ");
    return (_plus + this.desc);
  }
  
  @Pure
  public int getTaxId() {
    return this.taxId;
  }
  
  public void setTaxId(final int taxId) {
    this.taxId = taxId;
  }
  
  @Pure
  public String getDesc() {
    return this.desc;
  }
  
  public void setDesc(final String desc) {
    this.desc = desc;
  }
}
