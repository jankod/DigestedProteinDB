package hr.pbf.digestdb.util;
	
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class MySQLdb {

	private BasicDataSource ds;

	

	public void initDatabase(String username, String password, String url) throws SQLException {

		ds = new BasicDataSource();
		// ds.setPoolPreparedStatements(true);
		ds.setMaxWait(7000L);
		ds.setMaxActive(180);
		ds.addConnectionProperty("useUnicode", "false");
		ds.addConnectionProperty("characterEncoding", "ASCII");
		// ds.setMaxIdle(5);
		ds.setMaxOpenPreparedStatements(20000);
		
		ds.setTestOnBorrow(true);
		ds.setTestWhileIdle(true);
		// ds.setTimeBetweenEvictionRunsMillis(10000L);
		// ds.setMinEvictableIdleTimeMillis(60000L);
		//ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUsername(username);
		ds.setPassword(password);
		// ds.setRemoveAbandoned(true);
		// ds.setAccessToUnderlyingConnectionAllowed(true);
		// ds.setMaxIdle(4);
		ds.setValidationQuery("select 1");
		ds.setUrl(url );
		ds.getConnection().close();

	}

	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	public void closeDatabaase() {
		if (ds != null) {
			try {
				ds.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
