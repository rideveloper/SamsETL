package com.company;

import java.sql.*;

public class RoleTorole {
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

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

            PreparedStatement retrieveOldData = connection_new_db.prepareStatement
                    ("SELECT id "
                                    + "FROM school",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT INTO role (created_on, del_flag, version, sid, name, type) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                for (int i = 0; i < 3; i++) {
                    switch (i) {
                        case 0:
                            //staff role
                            insertNewData.setString(1, "N");
                            insertNewData.setInt(2, 0);
                            insertNewData.setInt(3, retrieveOldDataRS.getInt("id"));
                            insertNewData.setString(4, "Staff Role");
                            insertNewData.setInt(5, 1);
                            insertNewData.executeUpdate();
                            break;
                        case 1:
                            //parent role
                            insertNewData.setString(1, "N");
                            insertNewData.setInt(2, 0);
                            insertNewData.setInt(3, retrieveOldDataRS.getInt("id"));
                            insertNewData.setString(4, "Parent Role");
                            insertNewData.setInt(5, 2);
                            insertNewData.executeUpdate();
                            break;
                        case 2:
                            //student role
                            insertNewData.setString(1, "N");
                            insertNewData.setInt(2, 0);
                            insertNewData.setInt(3, retrieveOldDataRS.getInt("id"));
                            insertNewData.setString(4, "Student Role");
                            insertNewData.setInt(5, 3);
                            insertNewData.executeUpdate();
                            break;
                    }
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
