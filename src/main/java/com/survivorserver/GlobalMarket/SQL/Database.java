package com.survivorserver.GlobalMarket.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Database {

    private Logger log;
    private Connection con;
    private String user;
    private String pass;
    private String server;
    private String db;
    private String path;
    private int port;
    private PreparedStatement lastStatement;

    /**
     * SQLite constructor
     * @param log
     * @param user
     * @param pass
     * @param server
     * @param db
     * @param path
     */
    public Database(Logger log, String user, String pass, String server, String db, String path) {
        this.log = log;
        this.user = user;
        this.pass = pass;
        this.server = server;
        this.db = db;
        this.path = path;
    }

    /**
     * MySQL constructor
     * @param log
     * @param user
     * @param pass
     * @param server
     * @param db
     * @param port
     */
    public Database(Logger log, String user, String pass, String server, String db, int port) {
        this.log = log;
        this.user = user;
        this.pass = pass;
        this.server = server;
        this.db = db;
        this.port = port;
    }

    public boolean isSqlite() {
        return path != null;
    }

    public boolean isConnected() {
        try {
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("SELECT 1");
            r.close();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public synchronized boolean connect() {
        try {
            if (!isConnected()) {
                if (path != null) {
                    Class.forName("org.sqlite.JDBC");
                    con = DriverManager.getConnection("jdbc:sqlite://" + path + "/" + db + ".db");
                } else {
                    Class.forName("com.mysql.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "?useUnicode=true&characterEncoding=utf8", user, pass);
                    con.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + db);
                    con.setCatalog(db);


                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean close() {
        try {
            con.close();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public MarketStatement createStatement(String query) throws SQLException {
        try {
            if (lastStatement != null) {
                lastStatement.close();
            }
        } catch(Exception ignored) { }
        lastStatement = con.prepareStatement(query);
        return new MarketStatement(log, lastStatement);
    }

    public MarketStatement createStatement(String query, String toReturn) throws SQLException {
        try {
            if (lastStatement != null) {
                lastStatement.close();
            }
        } catch(Exception ignored) { }
        lastStatement = con.prepareStatement(query, new String[]{toReturn});
        return new MarketStatement(log, lastStatement);
    }
}
