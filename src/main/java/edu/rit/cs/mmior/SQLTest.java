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

        try {
            Connection con = DriverManager.getConnection("jdbc:postgresql://reddwarf.cs.rit.edu/", username, password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT sales_city FROM foodmart.region WHERE sales_city != 'None';");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
