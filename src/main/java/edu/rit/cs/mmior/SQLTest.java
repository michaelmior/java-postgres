package edu.cs.rit.mmior;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

class SQLTest {
    public static void main(String args[]) {
        Console console = System.console();
        String username = console.readLine("Username: ");
        String password = new String(console.readPassword("Password: "));

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection("jdbc:postgresql://reddwarf.cs.rit.edu/", username, password);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT sales_city FROM foodmart.region WHERE sales_city != 'None';");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            con.close();
        } catch (SQLException e) {
            System.err.println("Something went wrong.");
        } finally {
            try {
                if (rs != null) { rs.close(); }
                if (stmt != null) { stmt.close(); }
                if (con != null) { con.close(); }
            } catch (SQLException e) {
                System.err.println("Something went REALLY wrong.");
            }
        }
    }
}
