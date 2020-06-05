    package com.company;

    import java.math.BigDecimal;
    import java.sql.*;

    public class GradeTograde {
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
                        ("SELECT DISTINCT schoolCode, template "
                                        + "FROM Grade WHERE schoolCode = 'SG' GROUP BY schoolCode, template ORDER BY schoolCode",
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
                        ("INSERT INTO grade (created_on, del_flag, version, sid, accuracy, description, is_default, name) "
                                        + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                retrieveOldDataRS.beforeFirst();
                while(retrieveOldDataRS.next()) {
                        insertNewData.setString(1, "N");
                        insertNewData.setInt(2, 0);

                        insertNewData.setNull(3, 1);

                        insertNewData.setBigDecimal(4, BigDecimal.valueOf(0.10));
                        insertNewData.setString(5, retrieveOldDataRS.getString("template")+" Grade");
                        insertNewData.setNull(6, Types.NULL);
                        insertNewData.setString(7, retrieveOldDataRS.getString("template")+" Grade");

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
