package test;

import Server.Game_Server;
import Server.game_service;
import gameClient.Fruit;
import oop_dataStructure.OOP_DGraph;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class test {
    @Test
    public void testFruitComparison(){
        game_service game = Game_Server.getServer(13);
        // initialize game graph from server
        String graphStr = game.getGraph();
        OOP_DGraph graph = new OOP_DGraph();
        graph.init(graphStr);
        String fruitStr1 = game.getFruits().get(0);
        String fruitStr2 = game.getFruits().get(1);

        Fruit f1 = new Fruit(fruitStr1, graph);
        Fruit f1copy = new Fruit(fruitStr1, graph);
        Fruit f2 = new Fruit(fruitStr2, graph);
        assertTrue(f1.equals(f1copy));
        assertFalse(f1.equals(f2));
    }
}
