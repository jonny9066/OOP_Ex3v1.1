package gameClient;

import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Robot_Algs {
    public static List<oop_node_data> pathToBestFruit(Robot robot, List<Fruit> fruits, OOP_DGraph gg) {
        List<oop_node_data> path = new ArrayList<>();
        DijkstraAlg(gg, robot.getSrc());
        double bestValue = -1;
        Fruit bestFruit = null;
        Iterator<Fruit> itr = fruits.iterator();
        // set first fruit to be the best
        if(itr.hasNext()){
            bestFruit = itr.next();
            bestValue = calculateValue(gg.getNode(bestFruit.getSrc()).getWeight(), bestFruit.getValue());
        }
        // go over all fruit to see which has the highest *value
        // we calculate a value that is different from the regular value
        while (itr.hasNext()) {
            Fruit tempFruit = itr.next();
            double tempValue = calculateValue(gg.getNode(tempFruit.getSrc()).getWeight(), tempFruit.getValue());
            if (tempValue > bestValue) {
                bestFruit = tempFruit;
                bestValue = tempValue;
            }
        }

        if (bestFruit == null) {
            throw new RuntimeException("Function got no fruit!");
        }
        // first add dest, then src, then the other nodes on path
        oop_node_data n = gg.getNode(bestFruit.getDest());
        path.add(n);
        // check that robot isn't already on the fruit edge
        if (robot.getSrc() != bestFruit.getSrc()) {
            n = gg.getNode(bestFruit.getSrc());
            path.add(n);
            // Equivalent to: while n has predecessor. Elements are added from destination to source
            while (n.getTag() != -1) {
                n = gg.getNode(n.getTag());
                path.add(n);
            }
            // remove node robot is on
            path.remove(path.size() - 1);
            // reverse the order of S, because we inserted the nodes in reverse order
            Collections.reverse(path);
        }

        return path;
    }
    private static double calculateValue(double value, double weight){
        return value/weight;
    }


    private static void initializeSingleSource(OOP_DGraph graph, int s) {
        oop_node_data n;
        Iterator<oop_node_data> itr = graph.getV().iterator();
        while (itr.hasNext()) {
            n = itr.next();
            n.setTag(-1);
            n.setWeight(Integer.MAX_VALUE);
        }
        // Set the weight of the source node to 0.
        n = graph.getNode(s);
        n.setWeight(0);
    }

    /*
    Relaxes an edge with the following source and destination:
    If the weight of d is smaller than the weight of getting to it from s through this edge, set it the latter weight
    and set it's predecessor s.
     */
    private static void relaxEdge(oop_node_data s, oop_node_data d, double w) {
        if (d.getWeight() > s.getWeight() + w) {
            // Set new weight.
            d.setWeight(s.getWeight() + w);
            // Set predecessor to s.
            d.setTag(s.getKey());
        }
    }

    /*
    ****ALGORITHM TAKEN FROM THE BOOK "INTRODUCTION TO ALGORITHMS" ***
    Initialize single source, add all nodes to queue - Q, remove the minimal node (in the first run it's s),
    relax its adjacent edges and repeat until Q is empty.
     */
    private static void DijkstraAlg(OOP_DGraph g, int s) {
        oop_node_data n;
        oop_edge_data e;
        ArrayList<oop_node_data> Q = new ArrayList<>();
        Iterator<oop_edge_data> adj;
        Iterator<oop_node_data> nodes;
        initializeSingleSource(g, s);
        // Fill Q with nodes of g.
        nodes = g.getV().iterator();
        while (nodes.hasNext()) {
            Q.add(nodes.next());
        }
        // While queue is not empty.
        while (!Q.isEmpty()) {
            // Get and remove the node with the smallest weight from Q, add it to S.
            n = qGetAndRemoveMin(Q);
            // Relax all the edges going from n.
            adj = g.getE(n.getKey()).iterator();
            while (adj.hasNext()) {
                e = adj.next();
                relaxEdge(g.getNode(e.getSrc()), g.getNode(e.getDest()), e.getWeight());
            }
        }
    }

    private static oop_node_data qGetAndRemoveMin(ArrayList<oop_node_data> q) {
        oop_node_data n, min;
        Iterator<oop_node_data> itr = q.iterator();
        min = itr.next();
        while (itr.hasNext()) {
            n = itr.next();
            if (n.getWeight() < min.getWeight()) {
                min = n;
            }
        }
        q.remove(min);
        return min;
    }
}
