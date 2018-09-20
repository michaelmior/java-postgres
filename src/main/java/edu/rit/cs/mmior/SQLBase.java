package edu.cs.rit.mmior;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

class SQLBase {
    public static Connection getConnection() throws SQLException {
        // Read the password file
        // should be ~/.pgpass and look like
        // reddwarf.cs.rit.edu:5432:USERNAME:USERNAME:PASSWORD
        String homeDir = System.getProperty("user.home");
        String content = null;

        try {
            content = new Scanner(new File(homeDir, ".pgpass")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load password file.");
            System.exit(1);
        }

        String parts[] = content.split(":");
        String host = parts[0];
        String port = parts[1];
        String username = parts[2];
        String password = parts[4];

        return DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/?currentSchema=foodmart", username, password);
    }
}
