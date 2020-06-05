package com.company;

import java.sql.*;

public class EmployeeTostaff {
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
                    ("SELECT schoolId_schoolId,city,country,state,dateOfBirth,email,firsName,sex,lastName,middleName,taskAssigned,phoneNumber,title,employeeId "
                                    + "FROM Employee WHERE schoolId_schoolId=39",
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
                    ("INSERT INTO staff (created_on,del_flag,version,sid,city,country,state,state_of_origin,date_of_birth,email_address,first_name," +
                                    "gender,last_name,middle_name,occupation,phone_number,staff_id,title,year_of_experience) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);
                insertNewData.setInt(3, retrieveOldDataRS.getInt("schoolId_schoolId"));
                insertNewData.setString(4, retrieveOldDataRS.getString("city"));
                insertNewData.setString(5, retrieveOldDataRS.getString("country"));
                insertNewData.setString(6, retrieveOldDataRS.getString("city"));
                insertNewData.setString(7, retrieveOldDataRS.getString("state"));
                insertNewData.setDate(8, retrieveOldDataRS.getDate("dateOfBirth"));
                insertNewData.setString(9, retrieveOldDataRS.getString("email"));
                insertNewData.setString(10, retrieveOldDataRS.getString("firsName"));

                if (retrieveOldDataRS.getString("sex") != null) {
                    if (retrieveOldDataRS.getString("sex").equalsIgnoreCase("male") ||
                            retrieveOldDataRS.getString("sex").equalsIgnoreCase("m"))
                        insertNewData.setString(11, "M");
                    else if (retrieveOldDataRS.getString("sex").equalsIgnoreCase("female") ||
                            retrieveOldDataRS.getString("sex").equalsIgnoreCase("f"))
                        insertNewData.setString(11, "F");
                    else
                        insertNewData.setNull(11,Types.NULL);
                } else {
                    insertNewData.setNull(11,Types.NULL);
                }

                insertNewData.setString(12, retrieveOldDataRS.getString("lastName"));
                insertNewData.setString(13, retrieveOldDataRS.getString("middleName"));
                if (retrieveOldDataRS.getString("taskAssigned") != null) {
                    insertNewData.setString(14, retrieveOldDataRS.getString("taskAssigned").toUpperCase());
                } else {
                    insertNewData.setNull(14, Types.NULL);
                }

                if (retrieveOldDataRS.getString("phoneNumber") == null ||
                        retrieveOldDataRS.getString("phoneNumber").length() < 11) {
                    insertNewData.setNull(15, Types.NULL);
                } else {
                    insertNewData.setString(15, retrieveOldDataRS.getString("phoneNumber").substring(0, 11));
                }

                insertNewData.setString(16, retrieveOldDataRS.getString("employeeId"));

                if (retrieveOldDataRS.getString("title") != null) {
                    if (retrieveOldDataRS.getString("title").toLowerCase().contains("mrs")) {
                        insertNewData.setString(17, "MRS");
                    } else if (retrieveOldDataRS.getString("title").toLowerCase().contains("miss")) {
                        insertNewData.setString(17, "MISS");
                    } else if (retrieveOldDataRS.getString("title").equalsIgnoreCase("mr") ||
                            retrieveOldDataRS.getString("title").equalsIgnoreCase("mr.")) {
                        insertNewData.setString(17, "MR");
                    } else {
                        insertNewData.setNull(17, Types.NULL);
                    }
                } else {
                    insertNewData.setNull(17, Types.NULL);
                }

                insertNewData.setInt(18, 0);

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
