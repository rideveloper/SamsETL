package com.company;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class SesionHistoryToSchoolEvent {

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
            //ex.printStackTrace();
            return null;
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
                    ("SELECT FirstTermstartDate,firstTermendDate,schoolCode,secondTermendDate,secondTermstartDate,thirdTermendDate,thirdTermstartDate"
                                    + " FROM SesionHistory WHERE schoolCode = 'SG'",
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
                    ("INSERT INTO school_event (created_on, del_flag, version, sid, description, end_date, start_date, title, event_type_id) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            while(retrieveOldDataRS.next())
            {
                for (int i = 0; i <= 2; i++) {
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

                    switch (i) {
                        case 0:
                            insertNewData.setString(4, "First Term");
                            insertNewData.setDate(5, formatDate(retrieveOldDataRS.getString("firstTermendDate")));
                            insertNewData.setDate(6, formatDate(retrieveOldDataRS.getString("FirstTermstartDate")));
                            insertNewData.setString(7, "First Term");
                            insertNewData.setInt(8, 4);
                            break;
                        case 1:
                            insertNewData.setString(4, "Second Term");
                            insertNewData.setDate(5, formatDate(retrieveOldDataRS.getString("secondTermendDate")));
                            insertNewData.setDate(6, formatDate(retrieveOldDataRS.getString("secondTermstartDate")));
                            insertNewData.setString(7, "Second Term");
                            insertNewData.setInt(8, 4);
                            break;
                        case 2:
                            insertNewData.setString(4, "Third Term");
                            insertNewData.setDate(5, formatDate(retrieveOldDataRS.getString("thirdTermendDate")));
                            insertNewData.setDate(6, formatDate(retrieveOldDataRS.getString("thirdTermstartDate")));
                            insertNewData.setString(7, "Third Term");
                            insertNewData.setInt(8, 4);
                            break;
                    }

                    insertNewData.executeUpdate();
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
