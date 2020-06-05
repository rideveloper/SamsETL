package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class StudentTostudent {

    public Date formatDate(String date) {
        LocalDate formattedDate = null;
        ZoneId defaultZoneId = ZoneId.systemDefault();
        SimpleDateFormat oldDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
        if (date == null)
            return null;
        try {
            formattedDate = LocalDate.parse(newDate.format(oldDate.parse(date)));
        } catch (ParseException ex) {
            return null;
            //ex.printStackTrace();
        }

        return Date.valueOf(formattedDate);
    }

    protected void performMigration() {
        Connection connection_old_db = null;
        Connection connection_new_db = null;
        Connection connection_local_target_db = null;

        try
        {
            Class.forName("com.mysql.jdbc.Driver"); //LOADS THE DRIVER FOR THE DATABASE
            //SOURCE DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_old_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/sams-live?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_old_db.setAutoCommit(false);

            // BEFORE GENERATING STAFF ID CHECK FOR UNIQUENESS OF NAMES
            PreparedStatement retrieveOldData = connection_old_db.prepareStatement
                    ("SELECT school_schoolId, city, country, dateOfBirth, firsName, lastName, middleName, religion, sex, stateOfOrigin, " +
                                    "studentId, guardianEmail, guardianphoneNumber, schoolClass_id "
                                    + "FROM Student",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/sams_done?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT INTO student (created_on, del_flag, version, sid, city, country, state, state_of_origin, admission_number, date_of_birth," +
                                    "email, first_name, gender, last_name, middle_name, phone_number, religion, template_serial_no, level_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                ResultSet retrieveDataRSFromLevel = null;

                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolClassCode, school_schoolId "
                                + "FROM SchoolClass WHERE id = ?");
                retrieveOldDataForSchool.setInt(1, retrieveOldDataRS.getInt("schoolClass_id"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);

                while (retrieveOldDataRSForSchool.next()) {
                    PreparedStatement retrieveDataFromLevel = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM level WHERE sid = ? AND name = ?");
                    retrieveDataFromLevel.setInt(1, retrieveOldDataRSForSchool.getInt("school_schoolId"));
                    retrieveDataFromLevel.setString(2, retrieveOldDataRSForSchool.getString("schoolClassCode"));
                    retrieveDataRSFromLevel = retrieveDataFromLevel.executeQuery();
                }

                insertNewData.setInt(3, retrieveOldDataRS.getInt("school_schoolId"));
                insertNewData.setString(4, retrieveOldDataRS.getString("city"));
                insertNewData.setString(5, retrieveOldDataRS.getString("country"));
                insertNewData.setString(6, retrieveOldDataRS.getString("city"));
                insertNewData.setString(7, retrieveOldDataRS.getString("stateOfOrigin"));
                insertNewData.setString(8, retrieveOldDataRS.getString("studentId"));
                insertNewData.setDate(9, formatDate(retrieveOldDataRS.getString("dateOfBirth")));
                insertNewData.setString(10, retrieveOldDataRS.getString("guardianEmail"));
                insertNewData.setString(11, retrieveOldDataRS.getString("firsName"));

                if (retrieveOldDataRS.getString("sex").equalsIgnoreCase("male") ||
                        retrieveOldDataRS.getString("sex").equalsIgnoreCase("m"))
                    insertNewData.setString(12, "M");
                else if (retrieveOldDataRS.getString("sex").equalsIgnoreCase("female") ||
                        retrieveOldDataRS.getString("sex").equalsIgnoreCase("f"))
                    insertNewData.setString(12, "F");
                else
                    insertNewData.setNull(12,Types.NULL);

                insertNewData.setString(13, retrieveOldDataRS.getString("lastName"));
                insertNewData.setString(14, retrieveOldDataRS.getString("middleName"));

                if (retrieveOldDataRS.getString("guardianphoneNumber") == null ||
                        retrieveOldDataRS.getString("guardianphoneNumber").length() < 11) {
                    insertNewData.setNull(15, Types.NULL);
                } else {
                    insertNewData.setString(15, retrieveOldDataRS.getString("guardianphoneNumber").substring(0, 11));
                }

                if (retrieveOldDataRS.getString("religion").toLowerCase().contains("christ"))
                    insertNewData.setString(16, "CHRISTIANITY");
                else if (retrieveOldDataRS.getString("religion").toLowerCase().contains("islam") ||
                        retrieveOldDataRS.getString("religion").toLowerCase().contains("muslim"))
                    insertNewData.setString(16, "ISLAM");
                else
                    insertNewData.setNull(16,Types.NULL);

                insertNewData.setInt(17, 0);

                if (retrieveDataRSFromLevel != null) {
                    while (retrieveDataRSFromLevel.next()) {
                        insertNewData.setInt(18, retrieveDataRSFromLevel.getInt("id"));
                    }
                } else {
                    insertNewData.setNull(18, Types.NULL);
                }

                insertNewData.executeUpdate();
            }

            connection_new_db.commit();
            System.out.println("Successfully migrated");
        }

        catch ( SQLException sqlException )
        {
            System.out.println("troubling with sql query");
            System.out.println(sqlException.getMessage());
            sqlException.printStackTrace();
        }

        catch ( ClassNotFoundException classNotFound )
        {

            System.out.println("trouble with finding class");
            System.out.println(classNotFound.getMessage());
            classNotFound.printStackTrace();

        }




        // IF A NULL POINTER EXCEPTION IS ENCOUNTERED IN THE ABOVE TRY BLOCK THEN RESET THE LOGIN PAGE.
        catch(NullPointerException npe)
        {

            System.out.println("error found here 3");
            System.out.println(npe.getMessage());
            npe.printStackTrace();

            return;

        }

    }
}
