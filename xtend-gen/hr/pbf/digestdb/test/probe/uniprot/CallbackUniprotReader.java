package hr.pbf.digestdb.test.probe.uniprot;

import hr.pbf.digestdb.test.probe.uniprot.EntryUniprot;

@SuppressWarnings("all")
public interface CallbackUniprotReader {
  public abstract void readEntry(final EntryUniprot e);
}
