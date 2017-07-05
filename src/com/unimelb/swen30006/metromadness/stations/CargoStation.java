package com.unimelb.swen30006.metromadness.stations;

import java.awt.geom.Point2D;

import com.unimelb.swen30006.metromadness.passengers.Passenger;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * A CargoStation is a subclass of Station, however all trains can stop
 * here and Passengers generated here will have some amount of cargo
 * and only have destinations which are also CargoStations.
 */
public class CargoStation extends Station {

    /**
     * Initialises a new CargoStation at a given position, with a given
     * name and maximum number of passengers.
     *
     * @param position a Point2D.Float position of the Station on the map
     * @param name the name of the Station
     * @param maxPassengers the maximum number of passengers that can wait
     */
    public CargoStation(Point2D.Float position, String name,
                        int maxPassengers) {
        super(position, name, maxPassengers);
    }

    /**
     * Returns whether or not a train can stop at the station, i.e.
     * there is room for it and it is the correct type of train.
     * Since this is a CargoStation, all trains may stop here.
     *
     * @param train the Train which may/may not be able to stop
     * @return whether or not the train can stop at the station
     */
    @Override
    public boolean canStop(Train train) {
        return this.canEnter(train);
    }

    /**
     * Generates a single passenger, with a given destination. Since this
     * is a CargoStation all generated passengers will have some random
     * amount of cargo (with weight [1, MAX_CARGO]).
     *
     * @param destination the destination of the newly generated passenger
     * @return the generated Passenger
     */
    @Override
    public Passenger generatePassenger(Station destination) {
        int cargoWeight = this.getRandom().nextInt(Passenger.MAX_CARGO) + 1;
        return new Passenger(cargoWeight, this, destination);
    }

    /**
     * Returns whether or not a station is a valid destination from
     * this station. For a destination to be valid it has to be different
     * to this station and also a CargoStation.
     *
     * @param station the Station which may/may not be valid
     * @return whether or not station is a valid destination
     */
    @Override
    public boolean isValidDestination(Station destination) {
        boolean isCargo = destination instanceof CargoStation;
        return (isCargo && !this.equals(destination));
    }

}
