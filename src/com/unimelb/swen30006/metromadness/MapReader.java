package com.unimelb.swen30006.metromadness;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

// Imports for parsing XML files
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;


// Classes to be generated by MapReader
import com.unimelb.swen30006.metromadness.stations.Station;
import com.unimelb.swen30006.metromadness.stations.CargoStation;
import com.unimelb.swen30006.metromadness.tracks.Line;
import com.unimelb.swen30006.metromadness.trains.CargoTrain;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * MapReader is a utility class used to read a configuration .xml
 * file containing information about stations, lines and trains.
 *
 * It follows the Factory pattern (as opposed to the Creator pattern)
 * to promote high cohesion and low coupling, and to provide indirection
 * between the domain logic and the configuration file format.
 */
public class MapReader {

    /** The multiplier to be applied to a colour's rgb values. */
    private static final float COLOUR_COEFFICIENT = 1f / 255f;
    /** The multiplier for a positions coordinates to scale the screen. */
    private static final float POSITION_SCALE = 8;

    /** The max number of passengers on a Big train. */
    private static final int BIG_PASSENGERS = 80;
    /** The max number of passengers on a Small train. */
    private static final int SMALL_PASSENGERS = 10;
    /** The max cargo weight on a Big cargo train. */
    private static final int BIG_CARGO = 1000;
    /** The max cargo weight on a Small cargo train. */
    private static final int SMALL_CARGO = 200;


    /** A list of all the trains read in. */
    public ArrayList<Train> trains;
    /** A hash map containing all processes stations. */
    public HashMap<String, Station> stations;
    /** A hash map containing all processed lines. */
    public HashMap<String, Line> lines;

    /** Whether or not the file has been processed yet. */
    public boolean processed;
    /** The filename of the .xml file to be read. */
    public String filename;

    /**
     * Constructor for the MapReader class, will not actually
     * process the configuration file until any of the data is
     * requested.
     *
     * @param filename the path to the configuration .xml file
     */
    public MapReader(String filename) {
        this.filename = filename;
        this.processed = false;

        // Initialise empty data structures
        this.trains = new ArrayList<Train>();
        this.stations = new HashMap<String, Station>();
        this.lines = new HashMap<String, Line>();
    }

    /**
     * Processes the .xml file and creates all relevant domain objects if
     * the file hasn't already been processed.
     */
    public void process() {
        // We don't need to process if it's already been processed
        if (this.processed) {
            return;
        }

        try {
            // Build the doc factory
            FileHandle file = Gdx.files.internal(this.filename);
            XmlReader reader = new XmlReader();
            Element root = reader.parse(file);

            // Extract the relevant xml Elements for processing
            Element stations = root.getChildByName("stations");
            Element lines = root.getChildByName("lines");
            Element trains = root.getChildByName("trains");

            // Process each element
            processStations(stations);
            processLines(lines);
            processTrains(trains);

            this.processed = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Returns a collection of all created Trains.
     *
     * @return all trains in the simulation
     */
    public Collection<Train> getTrains() {
        this.process();
        return this.trains;
    }

    /**
     * Returns a collection of all created Lines.
     *
     * @return all lines in the simulation
     */
    public Collection<Line> getLines() {
        this.process();
        return this.lines.values();
    }

    /**
     * Returns a collection of all created Stations.
     *
     * @return all stations in the simulation
     */
    public Collection<Station> getStations() {
        this.process();
        return this.stations.values();
    }

    /**
     * Processes and creates all stations.
     *
     * @param stations the XML Element containing info about all stations
     */
    private void processStations(Element stations) {
        Array<Element> stationList = stations.getChildrenByName("station");
        for (Element element: stationList) {
            Station station = processStation(element);
            this.stations.put(station.getName(), station);
        }
    }

    /**
     * Processes and creates all lines.
     *
     * @param lines the XML Element containing info about all lines
     */
    private void processLines(Element lines) {
        Array<Element> lineList = lines.getChildrenByName("line");
        for (Element element: lineList) {
            Line line = processLine(element);
            this.lines.put(line.getName(), line);
        }
    }

    /**
     * Processes and creates all trains.
     *
     * @param trains the XML Element containing info about all trains
     */
    private void processTrains(Element trains) {
        Array<Element> trainList = trains.getChildrenByName("train");
        for (Element element: trainList) {
            Train train = processTrain(element);
            this.trains.add(train);
        }
    }

    /**
     * Processes a single station, creating and returning either
     * a Station or CargoStation.
     *
     * @param station the XML Element containing info about a single station
     * @return a Station with the appropriate attributes set
     */
    private Station processStation(Element station) {
        // Get station information
        String type = station.get("type");
        String name = station.get("name");

        // Retrieve the stations position on the screen (with scaling)
        int xPos = (int) (station.getInt("x_loc") / POSITION_SCALE);
        int yPos = (int) (station.getInt("y_loc") / POSITION_SCALE);
        Point2D.Float position = new Point2D.Float(xPos, yPos);

        // Retrieve the maximum passenger capacity of the station
        int maxPassengers = -1;
        if (type.equals("Active") || type.equals("Cargo")) {
            maxPassengers = station.getInt("max_passengers");
        }

        if (type.equals("Active")) {
            return new Station(position, name, maxPassengers);
        } else if (type.equals("Cargo")) {
            return new CargoStation(position, name, maxPassengers);
        } else {
            return new Station(position, name);
        }
    }

    /**
     * Processes a single line, creating and returning a Line object with
     * all relevant stations added.
     *
     * @param lineElement the XML Element containing info about a single line
     * @return a Line with the appropriate attributes set and stations added
     */
    private Line processLine(Element lineElement) {
        // Initialise new line with name and appropriate colours
        Color stationColour =
            extractColour(lineElement.getChildByName("station_colour"));
        Color trackColour =
            extractColour(lineElement.getChildByName("line_colour"));
        String name = lineElement.get("name");
        Line line = new Line(stationColour, trackColour, name);

        // Iterate through stations and register them with the line
        Array<Element> stationElements =
            lineElement.getChildrenByNameRecursively("station");
        for (Element stationElement: stationElements) {
            Station station = this.stations.get(stationElement.get("name"));
            boolean twoWay = stationElement.getBoolean("double");
            line.addStation(station, twoWay);
        }

        return line;
    }


    /**
     * Processes a single train, creating and returning a Train or CargoTrain
     * instance, with the appropriate passenger/cargo limits set.
     *
     * @param train the XML Element containing info about a single train
     * @return a Train with the appropriate attributes set
     */
    private Train processTrain(Element train) {
        // Retrieve the values
        String type = train.get("type");
        String lineName = train.get("line");
        String start = train.get("start");
        String name = train.get("name");
        boolean direction = train.getBoolean("direction");

        // Retrieve the lines and stations
        Line line = this.lines.get(lineName);
        Station station = this.stations.get(start);

        // Derive the capacity (for cargo and passenger trains)
        int passengerCapacity = -1;
        int cargoCapacity = -1;
        if (type.startsWith("Big")) {
            passengerCapacity = BIG_PASSENGERS;
            cargoCapacity = BIG_CARGO;
        } else if (type.startsWith("Small")) {
            passengerCapacity = SMALL_PASSENGERS;
            cargoCapacity = SMALL_CARGO;
        }

        // Create the Train/CargoTrain appropriately
        if (type.endsWith("Cargo")) {
            return new CargoTrain(name, line, station, direction,
                                  passengerCapacity, cargoCapacity);
        } else {
            return new Train(name, line, station, direction,
                             passengerCapacity);
        }
    }

    /**
     * Extract a colour from XML (set with values out of 255) to a Java Color
     * object with the appropriate rgb values set.
     *
     * @param colour the XML Element representing a colour
     * @return the appropriate Color object
     */
    private Color extractColour(Element colour){
        float red = colour.getFloat("red") * COLOUR_COEFFICIENT;
        float green = colour.getFloat("green") * COLOUR_COEFFICIENT;
        float blue = colour.getFloat("blue") * COLOUR_COEFFICIENT;
        return new Color(red, green, blue, 1f);
    }

}
