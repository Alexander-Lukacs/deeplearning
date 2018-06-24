package main;

import java.sql.*;

public class CreateDB {

    private Connection connection;

    /*static {
        try {
            java.sql.Driver driver = DriverManager.getDriver("jdbc:mysql://localhost/?rewriteBatchedStatements=true");
            if (Class.forName(driver.toString()).newInstance() != null) {
                System.out.println("JDBC-Treiber geladen");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Fehler beim Laden des JDBC-Treibers");
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    public CreateDB() {
        createConnection();
        createDBStructure();
        Thread shutDownHook = new Thread() {
            public void run() {
                System.out.println("Running shutdown hook");
                if(connection == null) System.out.println("Connedtion to database already closed");
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                        if (connection.isClosed())
                            System.out.println("Connection to database closed");
                    }
                } catch (SQLException e) {
                    System.err.println(
                            "Shutdown hook couldn't close database connection.");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutDownHook);
    }

    private void createConnection() {
        //String url = "jdbc:mysql://localhost/?rewriteBatchedStatements=true";
        String url_Connection = "jdbc:mysql://localhost:3306/mysql";
        String url_DBClassname = "com.mysql.jdbc.Driver";
        String user = "root";
        String pass = "So54_12eS";
        try {
            //java.sql.Driver driver = DriverManager.getDriver(url_DBClassname);
            //System.out.println(driver.toString());
            Class driverClass = Class.forName(url_DBClassname);
            DriverManager.registerDriver((Driver) driverClass.newInstance());
            System.out.println("Creating DBConnection");
            connection = DriverManager.getConnection(url_Connection, user, pass);
        } catch (SQLException e) {
            System.err.println("Couldn't create DBConnection");
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private boolean createDBStructure() {
        String dbName = "TestDB";

        String query0 = "CREATE DATABASE IF NOT EXISTS `" + dbName + "`";

        String query1 = "USE `" + dbName + "`";

        String query2 = "SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO'; ";

        String query3 = "CREATE TABLE IF NOT EXISTS `embeddings` ("
                + "`Jahr` int NOT NULL, "
                + "`Wort` varchar(100) NOT NULL, "
                + "`Dimension` int NOT NULL,"
                + "`Vektor` double NOT NULL) ENGINE=MyISAM DEFAULT CHARSET=utf8 "
                + "DEFAULT COLLATE=utf8_german2_ci";

        Statement stmt = null;
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.addBatch(query0);
            stmt.addBatch(query1);
            stmt.addBatch(query2);
            stmt.addBatch(query3);
            stmt.executeBatch();
            connection.commit();
            stmt.close();
            connection.close();
            System.out.println("Database successfully created or just existing");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public void insert(int jahr, String wort, int dim, double vec) throws SQLException {
        Statement statement = connection.createStatement();

        statement.executeUpdate("INSERT INTO embeddings " +
                "VALUES ("+jahr+", '"+wort+"', "+dim+", "+vec+")");
    }

    public static void main(String[] args) {
        new CreateDB();
    }
}
