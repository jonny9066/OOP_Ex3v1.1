package gameClient;

import Server.Game_Server;
import Server.game_service;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import org.json.JSONException;
import org.json.JSONObject;
import utils.StdDraw;

import java.util.*;
import java.util.List;

public class MyGameGUI implements Runnable {
    /**
     * Main function is used for testing purposes
     * @param args
     */
    public static void main(String[] args){
        //runCompetitionLevels();
        Thread game = new Thread(new MyGameGUI(7), "Game");
        game.start();
    }
    // initialize game
    private MyGameGUI(int level) {
        int myID = 208551374;
        Game_Server.login(myID);
        game = Game_Server.getServer(level);
        // initialize game graph from server
        String g = game.getGraph();
        gg = new OOP_DGraph();
        gg.init(g);
        // initialize a class that draws game and logs in KML
        painter_logger = new painterAndLogger(gg, level, game);
    }

    @Override
    // a thread that is running while the robots are moving
    public void run() {
        int sleepTime = 85;
        addRobots();
        game.startGame();
        // initialize Robot objects
        initializeRobots(game.getRobots());
        System.out.println("Starting game!\nPlaying...");
        // playing
        while (game.isRunning()) {
            game.move();
            // update src, dest and pos of robot objects
            updateRobots(game.getRobots());
            // iterate over robots and give each robot that is not moving a next node
            Collection<Robot> allRobots = robots.values();
            Iterator<Robot> itr = allRobots.iterator();
            while (itr.hasNext()) {
                Robot robot = itr.next(); // get next robot
                // check that robot is not moving
                if(robot.getDest() == -1){
                    // convert fruits to objects, this also finds the edges fruit are on
                    List<Fruit> fruits = tools.fruitsFromJsonToObject(game.getFruits(), gg);
                    // get path to best value fruit and the chosen fruit
                    PathAndFruit paf = Robot_Algs.pathToBestFruit(robot, fruits, gg);
                    robot.setPath(paf.getPath());
                    robot.setTargetFruit(paf.getFruit());
                    game.chooseNextEdge(robot.getId(), robot.getNextNode());
                }
            }
            // draw
            StdDraw.clear();
            painter_logger.drawAndLog();
            StdDraw.show();
            // pause thread
            try {
                Thread.sleep(sleepTime);
            }catch (InterruptedException ie){
                System.out.println("Thread exception!");
            }
        }
        // send kml
        String kml = painter_logger.saveAndGetKML();
        game.sendKML(kml);
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

        int[] score_moves = new int[2];
        score_moves[0] = grade;
        score_moves[1] = moves;

        System.out.println("Game over!\nScore is: " + score_moves[0] + "\nNumber of moves is: " + score_moves[1] );
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
            Thread game = new Thread(gameGUI, "Level " + String.valueOf(i));
            game.start();
            // check whether level needs to be passed
            if(i == passTable[j][0]){
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
                // check if we passed
                if(grade > passTable[j][1] && moves < passTable[j][2]){
                    j++;
                }
                else{
                    System.out.println("You failed level: " + i + "\n Your score: " +grade
                    +"\nRequired score: " + passTable[j][1] +"\nYour number of moves: "+ moves +
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






    private void addRobots(){
        boolean canAdd = true;
        List<Fruit> fruits = tools.fruitsFromJsonToObject(game.getFruits(), gg);
        while(canAdd) {
            double bestValue = -1;
            Fruit bestFruit = null;
            Iterator<Fruit> itr = fruits.iterator();
            while (itr.hasNext()) {
                Fruit fruit = itr.next();
                if(fruit.getValue() > bestValue){
                    bestFruit = fruit;
                    bestValue = fruit.getValue();
                }
            }
            canAdd = game.addRobot(bestFruit.getSrc());
            if(fruits.size() > 1)
                fruits.remove(bestFruit);
        }
    }
    private void initializeRobots(List<String> robots_json){
        robots = new HashMap<>();
        try {
            for (int i = 0; i < robots_json.size(); i++) {
                JSONObject r = new JSONObject(robots_json.get(i));
                int id = r.getJSONObject("Robot").getInt("id");
                robots.put(id, new Robot(robots_json.get(i)));
            }
        }catch(JSONException e){
            System.out.println(e);
        }
    }
    // get every robot from the hash map and update its dest, src and pos with corresponding string
    private void updateRobots(List<String> robots_json){
        try {
            for (int i = 0; i < robots_json.size(); i++) {
                JSONObject r = new JSONObject(robots_json.get(i));
                int id = r.getJSONObject("Robot").getInt("id");
                robots.get(id).updateCoordinates(robots_json.get(i));
            }
        }catch(JSONException e){
            System.out.println(e);
        }
    }
    private String getGameInfo(){
        return game.toString();
    }



    // private data
    private OOP_DGraph gg;
    private game_service game;
    HashMap<Integer, Robot> robots;
    painterAndLogger painter_logger;



}