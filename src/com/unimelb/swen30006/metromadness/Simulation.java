package com.unimelb.swen30006.metromadness;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.stations.Station;
import com.unimelb.swen30006.metromadness.tracks.Line;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * The Simulation class acts as an interface between the
 * ApplicationAdapter (MetroMadness) and the domain logic
 * in this application.
 *
 * It is responsible for creating and rendering all lines,
 * stations and trains. It is also responsible for updating
 * each train.
 */
public class Simulation {

    /** A list of all of the Stations in the simulation. */
    private ArrayList<Station> stations;
    /** A list of all the Lines in the simulation. */
    private ArrayList<Line> lines;
    /** A list of all the Trains in the simulation. */
    private ArrayList<Train> trains;

    /**
    * Constructor for the Simulation class.
    *
    * @param fileName name of the .xml file with initialisation info
    */
    public Simulation(String fileName) {
        // Create a map reader and read in the file
        MapReader mapReader = new MapReader(fileName);
        mapReader.process();

        // Create a list of lines
        this.lines = new ArrayList<Line>();
        this.lines.addAll(mapReader.getLines());

        // Create a list of stations
        this.stations = new ArrayList<Station>();
        this.stations.addAll(mapReader.getStations());

        // Create a list of trains
        this.trains = new ArrayList<Train>();
        this.trains.addAll(mapReader.getTrains());
    }

    /**
    * Updates each train with the number of seconds
    * elapsed since the last update.
    */
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        for (Train train: this.trains) {
            try {
                train.update(delta);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
    * Delegates the rendering of each line, train and station
    * to their respective instances.
    *
    * @param renderer the ShapeRenderer used to render to the screen
    */
    public void render(ShapeRenderer renderer) {
        for (Line line: this.lines) {
            line.render(renderer);
        }

        for (Train train: this.trains) {
            train.render(renderer);
        }

        for (Station station: this.stations) {
            station.render(renderer);
        }
    }

}
