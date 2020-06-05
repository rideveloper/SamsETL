package com.company;

import java.sql.*;

public class session_events2 {
    protected void performMigration() {
        Connection connection_old_db = null;
        Connection connection_new_db = null;
        Connection connection_local_target_db = null;

        try
        {
            Class.forName("com.mysql.jdbc.Driver"); //LOADS THE DRIVER FOR THE DATABASE
            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

            // BEFORE GENERATING STAFF ID CHECK FOR UNIQUENESS OF NAMES
            PreparedStatement retrieveOldData = connection_new_db.prepareStatement
                    ("SELECT id "
                                    + "FROM session",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertNewData = null;
            insertNewData = connection_new_db.prepareStatement
                    ("INSERT INTO session_events (session_id, events_id) "
                                    + "VALUES (?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            int cnt = 0;
            int n = 10;
            while(retrieveOldDataRS.next())
            {
                cnt++;
                if (cnt > 1) {
                    for (int i = 1; i <= 3; i++) {
                        insertNewData.setInt(1, cnt);
                        insertNewData.setInt(2, n);

                        n++;

                        insertNewData.executeUpdate();
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
