package gameClient;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDB {
    public static final String jdbcUrl="jdbc:mysql://db-mysql-ams3-67328-do-user-4468260-0.db.ondigitalocean.com:25060/oop?useUnicode=yes&characterEncoding=UTF-8&useSSL=false";
    public static final String jdbcUser="student";
    public static final String jdbcUserPassword="OOP2020student";
    public static final int ID= 208551374;

    /**
     * main for testing
     * @param args
     */
    public static void main(String[] args) {
        int id2 = 12345678;
        int level = 0;//1,2,3
        System.out.println(getInfo());
    }

    public static String getInfo(){
        String info = null;
        // arrays containing required scores and maximum moves to pass accordingly
        int[] passScore = {145, 450, 0, 720, 0, 570, 0, 0, 0, 510, 0, 1050, 0, 310, 0, 0, 235, 0, 0, 250, 200, 0, 0, 1000};
        int[] passMoves = {290, 580, Integer.MAX_VALUE, 580, Integer.MAX_VALUE, 500, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, 580, Integer.MAX_VALUE, 580, Integer.MAX_VALUE, 580, Integer.MAX_VALUE, Integer.MAX_VALUE,
                290, Integer.MAX_VALUE, Integer.MAX_VALUE, 580, 290, Integer.MAX_VALUE, Integer.MAX_VALUE, 1140};
        int gamesPlayed = 0;
        int currentLevel = 0;
        // will put best result (that doesn't exceed move count) here
        int[] bestResults = new int[24];
        try {
            // connect to server
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection =
                    DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
            Statement statement = connection.createStatement();
            String allCustomersQuery = "SELECT * FROM Logs where userID="+ ID;
            // get all the results
            ResultSet resultSet = statement.executeQuery(allCustomersQuery);
            // go over all the results and save relevant data
            while(resultSet.next()) {
                int level = resultSet.getInt("levelID");
                int score = resultSet.getInt("score");
                int moves = resultSet.getInt("moves");
                if(passMoves[level] >= moves) {
                    if (passScore[level] < score) {
                        // check if this score is higher than what we have
                        if (bestResults[level] < score) {
                            bestResults[level] = score;
                        }
                    }
                }

                // count games
                gamesPlayed++;
                // save highest level played
                if(level > currentLevel)
                    currentLevel = level;


            }

            // convert the info into string form
            info = "***GAME STATS***" +"\n" + "Games played: " + gamesPlayed + "\n" +"Current level: " + currentLevel +"\n";
            info += "Your best results:\n" + "| level : result | ";
            for(int i = 0; i < bestResults.length; i++){
                info += i + " : " + bestResults[i] + " | ";
            }
            resultSet.close();
            statement.close();
            connection.close();
        }

        catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("Vendor Error: " + sqle.getErrorCode());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

}

