    package com.company;

    import java.math.BigDecimal;
    import java.sql.*;
    import java.util.ArrayList;

    public class migrateGrade {
        protected void performMigration() {
            Connection connection_old_db = null;
            Connection connection_new_db = null;

            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                connection_old_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management?&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_old_db.setAutoCommit(false);

                PreparedStatement retrieveGradeData = connection_old_db.prepareStatement
                        ("SELECT DISTINCT gradeRemark, gradeType, maxScore, minScore, schoolCode, template "
                                        + "FROM Grade WHERE schoolCode = 'SG' GROUP BY gradeRemark, gradeType, maxScore, minScore, schoolCode, template ORDER BY schoolCode",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet retrieveGradeDataRS = retrieveGradeData.executeQuery();

                connection_new_db = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/school_management_new?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", "root", "Ayodele$3");
                connection_new_db.setAutoCommit(false);

                PreparedStatement retrieveLevelData = connection_new_db.prepareStatement
                        ("SELECT id FROM level WHERE sid = 39",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet retrieveLevelDataRS = retrieveLevelData.executeQuery();

                PreparedStatement insertGrade = null;
                insertGrade = connection_new_db.prepareStatement
                        ("INSERT INTO grade (created_on, del_flag, version, sid, accuracy, description, is_default, name) "
                                        + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                PreparedStatement insertGradeScore = null;
                insertGradeScore = connection_new_db.prepareStatement
                        ("INSERT INTO grade_score (created_on, del_flag, version, sid, max_score, min_score, name, remarks) "
                                        + "VALUES (CURRENT_TIMESTAMP,?,?,?,?,?,?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                PreparedStatement insert_grade_gradeScore = null;
                insert_grade_gradeScore = connection_new_db.prepareStatement
                        ("INSERT INTO grade_grade_scores (grade_id, grade_scores_id) "
                                        + "VALUES (?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                PreparedStatement insertGradeLevel = null;
                insertGradeLevel = connection_new_db.prepareStatement
                        ("INSERT INTO grade_levels (grade_id, levels_id) "
                                        + "VALUES (?,?)",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                ArrayList<String> grades = new ArrayList<>();

                retrieveGradeDataRS.beforeFirst();
                while(retrieveGradeDataRS.next()) {
                    int gradeId = 0;
                    if (!grades.contains(retrieveGradeDataRS.getString("template")+" Grade")) {
                        System.out.println("doesnt contain grade: create");
                        grades.add(retrieveGradeDataRS.getString("template")+" Grade");

                        insertGrade.setString(1, "N");
                        insertGrade.setInt(2, 0);
                        insertGrade.setInt(3, 39);
                        insertGrade.setBigDecimal(4, BigDecimal.valueOf(0.10));
                        insertGrade.setString(5, retrieveGradeDataRS.getString("template")+" Grade");
                        insertGrade.setNull(6, Types.NULL);
                        insertGrade.setString(7, retrieveGradeDataRS.getString("template")+" Grade");

                        insertGrade.executeUpdate();

                        ResultSet gradeIDRS = insertGrade.getGeneratedKeys();
                        if (gradeIDRS.next()) {
                            gradeId = gradeIDRS.getInt(1);
                        }
                        System.out.println("id of created = "+gradeId);
                        retrieveLevelDataRS.beforeFirst();
                        while(retrieveLevelDataRS.next()) {
                            insertGradeLevel.setInt(1, gradeId);
                            insertGradeLevel.setInt(2, retrieveLevelDataRS.getInt("id"));
                            System.out.println("insert gradeId "+gradeId+" with levelId "+retrieveLevelDataRS.getInt("id"));
                            insertGradeLevel.executeUpdate();
                        }


                    } else {
                        System.out.println("contains grade: fetch id");
                        PreparedStatement getExistingGrade = connection_new_db.prepareStatement
                                ("SELECT id "
                                        + "FROM grade WHERE sid = 39 AND name = ?");
                        getExistingGrade.setString(1, retrieveGradeDataRS.getString("template")+" Grade");
                        ResultSet getExistingGradeRS = getExistingGrade.executeQuery();

                        getExistingGradeRS.beforeFirst();
                        while (getExistingGradeRS.next()) {
                            gradeId = getExistingGradeRS.getInt("id");
                        }

                        System.out.println("fetched id = "+gradeId);
                    }

                    System.out.println("gradeid = "+gradeId);

                    insertGradeScore.setString(1, "N");
                    insertGradeScore.setInt(2, 0);
                    insertGradeScore.setInt(3, 39);
                    insertGradeScore.setDouble(4, Double.valueOf(retrieveGradeDataRS.getString("maxScore")));
                    insertGradeScore.setDouble(5, Double.valueOf(retrieveGradeDataRS.getString("minScore")));
                    insertGradeScore.setString(6, retrieveGradeDataRS.getString("gradeType"));
                    insertGradeScore.setString(7, retrieveGradeDataRS.getString("gradeRemark"));

                    insertGradeScore.executeUpdate();

                    ResultSet gradeScoreIdRS = insertGradeScore.getGeneratedKeys();
                    int gradeScoreId = 0;
                    if (gradeScoreIdRS.next()) {
                        gradeScoreId = gradeScoreIdRS.getInt(1);
                    }

                    insert_grade_gradeScore.setInt(1, gradeId);
                    insert_grade_gradeScore.setInt(2, gradeScoreId);

                    System.out.println("inserting gradeId "+gradeId+" & gradeScoreId "+gradeScoreId);

                    insert_grade_gradeScore.executeUpdate();
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
