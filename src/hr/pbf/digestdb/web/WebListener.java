package hr.pbf.digestdb.web;

import java.io.IOException;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder;

public class WebListener implements ServletContextListener {
	private static final Logger			log	= LoggerFactory.getLogger(WebListener.class);
	private static UniprotLevelDbFinder	finder;

	public static UniprotLevelDbFinder getFinder() {
		return finder;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (finder != null) {
			try {
				log.debug("close");
				finder.close();

			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent cnt) {
		try {
			String levelDbPath = "F:\\tmp\\trembl.leveldb";
			// String indexPath =
			// "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.leveldb.index.compact";

			//String ssTablePath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.index.sstable";
			levelDbPath = UniprotConfig.get(Name.PATH_TREMB_LEVELDB);
			String ssTablePath = UniprotConfig.get(UniprotConfig.Name.PATH_TREMBL_MASS_PEPTIDES_MAP);
			String protNamesPath = UniprotConfig.get(Name.PATH_TREMBL_PROT_NAMES_LEVELDB);

			log.debug("Trembl path " + levelDbPath);
			log.debug("Index path " + ssTablePath);
			log.debug("Prot names path: " + protNamesPath);
			finder = new UniprotLevelDbFinder(levelDbPath, ssTablePath, protNamesPath);

		} catch (IOException e) {
			log.error("", e);
		}
	}

}
