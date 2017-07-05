package com.unimelb.swen30006.metromadness.tracks;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.exceptions.InvalidLineActionException;
import com.unimelb.swen30006.metromadness.stations.Station;

/**
 * The line class is a representation of the the lines that
 * exist in the simulation.
 *
 * It is responsible for adding stations to each line, calculating
 * the next station/track a train should travel to/on based on the
 * current station, rendering each track, and calculating the direction
 * between two stations on the line.
 */
public class Line {

    /** The colour of the stations on this line. */
    private Color stationColour;
    /** The colour of the tracks on this line. */
    private Color trackColour;
    /** The name of this line. */
    private String name;
    /** A list of the Stations on this line. */
    private ArrayList<Station> stations;
    /** A list of the Tracks on this line. */
    private ArrayList<Track> tracks;

    /**
     * Constructor for Line class.
     *
     * @param station colour of the station
     * @param track colour of the track
     * @param name name of the line
     */
    public Line(Color station, Color track, String name) {
        this.stationColour = station;
        this.trackColour = track;
        this.name = name;

        // Create the data structures
        this.stations = new ArrayList<Station>();
        this.tracks = new ArrayList<Track>();
    }

    /**
     * Adds a new station, registers it to the line and
     * generates the track between the last station on
     * the line and the new station being added.
     *
     * @param station station to be added
     * @param twoWay true if adding a dualTrack
     */
    public void addStation(Station station, Boolean twoWay) {
        // We need to build the track if this is adding to existing stations
        if (!this.stations.isEmpty()) {
            // Get the last station
            Station last = this.stations.get(this.stations.size() - 1);

            // Generate a new track
            Track track;
            if (twoWay) {
                track = new DualTrack(last.getPosition(), station.getPosition(),
                                      this.trackColour);
            } else {
                track = new Track(last.getPosition(), station.getPosition(),
                                  this.trackColour);
            }
            this.tracks.add(track);
        }

        // Add the station
        station.registerLine(this);
        this.stations.add(station);
    }

    /**
     * Finds the next track on the line depending on the line direction.
     *
     * @param station current station to calculate direction from
     * @param forward direction that train would travel
     * @return next track on line
     * @throws Exception if line doesn't contain the station
     * @throws InvalidOutOfBoundsException if the index where the next track
     *         should be doesn't exist
     */
    public Track nextTrack(Station station, boolean forward) throws Exception {
        if (!this.stations.contains(station)) {
            throw new Exception();
        }
        // Determine the track index
        int curIndex = this.stations.lastIndexOf(station);

        // Increment to retrieve
        if (!forward) {
            curIndex -= 1;
        }

        return this.tracks.get(curIndex);
    }

    /**
     * Finds the next station on the line depending on the line direction.
     *
     * @param station current station to calculate direction from
     * @param forward direction that train would travel
     * @return next station on line
     * @throws InvalidLineActionException if line doesn't contain the station
     * @throws InvalidOutOfBoundsException if index of where next station
     *         should be doesn't exist
     */
    public Station nextStation(Station station, boolean forward)
    throws Exception {
        if (!this.stations.contains(station)) {
            throw new InvalidLineActionException();
        }

        int curIndex = this.stations.lastIndexOf(station);

        if (forward) {
            curIndex += 1;
        } else {
            curIndex -= 1;
        }

        return this.stations.get(curIndex);
    }

    /**
     * Gets the name of the line.
     *
     * @return name of line
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the colour of the stations on the line.
     *
     * @return station colour
     */
    public Color getStationColour() {
        return this.stationColour;
    }

    /**
     * Gets the colour of the tracks on the line.
     *
     * @return track colour
     */
    public Color getTrackColour() {
        return this.trackColour;
    }

    /**
     * Gets all the stations on the line.
     *
     * @return list of stations on line
     */
    public ArrayList<Station> getStations() {
        return this.stations;
    }
    
    /**
     * Returns whether or not a given station is on the Line.
     * 
     * @param station the Station in question
     * @return whether or not this line contains station
     */
    public boolean hasStation(Station station) {
        return this.stations.contains(station);
    }
    
    /**
     * Returns whether or not a given station is the start of this
     * line.
     * 
     * @return if the given station is the start of the line
     */
    public boolean startOfLine(Station station) {
        return (this.stations.indexOf(station) == 0);
    }
    
    /**
     * Returns whether or not a given station is the end of this
     * line.
     * 
     * @return if the given station is the end of the line
     */
    public boolean endOfLine(Station station) {
        int stationIndex = this.stations.indexOf(station);
        int endIndex = this.stations.size() - 1;
        return (stationIndex == endIndex);
    }

    /**
     * Renders each track on the line.
     *
     * @param renderer the ShapeRenderer used to render the screen
     */
    public void render(ShapeRenderer renderer) {
        // Delegate render responsibility to each track section
        for (Track track: this.tracks) {
            track.render(renderer);
        }
    }

    /**
     * Calculates the direction of the line based on starting station and
     * an ending station. If a train were to travel from a station with an
     * index lower than the index of the station traveled to, the train
     * would be going "forwards", and this method will return true.
     *
     * @param start station to calculate direction from
     * @param end station to calculate direction to
     * @return true if train is traveling forwards
     * @throws InvalidLineActionException if start and end station are
     *         the same
     */
    public boolean directionBetween(Station start, Station end)
    throws InvalidLineActionException {
        if (start.equals(end)){
            throw new InvalidLineActionException();
        }

        return (this.stations.indexOf(end) > this.stations.indexOf(start));
    }
}
