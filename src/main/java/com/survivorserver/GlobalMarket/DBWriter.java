package com.survivorserver.GlobalMarket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DBWriter {
	
	Logger log;
	private Connection connection;
	private String user;
	private String pass;
	private String server;
	private String db;
	private int port;
	boolean sqlite;
	String path;
	
	public DBWriter(Logger log, String user, String pass, String server, String db, int port, boolean sqlite, String path) {
		this.log = log;
		this.user = user;
		this.pass = pass;
		this.server = server;
		this.db = db;
		this.port = port;
		this.sqlite = sqlite;
		this.path = path;
	}
	
	public synchronized boolean connect() {
		try {
			if (sqlite) {
				if (!isConnected()) {
					Class.forName("org.sqlite.JDBC");
		            connection = DriverManager.getConnection("jdbc:sqlite://" + path + "/" + db + ".db");
				}
			} else {
				if (!isConnected()) {
					Class.forName("com.mysql.jdbc.Driver");
		            connection = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + db, user, pass);
				}
			}
			return true;
		} catch(Exception e) {
			log.severe("Could not connect to SQL server: " + e.getMessage());
			return false;
		}
	}
	
	public boolean setDb(String db) {
		try {
			this.db = db;
			connection.setCatalog(db);
			return true;
		} catch(Exception e) {
			log.severe("Could not change database: " + e.getMessage());
			return false;
		}
	}
	
	public boolean close() {
		try {
			connection.close();
			return true;
		} catch(Exception e) {
			log.severe("Could not close SQL connection: " + e.getMessage());
			return false;
		}
	}
	
	public boolean isConnected() {
		try {
			Statement s = connection.createStatement();
            ResultSet r = s.executeQuery("SELECT 1 from DUAL WHERE 1 = '0'");
            r.close();
            return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public ResultSet query(String query) {
		Statement s;
		try {
			s = connection.createStatement();
			if (query.startsWith("SELECT")) {
				return s.executeQuery(query);
			} else {
				s.executeUpdate(query);
			}
		} catch(Exception e) {
			log.info("An error occurred while trying to query the databse \"" + db + "\": " + e.getMessage());
		}
		return null;
	}
	
	public PreparedStatement prepareStatement(String query) {
		try {
			return connection.prepareStatement(query);
		} catch (SQLException e) {
			return null;
		}
	}
	
	public ResultSet queryStatement(PreparedStatement s) {
		try {
			return s.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
}

