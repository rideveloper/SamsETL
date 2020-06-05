package com.company;

import java.sql.*;
import java.time.LocalDateTime;

public class SchoolToSchool {
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
                    ("SELECT schoolId, city, country, license, schoolCode, schoolContact, schoolMotto, schoolName, state, phoneNumber "
                                    + "FROM School where schoolCode = ?",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            retrieveOldData.setString(1, "SG");
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
                    ("INSERT INTO school (id, created_on, del_flag, version, city, country, state, state_of_origin, code, email_address, has_user," +
                                    " license_count, motto, name, phone_number, status) "
                                    + "VALUES (?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {

                insertNewData.setInt(1, retrieveOldDataRS.getInt("schoolId"));
                insertNewData.setString(2, "N");
                insertNewData.setInt(3, 0);
                insertNewData.setString(4, retrieveOldDataRS.getString("city"));
                insertNewData.setString(5, retrieveOldDataRS.getString("country"));
                insertNewData.setString(6, retrieveOldDataRS.getString("state"));
                insertNewData.setString(7, retrieveOldDataRS.getString("state"));
                insertNewData.setString(8, retrieveOldDataRS.getString("schoolCode"));
                insertNewData.setString(9, retrieveOldDataRS.getString("schoolContact"));
                insertNewData.setBoolean(10, false);
                insertNewData.setInt(11, retrieveOldDataRS.getInt("license"));
                insertNewData.setString(12, retrieveOldDataRS.getString("schoolMotto"));
                insertNewData.setString(13, retrieveOldDataRS.getString("schoolName"));

                if (retrieveOldDataRS.getString("phoneNumber") == null ||
                        retrieveOldDataRS.getString("phoneNumber").length() < 11) {
                    insertNewData.setNull(14, Types.NULL);
                } else {
                    insertNewData.setString(14, retrieveOldDataRS.getString("phoneNumber").substring(0, 11));
                }

                insertNewData.setInt(15, 1);

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
