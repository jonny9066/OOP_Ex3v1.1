package gameClient;

import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Robot_Algs {
    public static PathAndTarget pathToClosestFruit(String robot_json, List<String> fruits, String g) {
        List<oop_node_data> path = new ArrayList<>();
        String closest_fruit_string = new String();
        try {
            // create a new graph to run dijkstra on
            OOP_DGraph gg = new OOP_DGraph();
            gg.init(g);
            // get node robot is on
            JSONObject r = new JSONObject(robot_json);
            int source = r.getJSONObject("Robot").getInt("src");
            //System.out.println(r);
            // tag graph nodes with Dijkstra's algorithm
            DijkstraAlg(gg, source);
            // get edge for first fruit, we assume there is at least 1 fruit
            //System.out.println(fruits.get(0));
            oop_edge_data closest_fruit_edge = null;
            // compare with other fruit edges to get closest fruit
            for (int i = 0; i < fruits.size(); i++) {
                oop_edge_data temp_fruit_edge = findFruitEdge(fruits.get(i), gg);
                if (closest_fruit_edge == null) {
                    closest_fruit_edge = temp_fruit_edge;
                    closest_fruit_string = fruits.get(i);
                }
                if (temp_fruit_edge != null) {
                    double smallestWeight = gg.getNode(closest_fruit_edge.getSrc()).getWeight();
                    double newWeight = gg.getNode(temp_fruit_edge.getSrc()).getWeight();
                    if (smallestWeight > newWeight)
                        closest_fruit_edge = temp_fruit_edge;
                        closest_fruit_string = fruits.get(i);
                }

            }
            if (closest_fruit_edge == null) {
                throw new RuntimeException("Failed to find any fruit!");
            }
            //System.out.println("For fruit on edge (" + closest_fruit_edge.getSrc() + ", " + closest_fruit_edge.getDest()+ ")");
            // first add dest, then src, then the other nodes on path
            oop_node_data n = gg.getNode(closest_fruit_edge.getDest());
            path.add(n);
            // check that robot isn't already on the fruit edge
            if (source != closest_fruit_edge.getSrc()) {
                n = gg.getNode(closest_fruit_edge.getSrc());
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


        } catch (JSONException exc) {
            System.out.println(exc);
        }
        /*
        String pathStr ="path: ";
        for (int i = 0; i < path.size(); i++) {
            pathStr += path.get(i).getKey() + ", ";
        }
        System.out.println(pathStr);

         */
        return new PathAndTarget(path, closest_fruit_string);
    }


    static public oop_edge_data findFruitEdge(String fruitStr, OOP_DGraph graph) {
        //System.out.println(fruitStr);
        try {
            oop_edge_data e;
            OOP_Point3D p_src, p_dst, p_fruit;
            Iterator<oop_node_data> itr_n;
            Iterator<oop_edge_data> itr_e;
            // get fruit info
            JSONObject f = new JSONObject(fruitStr);
            //System.out.println(f);
            String pos = f.getJSONObject("Fruit").getString("pos");
            int fruitType = f.getJSONObject("Fruit").getInt("type");
            // parse fruit coordinates
            p_fruit = parseCoordinate(pos);
            // go ever all edges and use the triangle equation to determine whether fruit is on any one

            itr_n = graph.getV().iterator();
            while (itr_n.hasNext()) {
                // iterator over edges belonging to node
                oop_node_data n = itr_n.next();
                itr_e = graph.getE(n.getKey()).iterator();
                while (itr_e.hasNext()) {
                    e = itr_e.next();
                    p_src = graph.getNode(e.getSrc()).getLocation();
                    p_dst = graph.getNode(e.getDest()).getLocation();
                    // delta must be positive and very small by the triangle equation
                    double eps = 0.0000001;
                    double delta = Math.abs(p_src.distance3D(p_fruit) + p_dst.distance3D(p_fruit) - p_src.distance3D(p_dst));
                    if (delta < eps) {
                        //check direction
                        if (fruitType < 0 && e.getDest() - e.getSrc() < 0) {
                            return e;
                        }
                        if (fruitType > 0 && e.getDest() - e.getSrc() > 0) {
                            return e;
                        }

                    }

                }
            }
        } catch (JSONException exc) {
            System.out.println(exc);
        }

        return null;
    }

    public static OOP_Point3D parseCoordinate(String c) {
        String[] xAndY = c.split(",");
        OOP_Point3D p = new OOP_Point3D(Double.parseDouble(xAndY[0]), Double.parseDouble(xAndY[1]));
        return p;
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
