    package com.company;

    import java.math.BigDecimal;
    import java.sql.*;
    import java.util.ArrayList;

    public class migrateAssessment {
        protected void performMigration() {
            Connection connection_old_db = null;
            Connection connection_new_db = null;

            try
            {
                Class.forName("com.mysql.jdbc.Driver");

                connection_old_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_old_db.setAutoCommit(false);

                connection_new_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_new_db.setAutoCommit(false);

                PreparedStatement retrieveGradeData = connection_old_db.prepareStatement
                        ("SELECT DISTINCT gradeRemark, gradeType, maxScore, minScore, schoolCode, template "
                                        + "FROM Grade WHERE schoolCode = 'SG' GROUP BY gradeRemark, gradeType, maxScore, minScore, schoolCode, template ORDER BY schoolCode",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet retrieveGradeDataRS = retrieveGradeData.executeQuery();

                PreparedStatement retrieveLevelData = connection_new_db.prepareStatement
                        ("SELECT id FROM level WHERE sid = 39",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet retrieveLevelDataRS = retrieveLevelData.executeQuery();

                PreparedStatement insertGrade = null;
                insertGrade = connection_new_db.prepareStatement
                        ("INSERT INTO grade (created_on, del_flag, version, sid, accuracy, description, is_default, name) "
                                        + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                retrieveGradeDataRS.beforeFirst();
                while(retrieveGradeDataRS.next()) {

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

        private void getTemplates(String examCode, int schoolId) {
            try {
                Class.forName("com.mysql.jdbc.Driver");

                Connection connection_old_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_old_db.setAutoCommit(false);

                Connection connection_new_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_new_db.setAutoCommit(false);

                PreparedStatement retrieveData = connection_old_db.prepareStatement
                        ("SELECT * FROM Config WHERE template = ? AND school_schoolId = ?",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet retrieveDataRS = retrieveData.executeQuery();
                retrieveData.setString(1, examCode);
                retrieveData.setInt(2, schoolId);

                retrieveDataRS.beforeFirst();
                while (retrieveDataRS.next()) {

                }

            } catch (Exception ex) {

            }

        }

//        private void getTemplate()
    }
