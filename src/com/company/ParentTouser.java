package com.company;

import java.sql.*;

public class ParentTouser {
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
                    ("SELECT schoolcode,parentcode "
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
                    ("INSERT INTO user (created_on,del_flag,version,sid,expiry_date,login_id,password,password_expiry_date,status,type,role_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,CURRENT_TIMESTAMP,?,?,CURRENT_TIMESTAMP,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            ResultSet retrieveRoleIdRS = null;
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

                    PreparedStatement retrieveRoleId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM role WHERE sid = ? AND name = ?");
                    retrieveRoleId.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    retrieveRoleId.setString(2, "Parent Role");
                    retrieveRoleIdRS = retrieveRoleId.executeQuery();
                } else {
                    insertNewData.setNull(3, Types.NULL);
                }

                insertNewData.setString(4, retrieveOldDataRS.getString("parentcode"));
                insertNewData.setString(5,"{bcrypt}$2a$10$dVexkI1bJ8X3ZDxJsKSmseRzLCHekd0i3VRMyrBJxZtdXFemWX2fC");
                insertNewData.setInt(6, 1);
                insertNewData.setInt(7, 2);

                if (retrieveRoleIdRS != null) {
                    if (retrieveRoleIdRS.next()) {
                        insertNewData.setInt(8, retrieveRoleIdRS.getInt("id"));
                    } else
                        insertNewData.setNull(8, Types.NULL);
                } else {
                    insertNewData.setNull(8, Types.NULL);
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
