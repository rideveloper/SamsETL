package com.company;

import java.math.BigDecimal;
import java.sql.*;

public class GradeTograde_score2 {
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
                    ("jdbc:mysql://localhost:3306/sams-live?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_old_db.setAutoCommit(false);

            // BEFORE GENERATING STAFF ID CHECK FOR UNIQUENESS OF NAMES
            PreparedStatement retrieveOldData = connection_old_db.prepareStatement
                    ("SELECT gradeRemark, gradeType, maxScore, minScore, schoolCode, template "
                                    + "FROM Grade",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet retrieveOldDataRS = retrieveOldData.executeQuery();

            //TARGET DB SETUP
            //CONNECTS TO THE URL OF THE DATABASE
            connection_new_db = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/sams_done?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
            connection_new_db.setAutoCommit(false);

//
            // INSERT INTO TARGET DB OPERATION
            PreparedStatement insertIntoGrade_score = null;
            insertIntoGrade_score = connection_new_db.prepareStatement
                    ("INSERT INTO grade_score (created_on, del_flag, version, sid, max_score, min_score, name, remarks) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            PreparedStatement insertIntoGrade = null;
            insertIntoGrade = connection_new_db.prepareStatement
                    ("INSERT INTO grade (created_on, del_flag, version, sid, accuracy, description, is_default, name) "
                                    + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            PreparedStatement insertIntoGrade_grade_scores = null;
            insertIntoGrade_grade_scores = connection_new_db.prepareStatement
                    ("INSERT INTO grade_grade_scores (grade_id, grade_scores_id) "
                                    + "VALUES (?,?)",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            retrieveOldDataRS.beforeFirst();

            int grade_id = 0;
            ResultSet ifGradeExistsRS = null;
            ResultSet getExistingGradeRS = null;
            while(retrieveOldDataRS.next())
            {
                PreparedStatement retrieveOldDataForSchool = connection_old_db.prepareStatement
                        ("SELECT schoolId "
                                + "FROM School WHERE schoolCode = ?");
                retrieveOldDataForSchool.setString(1, retrieveOldDataRS.getString("schoolCode"));
                ResultSet retrieveOldDataRSForSchool = retrieveOldDataForSchool.executeQuery();

                while (retrieveOldDataRSForSchool.next()) {
                    PreparedStatement ifGradeExists = connection_new_db.prepareStatement
                            ("SELECT EXISTS(SELECT id "
                                    + "FROM grade WHERE sid = ? AND name = ?) AS gradeExists");
                    ifGradeExists.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                    ifGradeExists.setString(2, retrieveOldDataRS.getString("template"));
                    ifGradeExistsRS = ifGradeExists.executeQuery();
                }

                while (ifGradeExistsRS.next()) {
                    if (ifGradeExistsRS.getInt("gradeExists") == 0) {
                        insertIntoGrade.setString(1, "N");
                        insertIntoGrade.setInt(2, 0);

                        if (retrieveOldDataRSForSchool.next()) {
                            insertIntoGrade.setInt(3, retrieveOldDataRSForSchool.getInt("schoolId"));
                        } else
                            insertIntoGrade.setNull(3, Types.NULL);

                        insertIntoGrade.setBigDecimal(4, BigDecimal.valueOf(0.10));
                        insertIntoGrade.setString(5, retrieveOldDataRS.getString("template")+" Grade");
                        insertIntoGrade.setNull(6, Types.NULL);
                        insertIntoGrade.setString(7, retrieveOldDataRS.getString("template"));

                        insertIntoGrade.executeUpdate();
                        ResultSet rs = insertIntoGrade.getGeneratedKeys();
                        int generatedKey = 0;
                        if (rs.next()) {
                            grade_id = generatedKey = rs.getInt(1);
                        }
                    } else {
                        PreparedStatement getExistingGrade = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM grade WHERE WHERE sid = ? AND name = ?");
                        getExistingGrade.setInt(1, retrieveOldDataRSForSchool.getInt("schoolId"));
                        getExistingGrade.setString(2, retrieveOldDataRS.getString("template"));
                        getExistingGradeRS = getExistingGrade.executeQuery();

                        while (getExistingGradeRS.next()) {
                            grade_id = getExistingGradeRS.getInt("id");
                        }
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
