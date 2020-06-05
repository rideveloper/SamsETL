package com.company;

import java.sql.*;

public class UserTouser {
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
                    ("SELECT isEnabled,roleType,userId,employee_id,school_schoolId "
                                    + "FROM User WHERE school_schoolId=39",
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

            while(retrieveOldDataRS.next())
            {

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);
                insertNewData.setInt(3, retrieveOldDataRS.getInt("school_schoolId"));
                insertNewData.setString(4, retrieveOldDataRS.getString("userId"));
                insertNewData.setString(5,"{bcrypt}$2a$10$dVexkI1bJ8X3ZDxJsKSmseRzLCHekd0i3VRMyrBJxZtdXFemWX2fC");
                insertNewData.setInt(6, retrieveOldDataRS.getInt("isEnabled"));
                insertNewData.setInt(7, 1);

                PreparedStatement retrieveRoleId = connection_new_db.prepareStatement
                        ("SELECT id "
                                + "FROM role WHERE sid = ? AND name = ?");
                retrieveRoleId.setInt(1, retrieveOldDataRS.getInt("school_schoolId"));
                retrieveRoleId.setString(2, "Staff Role");
                ResultSet retrieveRoleIdRS = retrieveRoleId.executeQuery();

                if (retrieveRoleIdRS != null) {
                    if (retrieveRoleIdRS.next()) {
                        insertNewData.setInt(8, retrieveRoleIdRS.getInt("id"));
                    } else
                        insertNewData.setNull(8, Types.NULL);
                } else {
                    insertNewData.setNull(8, Types.NULL);
                }

                insertNewData.executeUpdate();

                ResultSet rsId = insertNewData.getGeneratedKeys();
                int generatedKey = 0;
                if (rsId.next()) {
                    generatedKey = rsId.getInt(1);
                }

                PreparedStatement retrieveEmployeeDetails = connection_old_db.prepareStatement
                        ("SELECT employeeId "
                                + "FROM Employee WHERE id = ?");
                retrieveEmployeeDetails.setInt(1, retrieveOldDataRS.getInt("employee_id"));
                ResultSet retrieveEmployeeDetailsRS = retrieveEmployeeDetails.executeQuery();

                retrieveEmployeeDetailsRS.first();

                PreparedStatement retrieveStaffId = connection_new_db.prepareStatement
                        ("SELECT id "
                                + "FROM staff WHERE staff_id = ?");
                retrieveStaffId.setString(1, retrieveEmployeeDetailsRS.getString("employeeId"));
                ResultSet retrieveStaffIdRS = retrieveStaffId.executeQuery();

                retrieveStaffIdRS.first();

                PreparedStatement updateData = connection_new_db.prepareStatement
                        ("UPDATE staff SET user_id = ? WHERE id = ?",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                updateData.setInt(1, generatedKey);
                updateData.setInt(2, retrieveStaffIdRS.getInt("id"));
                updateData.executeUpdate();

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
