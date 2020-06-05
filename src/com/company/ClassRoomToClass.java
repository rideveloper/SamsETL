package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class ClassRoomToClass {

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
                    ("SELECT classCode, room, schoolCode, session "
                                    + "FROM ClassRoom WHERE schoolCode ='SG'",
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
                    ("INSERT INTO class (created_on, del_flag, version, sid, description, name, end_date, start_date, level_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                ResultSet retrieveDataRSFromLevel = null;
                ResultSet retrieveDataRSFromSession = null;

                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolCode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);

                while (retrieveOldDataRSForSchool.next()) {
                    PreparedStatement retrieveDataFromLevel = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM level WHERE sid = ? AND name = ?");
                    retrieveDataFromLevel.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveDataFromLevel.setString(2, retrieveOldDataRS.getString("classCode"));
                    retrieveDataRSFromLevel = retrieveDataFromLevel.executeQuery();

                    PreparedStatement retrieveDataFromSession = connection_new_db.prepareStatement
                            ("SELECT end_date, start_date "
                                    + "FROM session WHERE sid = ? AND description = ?");
                    retrieveDataFromSession.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveDataFromSession.setString(2, retrieveOldDataRS.getString("session"));
                    retrieveDataRSFromSession = retrieveDataFromSession.executeQuery();

                    insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));
                }

                insertNewData.setString(4, retrieveOldDataRS.getString("room")+" Class");
                insertNewData.setString(5, retrieveOldDataRS.getString("room"));

                if (retrieveDataRSFromSession != null) {
                    if (retrieveDataRSFromSession.next()) {
                        insertNewData.setDate(6, retrieveDataRSFromSession.getDate("end_date"));
                        insertNewData.setDate(7, retrieveDataRSFromSession.getDate("start_date"));
                    } else {
                        insertNewData.setNull(6, Types.NULL);
                        insertNewData.setNull(7, Types.NULL);
                    }
                } else {
                    insertNewData.setNull(6, Types.NULL);
                    insertNewData.setNull(7, Types.NULL);
                }

                if (retrieveDataRSFromLevel != null) {
                    if (retrieveDataRSFromLevel.next()) {
                        insertNewData.setInt(8, retrieveDataRSFromLevel.getInt("id"));
                    } else
                        insertNewData.setNull(8, Types.NULL);
                } else
                    insertNewData.setNull(8, Types.NULL);


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
