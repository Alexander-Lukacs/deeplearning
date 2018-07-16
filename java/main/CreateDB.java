package main;

import java.sql.*;

public class CreateDB {

    private Connection connection;

    private String TABLE = "embeddings_50";

    static {
        String url_DBClassname = "com.mysql.jdbc.Driver";
        try {
            Class<?> driverClass = Class.forName(url_DBClassname);
            DriverManager.registerDriver((Driver) driverClass.newInstance());
            if (driverClass != null) {
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
    }

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

    public void createConnection() {
        String url_Connection = "jdbc:mysql://localhost:3306/TestDB";
        String user = "root";
        String pass = "So54_12eS";
        try {
            System.out.println("Creating DBConnection");
            connection = DriverManager.getConnection(url_Connection, user, pass);
        } catch (SQLException e) {
            System.err.println("Couldn't create DBConnection");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private boolean createDBStructure() {
        String dbName = "TestDB";

        String query0 = "CREATE DATABASE IF NOT EXISTS `" + dbName + "`";

        String query1 = "USE `" + dbName + "`";

        String query2 = "SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO'; ";

        String query3 = "CREATE TABLE IF NOT EXISTS `"+TABLE+"` ("
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

        statement.executeUpdate("INSERT INTO "+TABLE+" " +
                "VALUES ("+jahr+", '"+wort+"', "+dim+", "+vec+")");

    }

    public void indexing() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE INDEX JAHR_INDEX ON "+TABLE+"(Jahr)");
        statement.executeUpdate("CREATE INDEX WORT_INDEX ON "+TABLE+"(Wort)");
    }

    public String select(String wort1, int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sim = null;

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor - e2.Vektor,2))) as sim" +
                " FROM "+TABLE+" e1 , "+TABLE +" e2" +
                " WHERE e1.Wort='" + wort1 + "' AND e1.Jahr="+jahr1+ " AND e2.Jahr="+ jahr2+ " AND e1.Dimension = e2.Dimension"+
                " GROUP BY e2.Wort" +
                " ORDER BY sim DESC" +
                " LIMIT 10 ");

        if(rs.next()) {
            wort = rs.getString(1);
            sim = rs.getString(2);
        }
        return wort + ", "+ sim;
    }

    public String select1(String wort1, int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        StringBuilder stringBuilder = new StringBuilder();
        String wort = null;
        String jahr = null;
        String dim = null;
        String vek = null;

        ResultSet rs = statement.executeQuery("SELECT e1.Wort, sum(e1.Vektor) as sum" +
                " FROM "+TABLE +" e1 "+
                " WHERE e1.Jahr="+jahr1+ " AND e1.Wort!='wave'"+
                " GROUP BY e1.Wort");

        while (!rs.isLast()) {
            if (rs.next()) {
                wort = rs.getString(1);
                vek = rs.getString(2);
                stringBuilder.append(wort+", ").append(vek).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
    public static void main(String[] args) {
        new CreateDB();
    }
}
