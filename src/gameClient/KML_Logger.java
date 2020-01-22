package gameClient;

import de.micromata.opengis.kml.v_2_2_0.*;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_node_data;
import oop_utils.OOP_Point3D;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.stream.Stream;

public class KML_Logger {
    /**
     * Create a new KML object and log the graph.
     * Also create 2 folders, one for graph, another for robots and fruit.
     * @param level name of the level we are logging
     * @param gg graph of the level we are logging
     */
    public KML_Logger(String level, OOP_DGraph gg){
        kml = new Kml();
        log = kml.createAndSetDocument().withName(level).withOpen(true);
        //create folder for graph
        Folder graph = log.createAndAddFolder();
        graph.withName("graph").withOpen(true);
        addGraph(gg, graph);
        // create folders for dynamic objects
        bananas = log.createAndAddFolder();
        bananas.withName("Bananas").withOpen(true);
        robots = log.createAndAddFolder();
        robots.withName("Robots").withOpen(true);
        apples = log.createAndAddFolder();
        apples.withName("Apples").withOpen(true);
        // add icons
        addIcon(graph, "https://raw.githubusercontent.com/jonny9066/OOP_Ex3/master/blue%20dot.png",
                0.5, "graph");
        addIcon(bananas, "https://raw.githubusercontent.com/jonny9066/OOP_Ex3/master/banana.png",
                0.5, "bananas");
        addIcon(robots, "https://raw.githubusercontent.com/jonny9066/OOP_Ex3/master/red%20dot.png",
                0.5, "robots");
        addIcon(apples, "https://raw.githubusercontent.com/jonny9066/OOP_Ex3/master/apple.png",
                0.5, "apples");


    }

    private static void addIcon(Folder folder, String href, double scale, String name){
        Icon icon = new Icon()
                .withHref(href);
        Style style = folder.createAndAddStyle();
        style.withId("style_" + name) // set the stylename to use this style from the placemark
                .createAndSetIconStyle().withScale(scale).withIcon(icon); // set size and icon
    }

    /**
     *
     * @param longitude
     * @param latitude
     * @param name name of object we are logging
     */
    public void addMovingPlacemark(double longitude, double latitude, String name){
        Placemark placemark;
        //for robot
        if(name.charAt(0) == 'R'){
            placemark = robots.createAndAddPlacemark();
            placemark.withName(name).withStyleUrl("#style_" + "robots");
        }//for apple
        else if(name.charAt(0) == 'A'){
            placemark = apples.createAndAddPlacemark();
            placemark.withName(name).withName(name).withStyleUrl("#style_" + "apples");
        }//must be banana
        else{
            placemark = bananas.createAndAddPlacemark();
            placemark.withName(name).withName(name).withStyleUrl("#style_" + "bananas");
        }
        // use the style for each continent
        placemark.createAndSetPoint().addToCoordinates(longitude, latitude); // set coordinates
        placemark.createAndSetTimeStamp().setWhen(java.time.LocalDate.now().toString()+"T"
                +java.time.LocalTime.now().toString()+"Z");
    }

    private static void createPlacemark(Folder folder, double longitude, double latitude, String name) {
        Placemark placemark = folder.createAndAddPlacemark();
        placemark.withName(name).withStyleUrl("#style_" + "graph");
        // use the style for each continent
        placemark.withName(name)
              // coordinates and distance (zoom level) of the viewer
             .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(0).withRange(12000000);

        placemark.createAndSetPoint().addToCoordinates(longitude, latitude); // set coordinates
    }
    private static void createLine(Folder folder, double longitude1, double latitude1, double longitude2,
                                   double latitude2) {
        Placemark placemark = folder.createAndAddPlacemark();
        LineString line = placemark.createAndSetLineString();
        line.addToCoordinates( longitude1, latitude1);
        line.addToCoordinates( longitude2, latitude2);
    }

    private  static void addGraph(OOP_DGraph gg, Folder folder){
        Iterator<oop_node_data> itrV;
        // add nodes to kml
        itrV = gg.getV().iterator();
        while (itrV.hasNext()) {
            oop_node_data n = itrV.next();
            OOP_Point3D p = n.getLocation();
            createPlacemark(folder, p.x(), p.y(), String.valueOf(n.getKey()));
        }
        itrV = gg.getV().iterator();
        while (itrV.hasNext()) {
            oop_node_data n = itrV.next();
            Iterator<oop_edge_data> itrE = gg.getE(n.getKey()).iterator();
            while (itrE.hasNext()) {
                oop_edge_data e = itrE.next();
                OOP_Point3D p1 = gg.getNode(e.getSrc()).getLocation();
                OOP_Point3D p2 = gg.getNode(e.getDest()).getLocation();
                createLine(folder, p1.x(), p1.y(), p2.x(), p2.y());
            }
        }
    }

    public String saveKML() throws FileNotFoundException{
        String filename = log.getName()+".kml";
        kml.marshal(new File(filename));
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filename), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private Kml kml;
    private Document log;
    private Folder bananas, apples, robots;
}
