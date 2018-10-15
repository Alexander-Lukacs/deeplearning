package main;

import java.sql.*;

public class CreateDB {

    private Connection connection;

    private String DIM = "100";
    private String TABLE = "embeddings_" + DIM;
    private String VIEW = "Liste_Laenge_" + DIM;
    private String INDEX = "INDEX_ALL";

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
                if (connection == null) System.out.println("Connedtion to database already closed");
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

        String query0 = "CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8 COLLATE utf8_general_ci";

        String query1 = "USE `" + dbName + "`";

        String query2 = "SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO'; ";

        String query3 = "CREATE TABLE IF NOT EXISTS `" + TABLE + "` ("
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

        statement.executeUpdate("INSERT INTO " + TABLE + " " +
                "VALUES (" + jahr + ", '" + wort + "', " + dim + ", " + vec + ")");

    }

    public void indexing() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE INDEX INDEX_ALL ON " + TABLE + "(Jahr,Wort,Dimension,Vektor)");
    }

    /*
    SELECT e2.Wort, sqrt(sum(pow(e1.Vektor - e2.Vektor,2))) as sim
    FROM embeddings_100 e1, embeddings_100 e2
    WHERE e1.Wort='president' AND e1.Jahr=1987 AND e2.Jahr=1987
    AND e1.Dimension=e2.Dimension AND
    (e2.Wort LIKE 'a%' OR e2.Wort LIKE 'b%' OR e2.Wort LIKE 'c%' OR e2.Wort LIKE 'd%' OR e2.Wort LIKE 'e%' OR e2.Wort LIKE 'f%'
    OR e2.Wort LIKE 'g%' OR e2.Wort LIKE 'h%' OR e2.Wort LIKE 'i%' OR e2.Wort LIKE 'j%' OR e2.Wort LIKE 'k%' OR e2.Wort LIKE 'l%'
    OR e2.Wort LIKE 'm%' OR e2.Wort LIKE 'n%' OR e2.Wort LIKE 'o%' OR e2.Wort LIKE 'p%' OR e2.Wort LIKE 'q%' OR e2.Wort LIKE 'r%'
    OR e2.Wort LIKE 's%' OR e2.Wort LIKE 't%' OR e2.Wort LIKE 'u%' OR e2.Wort LIKE 'v%' OR e2.Wort LIKE 'w%' OR e2.Wort LIKE 'x%'
    OR e2.Wort LIKE 'y%' OR e2.Wort LIKE 'z%')
    GROUP BY e2.Wort ORDER BY sim ASC LIMIT 50;

     */

            /*ResultSet rs = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor - e2.Vektor,2))) as sim" +
                " FROM " + TABLE + " e1 , " + TABLE + " e2" +
                " WHERE e1.Wort='" + wort1 + "' AND e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 + " AND e1.Dimension = e2.Dimension" +
                " GROUP BY e2.Wort" +
                " ORDER BY sim ASC" +
                " LIMIT 50 ");*/

    public String selectEuklidisch(String wort1, int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sim = null;
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor - e2.Vektor,2))) as sim" +
                " FROM " + TABLE + " e1, " + TABLE + " e2" +
                " WHERE e1.Wort='" + wort1 + "' AND e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 +
                " AND e1.Dimension=e2.Dimension AND" +
                " (e2.Wort LIKE 'a%' OR e2.Wort LIKE 'b%' OR e2.Wort LIKE 'c%' OR e2.Wort LIKE 'd%' OR e2.Wort LIKE 'e%' OR e2.Wort LIKE 'f%'" +
                " OR e2.Wort LIKE 'g%' OR e2.Wort LIKE 'h%' OR e2.Wort LIKE 'i%' OR e2.Wort LIKE 'j%' OR e2.Wort LIKE 'k%' OR e2.Wort LIKE 'l%'" +
                " OR e2.Wort LIKE 'm%' OR e2.Wort LIKE 'n%' OR e2.Wort LIKE 'o%' OR e2.Wort LIKE 'p%' OR e2.Wort LIKE 'q%' OR e2.Wort LIKE 'r%'" +
                " OR e2.Wort LIKE 's%' OR e2.Wort LIKE 't%' OR e2.Wort LIKE 'u%' OR e2.Wort LIKE 'v%' OR e2.Wort LIKE 'w%' OR e2.Wort LIKE 'x%'" +
                " OR e2.Wort LIKE 'y%' OR e2.Wort LIKE 'z%')" +
                " GROUP BY e2.Wort ORDER BY sim ASC LIMIT 1000");
        while (!rs.isLast()) {
            if (rs.next()) {
                i++;
                wort = rs.getString(1);
                sim = rs.getString(2);
                stringBuilder.append(i + ". " + wort + ", ").append(sim).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public String selectVeränderung(int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sum = null;
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Höchste Veränderung\n");

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor-e2.Vektor,2))) AS sum" +
                " FROM " + TABLE + " e1, " + TABLE + " e2" +
                " WHERE e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 + " AND e1.Wort=e2.Wort AND e1.Dimension=e2.Dimension" +
                " GROUP BY e2.Wort" +
                " ORDER BY sum DESC" +
                " LIMIT 10");
//SELECT DISTINCT e2.Wort, sqrt(sum(pow(e1.Vektor-e2.Vektor,2))) AS sum
// FROM embeddings_100 e1, embeddings_100 e2
// WHERE e1.Wort=e2.Wort AND e1.Dimension=e2.Dimension AND e1.Jahr=1987 AND e2.Jahr=1992
// GROUP BY e2.Wort
// ORDER BY sum
// LIMIT 50;

        while (!rs.isLast()) {
            if (rs.next()) {
                wort = rs.getString(1);
                sum = rs.getString(2);
                stringBuilder.append(wort + ", ").append(sum).append("\n");
            }
        }

        stringBuilder.append("\nGeringste Veränderung\n");

        ResultSet rs2 = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor-e2.Vektor,2))) AS sum" +
                " FROM " + TABLE + " e1, " + TABLE + " e2" +
                " WHERE e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 + " AND e1.Wort=e2.Wort AND e1.Dimension=e2.Dimension" +
                " GROUP BY e2.Wort" +
                " ORDER BY sum ASC" +
                " LIMIT 10");

        while (!rs2.isLast()) {
            if (rs2.next()) {
                wort = rs2.getString(1);
                sum = rs2.getString(2);
                stringBuilder.append(wort + ", ").append(sum).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public String selectPräsident(String name, int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sum = null;
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, sqrt(sum(pow(e1.Vektor-e2.Vektor,2))) AS sum" +
                " FROM " + TABLE + " e1, " + TABLE + " e2" +
                " WHERE e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 + " AND e1.Wort='" + name + "' AND e1.Dimension=e2.Dimension" +
                " GROUP BY e2.Wort" +
                " ORDER BY sum ASC" +
                " LIMIT 50");

        while (!rs.isLast()) {
            if (rs.next()) {
                i++;
                wort = rs.getString(1);
                sum = rs.getString(2);
                stringBuilder.append(i + ". " + wort + ", ").append(sum).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public void dropIndex() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("ALTER TABLE " + TABLE + " DROP INDEX " + INDEX);
    }

    public String select1(String wort1, int jahr1) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sim = null;
        String j = null;
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();

        ResultSet rs = statement.executeQuery("SELECT e1.Wort, e1.Vektor, e1.Jahr" +
                " FROM " + TABLE + " e1" +
                " WHERE e1.Wort='" + wort1 + "' AND e1.Jahr=" + jahr1);

        while (!rs.isLast()) {
            if (rs.next()) {
                i++;
                wort = rs.getString(1);
                sim = rs.getString(2);
                j = rs.getString(3);
                stringBuilder.append(i + ". " + wort + ", " + j + ", ").append(sim).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public String select2(String wort1, int jahr1) throws SQLException {
        Statement statement = connection.createStatement();
        String wort = null;
        String sim = null;
        String j = null;
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, e2.Vektor, e2.Jahr" +
                " FROM " + TABLE + " e2" +
                " WHERE e2.Wort='" + wort1 + "' AND e2.Jahr=" + jahr1);

        while (!rs.isLast()) {
            if (rs.next()) {
                i++;
                wort = rs.getString(1);
                sim = rs.getString(2);
                j = rs.getString(3);
                stringBuilder.append(i + ". " + wort + ", " + j + ", ").append(sim).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public void createView() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE VIEW Liste_Laenge_50 ( Jahr, Wort, Laenge ) AS SELECT Jahr, Wort, sqrt(sum(Vektor * Vektor)) as Laenge" +
                " FROM embeddings_50" +
                " GROUP BY Jahr, Wort");
        statement.executeUpdate("CREATE VIEW Liste_Laenge_100 ( Jahr, Wort, Laenge ) AS SELECT Jahr, Wort, sqrt(sum(Vektor * Vektor)) as Laenge" +
                " FROM embeddings_100" +
                " GROUP BY Jahr, Wort");
    }

    public String selectCosinus(String wort1, int jahr1, int jahr2) throws SQLException {
        Statement statement = connection.createStatement();
        StringBuilder stringBuilder = new StringBuilder();
        String wort = null;
        String cos = null;
        int i = 0;

        ResultSet rs = statement.executeQuery("SELECT e2.Wort, sum(e1.Vektor * e2.Vektor) / (l1.Laenge * l2.Laenge) as cos" +
                " FROM " + TABLE + " e1 , " + TABLE + " e2 , " + VIEW + " l1 , " + VIEW + " l2" +
                " WHERE e1.Wort='" + wort1 + "' AND e1.Jahr=" + jahr1 + " AND e2.Jahr=" + jahr2 +
                " AND e1.Dimension=e2.Dimension AND e1.jahr=l1.jahr AND e2.jahr=l2.jahr AND l1.Wort=e1.Wort AND l2.Wort=e2.Wort" +
                " AND (e2.Wort LIKE 'a%' OR e2.Wort LIKE 'b%' OR e2.Wort LIKE 'c%' OR e2.Wort LIKE 'd%' OR e2.Wort LIKE 'e%' OR e2.Wort LIKE 'f%'" +
                " OR e2.Wort LIKE 'g%' OR e2.Wort LIKE 'h%' OR e2.Wort LIKE 'i%' OR e2.Wort LIKE 'j%' OR e2.Wort LIKE 'k%' OR e2.Wort LIKE 'l%'" +
                " OR e2.Wort LIKE 'm%' OR e2.Wort LIKE 'n%' OR e2.Wort LIKE 'o%' OR e2.Wort LIKE 'p%' OR e2.Wort LIKE 'q%' OR e2.Wort LIKE 'r%'" +
                " OR e2.Wort LIKE 's%' OR e2.Wort LIKE 't%' OR e2.Wort LIKE 'u%' OR e2.Wort LIKE 'v%' OR e2.Wort LIKE 'w%' OR e2.Wort LIKE 'x%'" +
                " OR e2.Wort LIKE 'y%' OR e2.Wort LIKE 'z%')" +
                " GROUP BY e2.Wort" +
                " ORDER BY cos DESC" +
                " LIMIT 1000 ");

        while (!rs.isLast()) {
            if (rs.next()) {
                i++;
                wort = rs.getString(1);
                cos = rs.getString(2);
                stringBuilder.append(i + ". " + wort + ", ").append(cos).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }

    public void deleteTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("TRUNCATE TABLE " + TABLE);
    }

    public static void main(String[] args) {
        new CreateDB();
    }
}
