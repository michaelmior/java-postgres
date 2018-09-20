package edu.cs.rit.mmior;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;

class SQLFake extends SQLBase {
    static final String CREATE_DOCTOR_TABLE =
        "CREATE TABLE Doctor (" +
        "  ssn CHAR(9)," +
        "  firstName VARCHAR(75) NOT NULL," +
        "  lastName VARCHAR(75) NOT NULL," +
        "  salary FLOAT NOT NULL," +
        "  supervisor CHARACTER(9)," +
        "  PRIMARY KEY (ssn)," +
        "  FOREIGN KEY (supervisor) REFERENCES Doctor(ssn));";

    static final String CREATE_PATIENT_TABLE =
        "CREATE TABLE Patient (" +
        "  ssn CHAR(9)," +
        "  firstName VARCHAR(75) NOT NULL," +
        "  middleName VARCHAR(75)," +
        "  lastName VARCHAR(75) NOT NULL," +
        "  primaryDoctor CHARACTER(9)," +
        "  PRIMARY KEY (ssn)," +
        "  FOREIGN KEY (primaryDoctor) REFERENCES Doctor(ssn));";

    static final int NUM_DOCTORS = 1000;
    static final float DOCTOR_MAX_SALARY = 300000;
    static final float DOCTOR_MIN_SALARY = 100000;
    static final float DOCTOR_PCT_SUPERVISED = 0.95f;
    static final int NUM_PATIENTS = 1000;
    static final float PATIENT_PCT_MIDDLE = 0.6f;

    public static void main(String args[]) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection("hospital");
            stmt = con.createStatement();
            stmt.execute("DROP TABLE IF EXISTS Doctor, Patient");
            stmt.execute(CREATE_DOCTOR_TABLE);
            stmt.execute(CREATE_PATIENT_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
            // System.err.println("Something went wrong.");
        } finally {
            try {
                if (stmt != null) { stmt.close(); }
            } catch (SQLException e) {
                System.err.println("Something went REALLY wrong.");
            }
        }

        Fairy fairy = Fairy.create();

        List<String> doctors = new ArrayList<>(NUM_DOCTORS);
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO Doctor VALUES (?, ?, ?, ?, ?)");
            for (int i = 0; i < NUM_DOCTORS; i++) {
                Person doctor = fairy.person();
                String ssn = doctor.getNationalIdentityCardNumber().replace("-", "");
                ps.setString(1, doctor.getNationalIdentityCardNumber().replace("-", ""));
                ps.setString(2, doctor.getFirstName());
                ps.setString(3, doctor.getLastName());
                ps.setFloat(4, (float) (Math.random() * (DOCTOR_MAX_SALARY - DOCTOR_MIN_SALARY) + DOCTOR_MIN_SALARY));

                if (Math.random() > DOCTOR_PCT_SUPERVISED || doctors.isEmpty()) {
                    ps.setString(5, null);
                } else {
                    ps.setString(5, doctors.get((int) (Math.random() * (i - 1))));
                }

                ps.execute();
                ps.clearParameters();
                doctors.add(ssn);
            }
        } catch (SQLException e) {
            System.err.println("Error adding doctors.");
            System.exit(1);
        }

        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO Patient VALUES (?, ?, ?, ?, ?)");
            for (int i = 0; i < NUM_PATIENTS; i++) {
                Person patient = fairy.person();
                String ssn = patient.getNationalIdentityCardNumber().replace("-", "");
                ps.setString(1, patient.getNationalIdentityCardNumber().replace("-", ""));
                ps.setString(2, patient.getFirstName());
                if (Math.random() < PATIENT_PCT_MIDDLE) {
                    ps.setString(3, patient.getMiddleName());
                } else {
                    ps.setString(3, null);
                }
                ps.setString(4, patient.getLastName());
                ps.setString(5, doctors.get((int) (Math.random() * NUM_DOCTORS)));

                ps.execute();
                ps.clearParameters();
            }
        } catch (SQLException e) {
            System.err.println("Error adding patients.");
            System.exit(1);
        }
    }
}
