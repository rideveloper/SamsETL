package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class parent_student {

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
                    ("SELECT school_schoolId, studentId, parentcode "
                                    + "FROM Student WHERE school_schoolId=39",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertParentStudent = connection_new_db.prepareStatement
                    ("INSERT INTO parent_student (created_on,del_flag,version,sid,parent_id,student_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                try {
                    insertParentStudent.setString(1, "N");
                    insertParentStudent.setInt(2, 0);
                    insertParentStudent.setInt(3, retrieveOldDataRS.getInt("school_schoolId"));

                    PreparedStatement retrieveStudentId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM student WHERE admission_number = ? AND sid = ?");
                    retrieveStudentId.setString(1, retrieveOldDataRS.getString("studentId"));
                    retrieveStudentId.setInt(2, retrieveOldDataRS.getInt("school_schoolId"));
                    ResultSet retrieveStudentIdRS = retrieveStudentId.executeQuery();

                    retrieveStudentIdRS.first();

                    insertParentStudent.setInt(5, retrieveStudentIdRS.getInt("id"));

                    PreparedStatement retrieveUserId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM user WHERE login_id = ? AND type = 2");
                    retrieveUserId.setString(1, retrieveOldDataRS.getString("parentcode"));
                    ResultSet retrieveUserIdRS = retrieveUserId.executeQuery();

                    retrieveUserIdRS.first();

                    PreparedStatement retrieveParentId = connection_new_db.prepareStatement
                            ("SELECT id "
                                    + "FROM parent WHERE user_id = ?");
                    retrieveParentId.setInt(1, retrieveUserIdRS.getInt("id"));
                    ResultSet retrieveParentIdRS = retrieveParentId.executeQuery();

                    retrieveParentIdRS.first();

                    insertParentStudent.setInt(4, retrieveParentIdRS.getInt("id"));

                    insertParentStudent.executeUpdate();
                } catch (Exception ex) {

                    insertParentStudent.setString(1, "N");
                    insertParentStudent.setInt(2, 0);
                    insertParentStudent.setInt(3, 0);
                    insertParentStudent.setInt(4, 0);
                    insertParentStudent.setInt(5, 0);

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
