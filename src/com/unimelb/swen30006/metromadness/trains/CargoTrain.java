package com.unimelb.swen30006.metromadness.trains;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.unimelb.swen30006.metromadness.LogUtil;
import com.unimelb.swen30006.metromadness.exceptions.FullTrainException;
import com.unimelb.swen30006.metromadness.passengers.Passenger;
import com.unimelb.swen30006.metromadness.stations.Station;
import com.unimelb.swen30006.metromadness.tracks.Line;

/**
 * A CargoTrain is like a regular Train, although it can
 * carry Passengers carrying cargo and will only stop at a
 * CargoStation.
 */
public class CargoTrain extends Train {

    /** The logger used to log messages to the console and other outputs. */
    private static Logger logger = LogManager.getLogger();
    
    /** The maximum cargo capacity (in kgs) of this CargoTrain. */
    private int maxCargo;
    /** The current cargo weight on the train. */
    private int currentCargo;

    /**
     * Initialises a new CargoTrain with a given name, line,
     * starting station, direction and capacities for both passengers
     * and cargo.
     * 
     * @param name the Trains name
     * @param line the Line the train travels on
     * @param start the starting Station for this train
     * @param forward whether or not the Train is travelling forward
     * @param maxPassengers the passenger capacity of this train
     * @param maxCargo the cargo capacity of this train in kilograms
     */
    public CargoTrain(String name, Line line, Station start,
                      boolean forward, int maxPassengers, int maxCargo) {
        super(name, line, start, forward, maxPassengers);
        this.maxCargo = maxCargo;
    }

    /**
     * Embarks a Passenger onto this train, which involves accounting for
     * the passenger's cargo, logging the action and adding them to the
     * list of Passengers.
     * 
     * @param passenger the Passenger to be embarked
     * @throws FullTrainException if the Passenger is
     *         unable to embark
     */
    public void embark(Passenger passenger)
    throws FullTrainException {
        if (!this.canEmbark(passenger)) {
            throw new FullTrainException();
        }

        logger.info(LogUtil.embarkMsg(passenger));
        this.getPassengers().add(passenger);
        this.currentCargo += passenger.getCargoWeight();
    }

    /**
     * Disembarks a passenger (doesn't have the responsibility of
     * removing a Passenger from the passengers list).
     * 
     * Responsible for logging this action.
     * 
     * @param passenger the Passenger to be embarked
     * @throws Exception if the Passenger is unable to embark
     */
    public void disembark(Passenger passenger) {
        logger.info(LogUtil.disembarkMsg(passenger));
        this.currentCargo -= passenger.getCargoWeight(); 
    }


    /**
     * Returns whether or not a given passenger is able to embark this train.
     * They will be able to do so when there is room in terms of passengers
     * and cargo.
     * 
     * @param passenger the passenger wishing to embark
     * @return whether or not the passenger can embark this train
     */
    public boolean canEmbark(Passenger passenger) {
        boolean roomForPassenger =
            (this.getPassengers().size() < this.getMaxPassengers());
        boolean roomForCargo =
            (this.currentCargo + passenger.getCargoWeight() <= this.maxCargo);
        return roomForPassenger && roomForCargo;
    }

}
