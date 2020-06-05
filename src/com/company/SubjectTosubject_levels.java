package com.company;

import java.sql.*;

public class SubjectTosubject_levels {
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
                    ("SELECT subjectName, schoolClass_id "
                                    + "FROM Subject",
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
                    ("INSERT IGNORE INTO subject_levels (subject_id, level_id) "
                                    + "VALUES (?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            // DELETE NULL DATA OP
            PreparedStatement deleteNullData = connection_new_db.prepareStatement
                    ("DELETE "
                            + "FROM subject_levels WHERE subject_id = 0");

            while(retrieveOldDataRS.next())
            {
                PreparedStatement retrieveOldDataFromSchoolClass = connection_old_db.prepareStatement
                        ("SELECT schoolClassCode, school_schoolId "
                                + "FROM SchoolClass WHERE id = ? AND school_schoolId=39");
                retrieveOldDataFromSchoolClass.setInt(1, retrieveOldDataRS.getInt("schoolClass_id"));
                ResultSet retrieveOldDataFromSchoolClassRS = retrieveOldDataFromSchoolClass.executeQuery();

                if (retrieveOldDataFromSchoolClassRS.next()) {
                    PreparedStatement schoolExists = connection_new_db.prepareStatement
                            ("SELECT EXISTS(SELECT id "
                                    + "FROM school WHERE id = ?) AS schoolExists");
                    schoolExists.setInt(1, retrieveOldDataFromSchoolClassRS.getInt("school_schoolId"));
                    ResultSet schoolExistsRS = schoolExists.executeQuery();

                    schoolExistsRS.first();
                    if (schoolExistsRS.getInt("schoolExists") == 1) {
                        PreparedStatement retrieveDataFromLevel = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM level WHERE sid = ? AND name = ?");
                        retrieveDataFromLevel.setInt(1, retrieveOldDataFromSchoolClassRS.getInt("school_schoolId"));
                        retrieveDataFromLevel.setString(2, retrieveOldDataFromSchoolClassRS.getString("schoolClassCode"));
                        ResultSet retrieveDataFromLevelRS = retrieveDataFromLevel.executeQuery();

                        PreparedStatement retrieveDataFromSubject = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM subject WHERE sid = ? AND name = ?");
                        retrieveDataFromSubject.setInt(1, retrieveOldDataFromSchoolClassRS.getInt("school_schoolId"));
                        if (retrieveOldDataRS.getString("subjectName").toLowerCase().contains("christ")) {
                            retrieveDataFromSubject.setString(2, "CHRISTAIN RELIGION STUDIES(R.N.V)");
                        } else {
                            retrieveDataFromSubject.setString(2, retrieveOldDataRS.getString("subjectName"));
                        }

                        ResultSet retrieveDataFromSubjectRS = retrieveDataFromSubject.executeQuery();

                        retrieveDataFromLevelRS.first();
                        retrieveDataFromSubjectRS.first();

                        insertNewData.setInt(2, retrieveDataFromLevelRS.getInt("id"));
                        try {
                            insertNewData.setInt(1, retrieveDataFromSubjectRS.getInt("id"));
                        } catch (Exception ex) {
                            insertNewData.setInt(1, 0);
                        }
                    }

                    insertNewData.executeUpdate();
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
