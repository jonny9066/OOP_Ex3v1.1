package gameClient;

import Server.Game_Server;
import Server.game_service;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import org.json.JSONException;
import org.json.JSONObject;
import utils.StdDraw;

import javax.swing.*;
import java.util.*;
import java.util.List;

public class MyGameGUI implements Runnable {
    /**
     * Main function is used for testing purposes
     * @param args
     */
    public static void main(String[] args){
        int level = 0;
        int rest = 0;
        try {
            JFrame f;
            f = new JFrame("Enter level");
            level = Integer.parseInt(JOptionPane.showInputDialog(f, "Enter level between 0 and 23"));
            rest = Integer.parseInt(JOptionPane.showInputDialog(f, "Enter sleep time in ms, or 0 to use default"));
            if(level > 23 || level < 0)
                throw new RuntimeException();
        } catch (Exception e) {
            JFrame f = new JFrame();
            JOptionPane.showMessageDialog(f, "Bad input", "Alert", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(rest >400 || rest < 0){
                rest = 80;
        }
        Thread game = new Thread(new MyGameGUI(level, rest), "Game " + level);
        game.start();

    }
    // initialize game
    private MyGameGUI(int level, int rest) {
        int myID = 208551374;
        Game_Server.login(myID);
        game = Game_Server.getServer(level);
        this.rest = rest;
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
        addRobots();
        game.startGame();
        System.out.println("Starting game!\nPlaying...");
        // initialize Robot objects
        initializeRobots(game.getRobots());
        //make a list of fruits that robots are after
        List<Fruit> forbiddenFruits = new ArrayList<>();
        // playing
        while (game.isRunning()) {
            game.move();
            // update src, dest and pos of robot objects
            updateRobots(game.getRobots());
            // iterate over robots from smallest to larges id
            Iterator<Robot> itr = robots.values().iterator();
            while (itr.hasNext()) {
                Robot robot = itr.next(); // get next robot by id
                // check that robot is not moving
                if(robot.getDest() == -1){
                    // remove the robot's target fruit from list of target fruit
                    forbiddenFruits.remove(robot.getTargetFruit());
                    // convert fruits to objects, this also finds the edges fruit are on
                    List<Fruit> fruits = tools.fruitsFromJsonToObject(game.getFruits(), gg);
                    // get path to best value fruit and fruit itself
                    PathAndFruit paf = Robot_Algs.pathToBestFruit(robot, fruits, forbiddenFruits, gg);
                    robot.setPath(paf.getPath());
                    robot.setTargetFruit(paf.getFruit());
                    // add fruit to list of target fruits
                    forbiddenFruits.add(paf.getFruit());

                    game.chooseNextEdge(robot.getId(), robot.getNextNode());
                }
            }
            // draw
            StdDraw.clear();
            painter_logger.drawAndLog();
            StdDraw.show();
            // pause thread
            try {
                Thread.sleep(rest);
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
    private static void runAllLevels(){
        for (int i = 0; i < 24; i++) {
            Thread game = new Thread(new MyGameGUI(i, 12), "Game" + i);
            game.start();
            while (game.isAlive()){

            }
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
    private int rest;
    HashMap<Integer, Robot> robots;
    painterAndLogger painter_logger;



}