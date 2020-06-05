package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class StudentToclass_students {

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
                    ("jdbc:mysql://localhost:3306/school_management?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_old_db.setAutoCommit(false);

            // BEFORE GENERATING STAFF ID CHECK FOR UNIQUENESS OF NAMES
            PreparedStatement retrieveOldData = connection_old_db.prepareStatement
                    ("SELECT school_schoolId,studentId,schoolClass_id,room "
                                    + "FROM Student WHERE school_schoolId=39",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT INTO class_students (class_id, student_id) "
                                    + "VALUES (?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            // DELETE NULL DATA OP
            PreparedStatement deleteNullData = connection_new_db.prepareStatement
                    ("DELETE "
                            + "FROM class_students WHERE class_id = 0");

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                PreparedStatement retrieveDataFromStudent = connection_new_db.prepareStatement
                        ("SELECT id "
                                + "FROM student WHERE sid = ? AND admission_number = ?");
                retrieveDataFromStudent.setInt(1, retrieveOldDataRS.getInt("school_schoolId"));
                retrieveDataFromStudent.setString(2, retrieveOldDataRS.getString("studentId"));
                ResultSet retrieveDataFromStudentRS = retrieveDataFromStudent.executeQuery();

                if (retrieveOldDataRS.getString("schoolClass_id") != null) {
                    PreparedStatement retrieveOldDataFromSchoolClass = connection_old_db.prepareStatement
                            ("SELECT schoolClassCode "
                                    + "FROM SchoolClass WHERE id = ?");
                    retrieveOldDataFromSchoolClass.setInt(1, retrieveOldDataRS.getInt("schoolClass_id"));
                    ResultSet retrieveOldDataFromSchoolClassRS = retrieveOldDataFromSchoolClass.executeQuery();

                    if (retrieveOldDataFromSchoolClassRS.next()) {
                        PreparedStatement retrieveDataFromLevel = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM level WHERE sid = ? AND name = ?");
                        retrieveDataFromLevel.setInt(1, retrieveOldDataRS.getInt("school_schoolId"));
                        retrieveDataFromLevel.setString(2, retrieveOldDataFromSchoolClassRS.getString("schoolClassCode"));
                        ResultSet retrieveDataFromLevelRS = retrieveDataFromLevel.executeQuery();

                        if (retrieveDataFromLevelRS.next()) {
                            PreparedStatement retrieveDataFromClass = connection_new_db.prepareStatement
                                        ("SELECT id "
                                                + "FROM class WHERE sid = ? AND name = ? AND level_id = ?");
                                retrieveDataFromClass.setInt(1, retrieveOldDataRS.getInt("school_schoolId"));
                                retrieveDataFromClass.setString(2, retrieveOldDataRS.getString("room"));
                                retrieveDataFromClass.setInt(3, retrieveDataFromLevelRS.getInt("id"));
                                ResultSet retrieveDataFromClassRS = retrieveDataFromClass.executeQuery();

                            if (retrieveDataFromClassRS.next() && retrieveDataFromStudentRS.next()) {
                                insertNewData.setInt(1, retrieveDataFromClassRS.getInt("id"));
                                insertNewData.setInt(2, retrieveDataFromStudentRS.getInt("id"));
                            }
                        }
                    }
                } else {
                    insertNewData.setInt(1, 0);
                    if (retrieveDataFromStudentRS.next())
                        insertNewData.setInt(2, retrieveDataFromStudentRS.getInt("id"));
                }

                insertNewData.executeUpdate();
            }
            deleteNullData.executeUpdate();

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
