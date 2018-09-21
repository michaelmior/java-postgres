package edu.cs.rit.mmior;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.DateProducer;
import io.codearte.jfairy.producer.person.Person;
import io.codearte.jfairy.producer.text.TextProducer;

import me.tongfei.progressbar.ProgressBar;

import org.joda.time.DateTime;

class SQLFake extends SQLBase {
    static final String CREATE_DOCTOR_TABLE =
        "CREATE TABLE Doctor (" +
        "  ssn CHAR(9)," +
        "  firstName VARCHAR(75) NOT NULL," +
        "  lastName VARCHAR(75) NOT NULL," +
        "  salary FLOAT NOT NULL," +
        "  PRIMARY KEY (ssn));";

    static final String CREATE_SUPERVISEDBY_TABLE =
        "CREATE TABLE SupervisedBy (" +
        "  supervisor CHAR(9)," +
        "  supervisee CHAR(9)," +
        "  PRIMARY KEY (supervisor, supervisee)," +
        "  FOREIGN KEY (supervisor) REFERENCES Doctor(ssn)," +
        "  FOREIGN KEY (supervisee) REFERENCES Doctor(ssn));";

    static final String CREATE_PATIENT_TABLE =
        "CREATE TABLE Patient (" +
        "  ssn CHAR(9)," +
        "  firstName VARCHAR(75) NOT NULL," +
        "  middleName VARCHAR(75)," +
        "  lastName VARCHAR(75) NOT NULL," +
        "  primaryDoctor CHARACTER(9)," +
        "  PRIMARY KEY (ssn)," +
        "  FOREIGN KEY (primaryDoctor) REFERENCES Doctor(ssn));";

    static final String CREATE_VISIT_TABLE =
        "CREATE TABLE Visit (" +
        "  patient CHAR(9)," +
        "  scheduled TIMESTAMP," +
        "  height FLOAT NOT NULL," +
        "  weight FLOAT NOT NULL," +
        "  blood_pressure FLOAT NOT NULL," +
        "  temperature FLOAT NOT NULL," +
        "  other_notes TEXT," +
        "  PRIMARY KEY (patient, scheduled)," +
        "  FOREIGN KEY (patient) REFERENCES Patient(ssn));";

    static final int NUM_DOCTORS = 100;
    static final float DOCTOR_MAX_SALARY = 300000;
    static final float DOCTOR_MIN_SALARY = 100000;
    static final float DOCTOR_PCT_SUPERVISED = 0.95f;

    static final int SUPERIVISORS_PER_DOC = 2;

    static final int NUM_PATIENTS = 1000;
    static final float PATIENT_PCT_MIDDLE = 0.6f;

    static final int NUM_VISITS = 5000;
    static final int VISIT_YEARS_AGO = 3;
    static final int VISIT_MIN_HOUR = 9;
    static final int VISIT_MAX_HOUR = 17;
    static final int VISIT_MIN_HEIGHT= 40;
    static final int VISIT_MAX_HEIGHT= 80;
    static final int VISIT_MIN_WEIGHT = 50;
    static final int VISIT_MAX_WEIGHT = 250;
    static final int VISIT_MIN_PRESSURE = 50;
    static final int VISIT_MAX_PRESSURE = 200;
    static final int VISIT_MIN_TEMP = 80;
    static final int VISIT_MAX_TEMP = 110;
    static final float VISIT_PCT_NOTES = 0.95f;

    public static void main(String args[]) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection("hospital");
            stmt = con.createStatement();
            stmt.execute("DROP TABLE IF EXISTS Doctor, SupervisedBy, Patient, Visit");
            stmt.execute(CREATE_DOCTOR_TABLE);
            stmt.execute(CREATE_SUPERVISEDBY_TABLE);
            stmt.execute(CREATE_PATIENT_TABLE);
            stmt.execute(CREATE_VISIT_TABLE);
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
        try (ProgressBar pb = new ProgressBar("Doctors", NUM_DOCTORS)) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO Doctor VALUES (?, ?, ?, ?)");
            PreparedStatement ps2 = con.prepareStatement("INSERT INTO SupervisedBy VALUES (?, ?)");
            for (int i = 0; i < NUM_DOCTORS; i++) {
                Person doctor = fairy.person();
                String ssn = doctor.getNationalIdentityCardNumber().replace("-", "");
                ps.setString(1, doctor.getNationalIdentityCardNumber().replace("-", ""));
                ps.setString(2, doctor.getFirstName());
                ps.setString(3, doctor.getLastName());
                ps.setFloat(4, floatBetween(DOCTOR_MIN_SALARY, DOCTOR_MAX_SALARY));

                ps.execute();
                ps.clearParameters();

                if (Math.random() < DOCTOR_PCT_SUPERVISED && doctors.size() >= SUPERIVISORS_PER_DOC) {
                    IntStream ints = ThreadLocalRandom.current().ints(0, i).distinct();
                    ints = ints.limit((int) Math.ceil(Math.random() * SUPERIVISORS_PER_DOC));
                    ints.forEach((int j) -> {
                        try {
                            ps2.setString(1, doctors.get(j));
                            ps2.setString(2, ssn);
                            ps2.execute();
                            ps2.clearParameters();
                        } catch (SQLException e) {
                            System.err.println("Error creating supervisor relationship.");
                            System.exit(1);
                        }
                    });
                }

                doctors.add(ssn);
                pb.step();
            }
        } catch (SQLException e) {
            System.err.println("Error adding doctors.");
            System.exit(1);
        }

        List<String> patients = new ArrayList<>(NUM_PATIENTS);
        try (ProgressBar pb = new ProgressBar("Patients", NUM_PATIENTS)) {
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
                patients.add(ssn);
                pb.step();
            }
        } catch (SQLException e) {
            System.err.println("Error adding patients.");
            System.exit(1);
        }

        try (ProgressBar pb = new ProgressBar("Visits", NUM_VISITS)) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO Visit VALUES (?, ?, ?, ?, ?, ?, ?)");
            DateProducer dp = fairy.dateProducer();
            TextProducer tp = fairy.textProducer();
            for (int i = 0; i < NUM_VISITS; i++) {
                ps.setString(1, patients.get((int) (Math.random() * NUM_PATIENTS)));
                DateTime date = dp.randomDateInThePast(VISIT_YEARS_AGO)
                                  .withHourOfDay((int) floatBetween(VISIT_MIN_HOUR, VISIT_MAX_HOUR))
                                  .withMinuteOfHour((int) (Math.random() * 60))
                                  .withSecondOfMinute((int) (Math.random() * 60));
                ps.setTimestamp(2, new java.sql.Timestamp(date.getMillis()));
                ps.setFloat(3, floatBetween(VISIT_MIN_HEIGHT, VISIT_MAX_HEIGHT));
                ps.setFloat(4, floatBetween(VISIT_MIN_WEIGHT, VISIT_MAX_WEIGHT));
                ps.setFloat(5, floatBetween(VISIT_MIN_PRESSURE, VISIT_MAX_PRESSURE));
                ps.setFloat(6, floatBetween(VISIT_MIN_TEMP, VISIT_MAX_TEMP));
                if (Math.random() < VISIT_PCT_NOTES) {
                    ps.setString(7, tp.paragraph());
                } else {
                    ps.setString(7, null);
                }

                ps.execute();
                ps.clearParameters();
                pb.step();
            }
        } catch (SQLException e) {
            System.err.println("Error adding visits.");
            System.exit(1);
        }
    }

    public static float floatBetween(float min, float max) {
        return (float) Math.random() * (max - min) + min;
    }
}
