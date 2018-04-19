package hr.pbf.digestdb.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniprotConfig {

	public static void main(String[] args) {
		// System.out.println(UniprotConfig.get(Name.PATH_TREMBL_CSV));

		Configuration p = get().getProperties();

		String var = get(Name.PATH_TREMBL_CSV);
		System.out.println(var);
	}

	public static String get(Name name) {
		return UniprotConfig.getProperties().getString(name.name().toLowerCase());
	}

	private static UniprotConfig	instance;
	private static final Logger		log	= LoggerFactory.getLogger(UniprotConfig.class);

	private static Configurations	configs;
	private static Configuration	properties;

	private UniprotConfig() {

	}

	public static enum Name {
		BASE_DIR,
		PATH_TREMBL_CSV,
		PATH_TREMB_LEVELDB,
		PATH_TREMB_PROT_NAMES_CSV,
		PATH_TREMB_LEVELDB_INDEX_CSV,
		// SSTable float[mass] => int[peptides] from mapdb
		PATH_TREMBL_MASS_PEPTIDES_MAP,
		PATH_TREMBL_PROT_NAMES_LEVELDB

	}

	public static UniprotConfig get() {
		init();
		return instance;
	}

	private static void init() {
		try {
			if (configs != null) {
				return;
			}
			configs = new Configurations();
			String fileName = "config_lin.properties";
			if (SystemUtils.IS_OS_WINDOWS) {
				fileName = "config_win.properties";
			}

			URL file = UniprotConfig.class.getResource("/" + fileName);
			if(file == null) {
				throw new RuntimeException("Not find config path ("+ fileName);
			}
			log.debug("Load "+ file);
			properties = configs.properties(file);
		} catch (Throwable e) {
			log.error("", e);
			ExceptionUtils.rethrow(e);
		}
	}

	public static Configuration getProperties() {
		init();
		return properties;
	}

}
