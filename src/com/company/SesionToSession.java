package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class SesionToSession {

    public Date formatDate(String date) {
        LocalDate formattedDate = null;
        ZoneId defaultZoneId = ZoneId.systemDefault();
        SimpleDateFormat oldDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");

        if (date == null)
            return null;

        try {
            formattedDate = LocalDate.parse(newDate.format(oldDate.parse(date)));
        } catch (ParseException ex) {
            return null;
            //ex.printStackTrace();
        }

        return Date.valueOf(formattedDate);
    }

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
                    ("SELECT schoolCode, sesionYear, FirstTermstartDate, thirdTermendDate "
                                    + "FROM Sesion WHERE schoolCode = 'SG'",
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
                    ("INSERT INTO session (created_on, del_flag, version, sid, description, name, end_date, start_date, status) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolCode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                insertNewData.setString(1, "N");
                insertNewData.setInt(2, 0);
                if (retrieveOldDataRSForSchool.next())
                    insertNewData.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));
                else
                    insertNewData.setInt(3, 0);

                insertNewData.setString(4, retrieveOldDataRS.getString("sesionYear"));
                insertNewData.setString(5, retrieveOldDataRS.getString("sesionYear")+" Session");
                insertNewData.setDate(6, formatDate(retrieveOldDataRS.getString("thirdTermendDate")));
                insertNewData.setDate(7, formatDate(retrieveOldDataRS.getString("FirstTermstartDate")));
                insertNewData.setString(8, "O");

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
