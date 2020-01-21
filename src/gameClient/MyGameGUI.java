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
        MyGameGUI mg = new MyGameGUI(23);
        mg.playGame();
        //runCompetitionLevels();
    }
    // initialize game
    private MyGameGUI(int level) {
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
    private static void runCompetitionLevels(){
        // int[x][y], x is for case, y is for {stage, grade and moves}
        int[][] passTable = {{0, 145, 290}, {1, 450, 580}, {3, 720, 580}, {5, 570, 500}, {9, 510, 580},
                {11, 1050, 580}, {13, 310, 580}, {16, 235, 290}, {19, 250, 580}, {20, 200, 290}, {23, 1000, 1140}};
        // i is current level, j is next level from the table to be passed
        int j = 0, i = 0;
        while(i < 24) {
            MyGameGUI gameGUI = new MyGameGUI(i);
            gameGUI.playGame();
            // check whether level needs to be passed
            if(i == passTable[j][0]){
                int[] scoreMoves = gameGUI.getScoreMoves();
                // check if we passed
                if(scoreMoves[0] > passTable[j][1] && scoreMoves[1] > passTable[j][2]){
                    j++;
                }
                else{
                    System.out.println("You failed level: " + i + "\n Your score: " +scoreMoves[0]
                    +"\nRequired score: " + passTable[j][1] +"\nYour number of moves: "+ scoreMoves[1] +
                            "\nRequired number of moves: " + passTable[j][2]);
                    break;
                }
            }
            i++;

        }
        if(i == 24){
            System.out.println("You passed all levels!!!");
        }
    }



    // place robots
    // when a robot is not moving find it a next node
    // save kml, score and moves
    public void playGame() {
        // add robots
        while(!game.isRunning()) {
            List<String> fruit = game.getFruits();
            Iterator<String> itr = fruit.iterator();
            while (itr.hasNext()) {
                oop_edge_data e = Robot_Algs.findFruitEdge(itr.next(), gg);
                if (e != null) {
                    game.addRobot(e.getSrc());
                    game.startGame();
                }
            }
        }
        // initialize Robot objects
        initializeRobots(game.getRobots());

        System.out.println("Starting game!\nPlaying...");
        // playing
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
                        // get corresponding robot object
                        Robot rObj = robots.get(id);
                        // try to get the next fruit on its path
                        oop_node_data nn = rObj.getNextNode();//Robot_Algs.nextNode(robots_json.get(i), game.getFruits(), game.getGraph());
                        if(nn != null){
                            game.chooseNextEdge(id, nn.getKey());
                        }
                        // if there's no next node on path, the fruit must've been reached
                        // so we get it a new path to an *available* fruit
                        else{
                            List<String> availableFruits = game.getFruits();
                            //System.out.println(availableFruits);
                            // get an iterator over Robots
                            Collection<Robot> robotsList = robots.values();
                            Iterator<Robot> itr = robotsList.iterator();
                            // remove all the fruits that are chased by other robots from the list of available fruits
                            while (itr.hasNext()){
                                availableFruits.remove(itr.next().getTargetFruitJSON());
                            }
                            // set Robot a new path
                            PathAndTarget pt = Robot_Algs.pathToClosestFruit(robots_json.get(i),
                                    availableFruits, game.getGraph());
                            rObj.setPath(pt.getPath());
                            rObj.setTargetFruitJSON(pt.getTarget());
                            // no need to move, will move next time
                        }
                    }

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }
            }
            // draw
            StdDraw.clear();
            drawAndLog(gg, game, log, eps, kml_log, textPos);
            StdDraw.show();
            StdDraw.pause(20);
        }
        // send kml
        game.sendKML(kml_log.getKML());
        //get score and moves
        int grade = -1;
        int moves = -1;
        try {
            JSONObject results = new JSONObject(game.toString());
            moves = results.getJSONObject("GameServer").getInt("moves");
            grade = results.getJSONObject("GameServer").getInt("grade");

        }catch(Exception e){
            System.out.println(e);
        }
        score_moves = new int[2];
        score_moves[0] = grade;
        score_moves[1] = moves;



        System.out.println("Game over!\nScore is: " + score_moves[0] + "\nNumber of moves is: " + score_moves[1] );
    }



    public static void drawAndLog(OOP_DGraph gg, game_service game, List<String> robots_json, double eps,
                                  KML_Logger kml_log, OOP_Point3D topMid) {
        Iterator<oop_node_data> itr1;
        Iterator<oop_edge_data> itr2;
        oop_node_data n;
        oop_edge_data e;
        OOP_Point3D p1, p2, p3;
        // Draw nodes
        itr1 = gg.getV().iterator();
        while (itr1.hasNext()) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.02);
            n = itr1.next();
            p1 = n.getLocation();
            // Actually draw
            StdDraw.point(p1.x(), p1.y());
            // Annotate
            StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
            StdDraw.text(p1.x(), p1.y() + eps*0.5, "" + n.getKey());
        }
        // draw edges
        itr1 = gg.getV().iterator();
        while (itr1.hasNext()) {
            n = itr1.next();
            itr2 = gg.getE(n.getKey()).iterator();
            while (itr2.hasNext()) {
                e = itr2.next();
                p1 = gg.getNode(e.getSrc()).getLocation();
                p2 = gg.getNode(e.getDest()).getLocation();
                // Actually draw
                StdDraw.setPenColor(StdDraw.BLACK);
                StdDraw.setPenRadius(0.0025);
                StdDraw.line(p1.x(), p1.y(), p2.x(), p2.y());
                // draw directional dots
                StdDraw.setPenColor(StdDraw.YELLOW);
                StdDraw.setPenRadius(0.01);
                p3 = new OOP_Point3D(0.25 * p1.x() + 0.75 * p2.x(), 0.25 * p1.y() + 0.75 * p2.y());
                StdDraw.point(p3.x(), p3.y());



            }
        }
        // draw fruit and log in kml
        java.util.List<String> fruit = game.getFruits();
        for (int i = 0; i < fruit.size(); i++) {
            try {
                JSONObject f = new JSONObject(fruit.get(i));
                String pos = f.getJSONObject("Fruit").getString("pos");
                int type = f.getJSONObject("Fruit").getInt("type");
                String[] xAndY = pos.split(",");
                p1 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                // type is dest - src of fruit's edge, if negative then src > dest and we mark it banana
                if(type < 0){
                    StdDraw.picture(p1.x(), p1.y(), "banana.png", 0.0004, 0.0003);
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Banana");
                }
                else {
                    StdDraw.picture(p1.x(), p1.y(), "apple.png", 0.0004, 0.0004);
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Apple");
                }

            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }

        }
        // draw robots and log in kml
        if(robots_json != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(topMid.x(), topMid.y() , "Scores:");
            for (int i = 0; i < robots_json.size(); i++) {
                try {
                    JSONObject r = new JSONObject(robots_json.get(i));
                    String pos = r.getJSONObject("Robot").getString("pos");
                    int score = r.getJSONObject("Robot").getInt("value");
                    int id = r.getJSONObject("Robot").getInt("id");
                    String[] xAndY = pos.split(",");
                    p1 = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
                    StdDraw.setPenColor(intToColor(i));
                    StdDraw.setPenRadius(0.004);
                    StdDraw.circle(p1.x(), p1.y(), 0.00015);
                    // draw score for robot
                    StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
                    StdDraw.text(topMid.x(), topMid.y() - eps*(id+1), "Robot "+id+": "+score+" points");
                    // log
                    kml_log.addMovingPlacemark(p1.x(), p1.y(), "Robot "+ String.valueOf(id));

                } catch (JSONException jsonException) {
                    System.out.println(jsonException);
                }

            }
        }
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
        StdDraw.text(topMid.x(), topMid.y() + eps, String.valueOf(game.timeToEnd()));

    }
    private static Color intToColor(int i){
        int cn = i%8;
        if(i == 0){return StdDraw.CYAN; }
        if(i == 1){return StdDraw.GREEN; }
        if(i == 2){return StdDraw.MAGENTA; }
        if(i == 3){return StdDraw.ORANGE; }
        if(i == 4){return StdDraw.PINK; }
        if(i == 5){return StdDraw.BOOK_BLUE; }
        if(i == 6){return StdDraw.BOOK_LIGHT_BLUE; }
        if(i == 7){return StdDraw.BOOK_RED; }
        else {return StdDraw.PRINCETON_ORANGE; }
    }

    // gets coordinate range, adds margin, sets position of text and defines epsilon
    private void setWindowParams(){
        double xMax = 0;
        double xMin = 0;
        double yMax = 0;
        double yMin = 0;
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
        double XYRatio = xLen/yLen;
        eps = xLen*0.01;
        textPos = new OOP_Point3D((xMax+xMin)/2, yMax - 2*eps);
        // set parameters in StdDraw
        StdDraw.enableDoubleBuffering();
        StdDraw.setCanvasSize((int)(500 * XYRatio), 500);
        StdDraw.setXscale(xMin, xMax);
        StdDraw.setYscale(yMin, yMax);
    }
    public String getKML(){
        return kml_log.getKML();
    }
    public  int[] getScoreMoves(){
        return score_moves;
    }
    private void initializeRobots(List<String> robots_json){
        robots = new HashMap<>();
        for (int i = 0; i < robots_json.size(); i++) {
            try {
                // get robot info
                JSONObject r = new JSONObject(robots_json.get(i));
                int id = r.getJSONObject("Robot").getInt("id");
                robots.put(id, new Robot(id));
            } catch (JSONException jsonException) {
                System.out.println(jsonException);
            }
        }
    }

    // private data
    private OOP_DGraph gg;
    private game_service game;
    private KML_Logger kml_log;
    private int[] score_moves;
    HashMap<Integer, Robot> robots;
    private final int id = 208551374;

    // hundredth of x length
    private double eps;
    // position for text on screen
    private OOP_Point3D textPos;
}