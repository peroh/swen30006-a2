package com.unimelb.swen30006.metromadness.passengers;

import com.unimelb.swen30006.metromadness.stations.Station;
import com.unimelb.swen30006.metromadness.tracks.Line;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * Passenger contains information about a Passenger traveling in the
 * MetroMadness simulation on a Train or CargoTrain.
 *
 * It knows about its starting and destination points, and is responsible
 * for knowing when to embark and disembark from a train.
 *
 * A passenger not carrying cargo simply has a cargoWeight of 0.
 */
public class Passenger {

    /** The maximum cargo weight a single passenger can hold. */
    public static final int MAX_CARGO = 50;

    /** Static variable used for generating unique sequential ids. */
    private static int nextId = 1;

    /** The Passengers unique identifier. */
    private int id;
    /** The station at which the Passenger is embarking a train. */
    private Station start;
    /** The station that the Passenger is trying to reach. */
    private Station destination;

    /**
     * The weight of the cargo the passenger is carrying. A weight of 0
     * indicated that the passenger is not carrying cargo.
     */
    private int cargoWeight;

    /**
     * Constructor for a Passenger who is carrying cargo.
     *
     * @param cargoWeight weight of cargo carried
     * @param start station the Passenger starts at
     * @param end the Passenger's destination station
     */
    public Passenger(int cargoWeight, Station start, Station end) {
        this.id = nextId++;
        this.start = start;
        this.destination = end;
        this.cargoWeight = cargoWeight;
    }

    /**
     * Constructor for a Passenger not carrying cargo.
     *
     * @param start station the Passenger starts at
     * @param end the Passenger's destination station
     */
    public Passenger(Station start, Station end) {
        this(0, start, end);
    }

    /**
     * Whether or not the Passenger is carrying cargo.
     *
     * @return whether the Passenger is carrying cargo
     */
    public boolean hasCargo() {
        return (cargoWeight > 0);
    }

    /**
     * Returns the id of the current Passenger.
     *
     * @return Passenger's id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the (possibly 0) amount of cargo a Passenger is carrying.
     *
     * @return the weight of the cargo carried
     */
    public int getCargoWeight() {
        return this.cargoWeight;
    }

    /**
     * Returns the Station where the passenger started.
     *
     * @return the Station passenger started at
     */
    public Station getStart() {
        return this.start;
    }


    /**
     * Returns the Passenger's destination Station.
     *
     * @return the passenger's destination Station
     */
    public Station getDestination() {
        return this.destination;
    }

    /**
     * Returns whether or not the Passenger should embark a given Train.
     *
     * A Passenger will try and embark if it is on the right line and
     * going in the correct direction.
     *
     * @param train the Train that the Passenger may/may not try to embark
     * @return whether or not the Passenger should try to embark the train
     * @throws Exception if the direction between the start and destination
     *                   stations can't be calculated
     */
    public boolean shouldEmbark(Train train) throws Exception {
        Line line = train.getLine();

        // Only embark trains on the correct train line
        if (!line.hasStation(this.destination)) {
            return false;
        }

        // Only embark trains going in the right direction
        boolean shouldGoForward = line.directionBetween(start, destination);
        if (train.isForward() != shouldGoForward) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether or not the Passenger should disembark the Train it is
     * on.
     *
     * A Passenger should disembark if the current station is its destination.
     *
     * @param train the Train the Passenger is currently on
     * @return whether or not the Passenger will disembark the train
     */
    public boolean shouldDisembark(Train train) {
        return train.getStation().equals(this.destination);
    }

}
