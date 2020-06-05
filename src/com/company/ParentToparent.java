package com.company;

import java.sql.*;

public class ParentToparent {
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
                    ("SELECT schoolcode,city,country,address1,address2,guardianEmail,fatherName,guardianphoneNumber,parentcode "
                                    + "FROM Parent WHERE schoolcode='SG'",
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
                    ("INSERT INTO parent (created_on,del_flag,version,sid,city,country,local_govt,state,email,first_name,phone_number,user_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            ResultSet retrieveUserIdRS = null;
            while(retrieveOldDataRS.next())
            {

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);

                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolcode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                if (retrieveOldDataRSForSchool.next()) {
                    insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));

                    PreparedStatement retrieveUserId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM user WHERE sid = ? AND login_id = ? AND type = 2");
                    retrieveUserId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveUserId.setString(2, retrieveOldDataRS.getString("parentcode"));
                    retrieveUserIdRS = retrieveUserId.executeQuery();
                } else {
                    insertNewData.setNull(3, Types.NULL);
                }

                insertNewData.setString(4, retrieveOldDataRS.getString("city"));
                insertNewData.setString(5,retrieveOldDataRS.getString("country"));
                insertNewData.setString(6, retrieveOldDataRS.getString("address1") +" "+ retrieveOldDataRS.getString("address2"));
                insertNewData.setString(7, retrieveOldDataRS.getString("city"));
                insertNewData.setString(8, retrieveOldDataRS.getString("guardianEmail"));
                insertNewData.setString(9, retrieveOldDataRS.getString("fatherName"));

                if (retrieveOldDataRS.getString("guardianphoneNumber") == null ||
                        retrieveOldDataRS.getString("guardianphoneNumber").length() < 11) {
                    insertNewData.setNull(10, Types.NULL);
                } else {
                    insertNewData.setString(10, retrieveOldDataRS.getString("guardianphoneNumber").substring(0, 11));
                }

                if (retrieveUserIdRS != null) {
                    if (retrieveUserIdRS.next()) {
                        insertNewData.setInt(11, retrieveUserIdRS.getInt("id"));
                    } else
                        insertNewData.setNull(11, Types.NULL);
                } else {
                    insertNewData.setNull(11, Types.NULL);
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
