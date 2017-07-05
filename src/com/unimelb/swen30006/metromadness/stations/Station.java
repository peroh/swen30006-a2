package com.unimelb.swen30006.metromadness.stations;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.exceptions.FullStationException;
import com.unimelb.swen30006.metromadness.exceptions.InvalidStationActionException;
import com.unimelb.swen30006.metromadness.passengers.Passenger;
import com.unimelb.swen30006.metromadness.tracks.Line;
import com.unimelb.swen30006.metromadness.trains.CargoTrain;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * Station represents a single station in the MetroMadness simulation.
 * It can be a part of 1 to many lines, and can have as many trains inside
 * it as it has platforms.
 *
 * If a Station is active then it is responsible for generating passengers
 * to embark trains that stop at the station.
 *
 * A train can only stop at this station if it is not a CargoTrain.
 */
public class Station {

    /** Number of platforms in Station, determines max number of Trains. */
    public static final int PLATFORMS = 2;
    /** The maximum number of Passengers that can be generated at one time. */
    public static final int MAX_GENERATED = 4;

    /** Radius of station when rendered. */
    public static final float RADIUS = 6;
    /** Number of circle segments when rendered (i.e. smoothness). */
    public static final int NUM_CIRCLE_SEGMENTS = 100;
    /** Maximum number of line colours which will be rendered. */
    public static final int MAX_LINES = 3;

    /** Static instance of Random used to generate random numbers. */
    private static Random random = new Random(30006);

    /** The position of the Station on the map. */
    private Point2D.Float position;
    /** The name of the Station. */
    private String name;
    /** Whether or not Passengers will enter from this Station. */
    private boolean active;
    /** The maximum number of Passengers able to wait at this Station. */
    private int maxPassengers;
    /** The list (queue) of waiting Passengers at this Station. */
    private ArrayList<Passenger> waiting;
    /** A list of all the Lines that this Station belongs too. */
    private ArrayList<Line> lines;
    /** A list of all the Trains that are currently in this Station. */
    private ArrayList<Train> trains;

    /**
     * Initialises a new Station with a given position, name and max
     * passenger capacity. By using this constructor as opposed to
     * Station(Point2D.Float, String) it is automatically set to active
     * (i.e. will generate Passengers).
     *
     * @param position a Point2D.Float describing the Stations position
     * @param name the name of the Station
     * @param maxPassengers the max number of passengers able to wait here
     */
    public Station(Point2D.Float position, String name, int maxPassengers) {
        this.position = position;
        this.name = name;

        this.active = true;
        this.maxPassengers = maxPassengers;
        this.waiting = new ArrayList<Passenger>();

        this.lines = new ArrayList<Line>();
        this.trains = new ArrayList<Train>();
    }

    /**
     * Initialises a new Station with a given position and name.
     * By using this constructor as opposed to
     * Station(Point2D.Float, String, int) it is automatically set to inactive
     * (i.e. will not generate Passengers).
     *
     * @param position a Point2D.Float describing the Stations position
     * @param name the name of the Station
     */
    public Station(Point2D.Float position, String name) {
        this.position = position;
        this.name = name;

        this.active = false;
        this.maxPassengers = 0;
        this.waiting = new ArrayList<Passenger>();

        this.lines = new ArrayList<Line>();
        this.trains = new ArrayList<Train>();
    }

    /**
     * Register that this station is part of a given Line.
     *
     * @param line the Line containing this Station.
     */
    public void registerLine(Line line) {
        this.lines.add(line);
    }

    /**
     * Returns whether or not there is room for the train in
     * the station. Doesn't determine whether it can stop (i.e.
     * if a CargoTrain is entering a regular Station).
     *
     * @param train the Train which may/may not be able to enter
     * @return whether the train can enter the station (different to stop)
     */
    public boolean canEnter(Train train) {
        return (this.trains.size() < PLATFORMS);
    }

    /**
     * Returns whether or not a train can stop at the station, i.e.
     * there is room for it and it is the correct type of train.
     * A CargoTrain will never be able to stop at a regular Station.
     *
     * @param train the Train which may/may not be able to stop
     * @return whether or not the train can stop at the station
     */
    public boolean canStop(Train train) {
        if (train instanceof CargoTrain) {
            return false;
        }

        return this.canEnter(train);
    }

    /**
     * Processes the entry of a train to the station, will not
     * add any passengers, this will be processed in `stop()`.
     *
     * @param train the Train entering this Station
     * @throws FullStationException if Train can't enter this Station
     */
    public void enter(Train train) throws FullStationException {
        if (!this.canEnter(train)) {
            throw new FullStationException();
        }

        this.trains.add(train);
    }

    /**
     * Processes a train stopping at the current station, if the Station is
     * active it will generate some passengers
     * and all passengers which should embark the train will.
     *
     * @param train the Train stopping at the Station
     * @throws InvalidStationActionException if the Train can't stop
     *         at this Station
     * @throws FullStationException if Train can't enter this Station
     */
    public void stop(Train train) throws Exception {
        if (!this.canStop(train)) {
            throw new InvalidStationActionException();
        }

        this.enter(train);

        // If this station is active then generate and embark passengers
        if (this.active) {
            this.generatePassengers();
            train.embarkPassengers();
        }
    }

    /**
     * Processes a train departing the Station. If the train isn't
     * currently in the station then an exception is raised.
     *
     * @param train the Train leaving the station
     * @throws InvalidStationActionException if the Train isn't
     *         currently in the station
     */
    public void leave(Train train) throws InvalidStationActionException {
        if (!this.trains.contains(train)) {
            throw new InvalidStationActionException();
        }

        this.trains.remove(train);
    }

    /**
     * Generates and adds up to MAX_GENERATED passengers to the waiting list.
     * If there are no possible destinations no passengers will be generated.
     */
    private void generatePassengers() {
        // We want to generate up to MAX_GENERATED, but don't
        // want the Station to be over full
        int toGenerate = this.getRandom().nextInt(MAX_GENERATED) + 1;
        int roomLeft = Math.max(0, this.maxPassengers - this.waiting.size());
        toGenerate = Math.min(toGenerate, roomLeft);

        // We need to generate a destination for each passenger
        for (int i = 0; i < toGenerate; i++) {
            Station destination = this.getValidDestination();
            if (destination == null) {
                break;
            }
            
            Passenger passenger = this.generatePassenger(destination);
            this.waiting.add(passenger);
        }
    }

    /**
     * Generates a single passenger, with a given destination. Will
     * be overridden by CargoStation.
     *
     * @param destination the destination of the newly generated passenger
     * @return the generated Passenger
     */
    public Passenger generatePassenger(Station destination) {
        return new Passenger(this, destination);
    }

    /**
     * Generates a random destination station that is valid and on
     * a line that's connected to this station. If there aren't any
     * valid destinations null is returned
     *
     * @return a random destination Station or null if there are no valid ones
     */
    private Station getValidDestination() {
        HashSet<Station> candidates = new HashSet<Station>();

        // Go through all stations on lines connected to this station
        // and keep track of the valid ones
        for (Line line: this.lines) {
            for (Station station: line.getStations()) {
                if (this.isValidDestination(station)) {
                    candidates.add(station);
                }
            }
        }

        // If there are no candidate destinations just return null
        if (candidates.isEmpty()) {
            return null;
        }

        // Return a random stations from the candidate station set
        int randomIndex = this.getRandom().nextInt(candidates.size());
        return (Station) candidates.toArray()[randomIndex];
    }

    /**
     * Returns whether or not a station is a valid destination from
     * this station. Will be overridden by subclasses.
     *
     * For a station to be valid it just has to be different to this station.
     * @param station the Station which may/may not be valid
     * @return whether or not station is a valid destination
     */
    public boolean isValidDestination(Station station) {
        return !this.equals(station);
    }

    /**
     * Returns this Station's name.
     *
     * @return this Station's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns this Station's position.
     *
     * @return this Station's position.
     */
    public Point2D.Float getPosition() {
        return this.position;
    }

    /**
     * Returns the static Random instance. Needed by subclasses to
     * generate random numbers.
     *
     * @return the static Random instance for Station class.
     */
    public Random getRandom() {
        return Station.random;
    }
    
    /**
     * Returns the reference to the mutable ArrayList of
     * passengers currently waiting at this station.
     * 
     * @return array list of all waiting passengers
     */
    public ArrayList<Passenger> getWaitingPassengers() {
        return this.waiting;
    }

    /**
     * Returns a String representation of the Station.
     *
     * @return string representation of Station.
     */
    @Override
    public String toString() {
        return "Station [position=" + position + ", name=" + name
                        + ", trains=" + trains.size() + "]";
    }

    /**
     * Renders the Station at its position on the screen, with
     * concentric circles indicating the color of the first MAX_LINES
     * lines it is a part of.
     *
     * @param renderer the ShapeRenderer used to render to the screen
     */
    public void render(ShapeRenderer renderer){
        float radius = RADIUS;

        // Draw up to MAX_LINES circles with station colours set by each line
        for (int i = 0; i < Math.min(this.lines.size(), MAX_LINES); i++) {
            Line line = this.lines.get(i);
            renderer.setColor(line.getStationColour());
            renderer.circle(this.position.x, this.position.y,
                            radius, NUM_CIRCLE_SEGMENTS);
            // Decrement radius before drawing the next circle
            radius -= 1;
        }

        // Colour the circle depending on how full the Station is
        float fullness = this.trains.size() / (float) PLATFORMS;
        Color colour = Color.WHITE.cpy().lerp(Color.DARK_GRAY, fullness);

        // If there are waiting passengers set colour to red
        if (!this.waiting.isEmpty()) {
            colour = Color.RED;
        }

        renderer.setColor(colour);
        renderer.circle(this.position.x, this.position.y,
                        radius, NUM_CIRCLE_SEGMENTS);
    }

}
