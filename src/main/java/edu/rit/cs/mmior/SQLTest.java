package edu.cs.rit.mmior;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;

class SQLTest {
    public static void main(String args[]) {
        // Read the password file
        // should be ~/.pgpass and look like
        // reddwarf.cs.rit.edu:5432:USERNAME:USERNAME:PASSWORD
        String homeDir = System.getProperty("user.home");
        String content = null;

        try {
            content = new Scanner(new File(homeDir, ".pgpass")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load password file.");
        }

        String parts[] = content.split(":");
        String host = parts[0];
        String port = parts[1];
        String username = parts[2];
        String password = parts[4];

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/?currentSchema=foodmart", username, password);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT sales_city FROM region WHERE sales_city != 'None';");
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
