package edu.cs.rit.mmior;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

class SQLTest extends SQLBase {
    public static void main(String args[]) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = getConnection("foodmart");
            rs = executeQuery(con, "SELECT sales_city FROM region;");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            con.close();
        } catch (SQLException e) {
            System.err.println("Something went wrong.");
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (SQLException e) {
                System.err.println("Something went REALLY wrong.");
            }
        }
    }
}
