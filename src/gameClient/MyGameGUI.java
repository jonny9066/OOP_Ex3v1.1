package gameClient;

import Server.Game_Server;
import Server.game_service;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import org.json.JSONException;
import org.json.JSONObject;
import oop_utils.OOP_Point3D;
import utils.StdDraw;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class MyGameGUI {
    /**
     * Main function is used for testing purposes
     * @param args
     */
    public static void main(String[] args){
        MyGameGUI mg = new MyGameGUI(0);
        mg.start();
    }
    // initialize game
    private MyGameGUI(int level) {
        int id = 208551374;
        Game_Server.login(id);
        game = Game_Server.getServer(level);
        // initialize game graph from server
        String g = game.getGraph();
        gg = new OOP_DGraph();
        gg.init(g);
        // initialize kml logger
        kml_log = new KML_Logger(String.valueOf(level), gg);
        setWindowParams();

    }

    // run levels 0, 1, 3, ..., 23, compare scores and save to DB
    private void runCompetitionLevels(){
        // int[x][y], x is for case, y is for {stage, grade and moves}
        int[][] passTable = {{0, 145, 290}, {1, 450, 580}, {3, 720, 580}, {5, 570, 500}, {9, 510, 580},
                {11, 1050, 580}, {13, 310, 580}, {16, 235, 290}, {19, 250, 580}, {20, 200, 290}, {23, 1000, 1140}};
        for (int i = 0; i <passTable.length ; i++) {
            MyGameGUI gameGUI = new MyGameGUI(passTable[i][0]);

        }
    }




    private void start() {
        // enable to draw off screen
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize((int)(500 * XYRatio), 500);
        StdDraw.setXscale(xMin, xMax);
        StdDraw.setYscale(yMin, yMax);

        addRobots();
        int[] scoreMoves = play();
        System.out.println("Score is: " + scoreMoves[0] + "\nNumber of moves is: " + scoreMoves[1] );

        // get kml string
        String kml_str = kml_log.getKML();

    }


    // add robots close to fruit
    private void addRobots(){
        while(!game.isRunning()) {
            List<String> fruit = game.getFruits();
            Iterator<String> itr = fruit.iterator();
            while (itr.hasNext()) {
                oop_edge_data e = Robot_Algs.findFruitEdge(itr.next(), gg);
                if (e != null) {
                    boolean added = game.addRobot(e.getSrc());
                    game.startGame();
                    if (added) {
                        System.out.println("Robot added on node " + e.getSrc());
                    } else
                        break;
                }

            }
        }

        // in case there are no fruit we add the robots manually
        while(!game.isRunning()){
            for (int i = 0; i < gg.getV().size(); i++) {
                boolean added = game.addRobot(i);
                game.startGame();
                if (added) {
                    System.out.println("Robot added on node " + i);
                } else
                    break;
            }
        }

        System.out.println("Added all robots.");

    }
    //Go over robots, for every robot that is not moving get next move using function
    // returns score
    private int[] play(){
        int moves = 0;
        while (game.isRunning()) {
            List<String> log = game.move();
            // get robots from game
            List<String> robots_json = game.getRobots();
            // go over robots
            for (int i = 0; i < robots_json.size(); i++) {
                try {
                    // get robot info
                    JSONObject r = new JSONObject(robots_json.get(i));
                    int id = r.getJSONObject("Robot").getInt("id");
                    int dest = r.getJSONObject("Robot").getInt("dest");
                    // check that robot is not moving
                    if(dest == -1){
                        // get path for this robot
                        // move
                        oop_node_data nn = Robot_Algs.nextNode(robots_json.get(i), game.getFruits(), game.getGraph());
                        long move = game.chooseNextEdge(id, nn.getKey());
                        // check that robot moved
                        if(move != -1) {
                            moves ++;
                            //System.out.println("Next node for " + id + " is " + nn.getKey());
                        }
                        else
                            System.out.println("Robot " +id + " tries to go to node "
                                    + nn.getKey() + ", but can't!");
                    }

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }
            }
            // draw
            StdDraw.clear();
            painterAndLogger.drawAndLog(gg, game, log, eps, kml_log, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
        //get score
        int totalScore = 0;
        List<String> robots_json = game.getRobots();
        // go over robots
        for (int i = 0; i < robots_json.size(); i++) {
            try {
                // get robot info
                JSONObject r = new JSONObject(robots_json.get(i));
                int score = r.getJSONObject("Robot").getInt("value");
                totalScore+= score;
            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }
        }
        //System.out.println("Final score is " + totalScore);
        int[] scoreMoves = {totalScore, moves};
        return scoreMoves;

    }

    // gets coordinate range, adds margin, sets position of text and defines epsilon
    private void setWindowParams(){
        Iterator<oop_node_data> itr = gg.getV().iterator();
        OOP_Point3D l;
        if(itr.hasNext()){
            l = itr.next().getLocation();
            xMax = l.x();
            xMin = l.x();
            yMax = l.y();
            yMin = l.y();
        }
        while (itr.hasNext()){
            l = itr.next().getLocation();
            if(l.x() > xMax){
                xMax = l.x();
            }
            else if(l.x() < xMin){
                xMin = l.x();
            }
            if(l.y() > yMax){
                yMax = l.y();
            }
            else if(l.y() < yMin){
                yMin = l.y();
            }
        }
        // get lengths of coordinate axises
        double xLen = xMax - xMin;
        double yLen = yMax - yMin;
        // add margin
        double xMargin = 0.05*xLen;
        double yMargin = 0.05*yLen;
        xMax = xMax + xMargin;
        xMin = xMin - xMargin;
        yMax = yMax + yMargin;
        yMin = yMin - yMargin;
        xLen = xMax - xMin;
        yLen = yMax - yMin;
        // set useful parameters
        XYRatio = xLen/yLen;
        eps = xLen*0.01;
        textPos = new OOP_Point3D((xMax+xMin)/2, yMax - 2*eps);
    }

    // private data
    private OOP_DGraph gg;
    private game_service game;
    private KML_Logger kml_log;

    private double xMax;
    private double xMin;
    private double yMax;
    private double yMin;
    // x/y
    private double XYRatio;
    // hundredth of x length
    private double eps;
    // position for text on screen
    private OOP_Point3D textPos;
}