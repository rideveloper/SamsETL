package com.company;

import java.sql.*;

public class SubjectTeacherToassigned_subjects {
    protected void performMigration() {
        Connection connection_old_db = null;
        Connection connection_new_db = null;

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
                    ("SELECT schoolClasses, schoolCode, subjectName, employee_id "
                                    + "FROM SubjectTeacher WHERE schoolCode='SG'",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

//
            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT IGNORE INTO assigned_subjects (created_on,del_flag,version,sid,sclass_id,subject_id,teacher_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            // DELETE NULL DATA OP
            PreparedStatement deleteNullData = connection_new_db.prepareStatement
                    ("DELETE "
                            + "FROM assigned_subjects WHERE subject_id = 0 OR teacher_id = 0");

            while(retrieveOldDataRS.next())
            {
                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolcode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                retrieveOldDataRSForSchool.first();

                try {
                    PreparedStatement retrieveOldDataFromLevel = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM level WHERE sid = ? AND name = ?");
                    retrieveOldDataFromLevel.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveOldDataFromLevel.setString(2, retrieveOldDataRS.getString("schoolClasses"));
                    ResultSet retrieveOldDataFromLevelRS = retrieveOldDataFromLevel.executeQuery();

                    retrieveOldDataFromLevelRS.first();

                    PreparedStatement retrieveClasses = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM class WHERE sid = ? AND level_id = ?");
                    retrieveClasses.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveClasses.setInt(2, retrieveOldDataFromLevelRS.getInt("id"));
                    ResultSet retrieveClassesRS = retrieveClasses.executeQuery();

                    while (retrieveClassesRS.next()) {
                        insertNewData.setString(1, "N");
                        insertNewData.setInt(2, 0);
                        insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));
                        insertNewData.setInt(4, retrieveClassesRS.getInt("id"));

                        PreparedStatement retrieveDataFromSubject = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM subject WHERE sid = ? AND code = ?");
                        retrieveDataFromSubject.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                        retrieveDataFromSubject.setString(2, retrieveOldDataRS.getString("subjectName"));
                        ResultSet retrieveDataFromSubjectRS = retrieveDataFromSubject.executeQuery();

                        retrieveDataFromSubjectRS.first();

                        try {
                            insertNewData.setInt(5, retrieveDataFromSubjectRS.getInt("id"));
                        } catch (Exception ex) {
                            insertNewData.setInt(5, 0);
                        }

                        PreparedStatement retrieveDataFromEmployee = connection_old_db.prepareStatement
                                ("SELECT employeeId "
                                        + "FROM Employee WHERE id = ?");
                        retrieveDataFromEmployee.setInt(1, retrieveOldDataRS.getInt("employee_id"));
                        ResultSet retrieveDataFromEmployeeRS = retrieveDataFromEmployee.executeQuery();

                        retrieveDataFromEmployeeRS.first();

                        try {
                            PreparedStatement retrieveDataFromStaff = connection_new_db.prepareStatement
                                    ("SELECT id "
                                            + "FROM staff WHERE staff_id = ?");
                            retrieveDataFromStaff.setString(1, retrieveDataFromEmployeeRS.getString("employeeId"));
                            ResultSet retrieveDataFromStaffRS = retrieveDataFromStaff.executeQuery();

                            retrieveDataFromStaffRS.first();

                            insertNewData.setInt(6, retrieveDataFromStaffRS.getInt("id"));
                        } catch (Exception ex) {
                            insertNewData.setInt(6, 0);
                        }

                        insertNewData.executeUpdate();
                    }
                } catch (Exception ex) {
                    insertNewData.setString(1, "N");
                    insertNewData.setInt(2, 0);
                    insertNewData.setInt(3, 0);
                    insertNewData.setInt(4, 0);
                    insertNewData.setInt(5, 0);
                    insertNewData.setInt(6, 0);
                }

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
