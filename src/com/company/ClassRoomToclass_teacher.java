package com.company;

import java.sql.*;

public class ClassRoomToclass_teacher {
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
                    ("SELECT classCode,room,schoolCode,classteacherId,session "
                                    + "FROM ClassRoom WHERE schoolCode ='SG'",
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
                    ("INSERT IGNORE INTO class_teacher (created_on,del_flag,version,sid,designation,enabled,s_class_id,teacher_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

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
                    retrieveOldDataFromLevel.setString(2, retrieveOldDataRS.getString("classCode"));
                    ResultSet retrieveOldDataFromLevelRS = retrieveOldDataFromLevel.executeQuery();

                    PreparedStatement retrieveSessionData = connection_new_db.prepareStatement
                            ("SELECT start_date, end_date "
                                    + "FROM session WHERE description = ? AND status = 'O'");
                    retrieveSessionData.setString(1, retrieveOldDataRS.getString("session"));
                    ResultSet retrieveSessionDataRS = retrieveSessionData.executeQuery();

                    retrieveOldDataFromLevelRS.first();
                    retrieveSessionDataRS.first();

                    insertNewData.setString(1, "N");
                    insertNewData.setInt(2, 0);
                    insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));
                    insertNewData.setString(4, "Class Teacher");
                    insertNewData.setInt(5, 1);

                    PreparedStatement retrieveClassId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM class WHERE sid = ? AND name = ? AND level_id = ? AND end_date = ? AND start_date =?");
                    retrieveClassId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveClassId.setString(2, retrieveOldDataRS.getString("room"));
                    retrieveClassId.setInt(3, retrieveOldDataFromLevelRS.getInt("id"));
                    retrieveClassId.setDate(4, retrieveSessionDataRS.getDate("end_date"));
                    retrieveClassId.setDate(5, retrieveSessionDataRS.getDate("start_date"));

                    ResultSet retrieveClassIdRS = retrieveClassId.executeQuery();

                    retrieveClassIdRS.first();

                    insertNewData.setInt(6, retrieveClassIdRS.getInt("id"));

                    PreparedStatement retrieveDataFromStaff = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM staff WHERE staff_id = ?");
                    retrieveDataFromStaff.setString(1, retrieveOldDataRS.getString("classteacherId"));
                    ResultSet retrieveDataFromStaffRS = retrieveDataFromStaff.executeQuery();

                    retrieveDataFromStaffRS.first();

                    insertNewData.setInt(7, retrieveDataFromStaffRS.getInt("id"));

                    insertNewData.executeUpdate();
                } catch (Exception ex) {
                    insertNewData.setString(1, "N");
                    insertNewData.setInt(2, 0);
                    insertNewData.setInt(3, 0);
                    insertNewData.setInt(4, 0);
                    insertNewData.setInt(5, 0);
                    insertNewData.setInt(6, 0);
                    insertNewData.setInt(7, 0);
                }

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
