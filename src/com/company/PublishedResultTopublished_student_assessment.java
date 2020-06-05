package com.company;

import java.sql.*;

public class PublishedResultTopublished_student_assessment {
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
                    ("SELECT classCode,grade,remarks,room,schoolCode,session,studentid,subject,term,total,template "
                                    + "FROM PublishedResult",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/sams_done?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

//
            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT INTO published_student_assessment (created_on,del_flag,version,sid,remarks,score,total_score,event_id," +
                                    "grade_score_id,sclass_id,student_id,subject_id,type_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            ResultSet retrieveRSLevelId = null;
            ResultSet retrieveRSSclass_Id = null;
            ResultSet retrieveStudentIdRS = null;
            ResultSet retrieveGradeIdRS = null;
            ResultSet retrieveSubjectRSId =  null;
            while(retrieveOldDataRS.next())
            {

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);

                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolCode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                if (retrieveOldDataRSForSchool.next()) {
                    insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));

                    PreparedStatement retrieveLevelId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM level WHERE sid = ? AND name = ?");
                    retrieveLevelId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveLevelId.setString(2, retrieveOldDataRS.getString("classCode"));
                    retrieveRSLevelId = retrieveLevelId.executeQuery();

                    PreparedStatement retrieveStudentId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM student WHERE sid = ? AND admission_number = ?");
                    retrieveStudentId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveStudentId.setString(2, retrieveOldDataRS.getString("studentid"));
                    retrieveStudentIdRS = retrieveStudentId.executeQuery();

                    PreparedStatement retrieveGradeId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM grade WHERE sid = ? AND name = ?");
                    retrieveGradeId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveGradeId.setString(2, retrieveOldDataRS.getString("template"));
                    retrieveGradeIdRS = retrieveGradeId.executeQuery();

                    PreparedStatement retrieveSubjectId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM subject WHERE sid = ? AND code = ?");
                    retrieveSubjectId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveSubjectId.setString(2, retrieveOldDataRS.getString("subject"));
                    retrieveSubjectRSId = retrieveSubjectId.executeQuery();
                } else
                    insertNewData.setNull(3, Types.NULL);

                insertNewData.setString(4, retrieveOldDataRS.getString("remarks"));
                insertNewData.setDouble(5, Double.parseDouble(retrieveOldDataRS.getString("total")));
                insertNewData.setDouble(6, Double.parseDouble("100"));
                insertNewData.setInt(7, 1);
                insertNewData.setInt(8, 1);

                while (retrieveRSLevelId.next()) {
                    PreparedStatement retrieveSclass_Id = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM class WHERE level_id = ? AND name = ?");
                    retrieveSclass_Id.setInt(1, retrieveRSLevelId.getInt("id"));
                    retrieveSclass_Id.setString(2, retrieveOldDataRS.getString("room"));
                    retrieveRSSclass_Id = retrieveSclass_Id.executeQuery();
                }

                if (retrieveRSSclass_Id != null) {
                    if (retrieveRSSclass_Id.next()) {
                        insertNewData.setInt(9, retrieveRSSclass_Id.getInt("id"));
                    } else
                        insertNewData.setInt(9, 0);
                } else {
                    insertNewData.setInt(9, 0);
                }


                if (retrieveStudentIdRS != null) {
                    if (retrieveStudentIdRS.next()) {
                        insertNewData.setInt(10, retrieveStudentIdRS.getInt("id"));
                    } else
                        insertNewData.setNull(10, Types.NULL);
                } else {
                    insertNewData.setNull(10, Types.NULL);
                }

                if (retrieveSubjectRSId != null) {
                    if (retrieveSubjectRSId.next()) {
                        insertNewData.setInt(11, retrieveSubjectRSId.getInt("id"));
                    } else
                        insertNewData.setInt(11, 0);
                } else {
                    insertNewData.setInt(11, 0);
                }

                if (retrieveGradeIdRS != null) {
                    if (retrieveGradeIdRS.next()) {
                        insertNewData.setInt(12, retrieveGradeIdRS.getInt("id"));
                    } else
                        insertNewData.setNull(12, Types.NULL);
                } else {
                    insertNewData.setNull(12, Types.NULL);
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
