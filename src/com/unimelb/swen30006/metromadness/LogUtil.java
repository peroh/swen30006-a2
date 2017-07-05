package com.unimelb.swen30006.metromadness;

import com.unimelb.swen30006.metromadness.passengers.Passenger;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * The LogUtil class abstracts the logging behavior (using the
 * pure fabrication pattern) to increase cohesion in this class and the
 * classes that require logging behavior.
 *
 * It is responsible for logging when a train changes its state,
 * or when a passenger embark/disembark.
 */
public class LogUtil {

    /** The message logged when a Passenger embarks a train. */
    public static final String EMBARK_MESSAGE = "Passenger %d carrying"
        + " %s kg cargo is embarking at %s heading to %s";
    /** The message logged when a Passenger disembarks a train. */
    public static final String DISEMBARK_MESSAGE = "Passenger %d is"
        + " disembarking at %s";

    /** The message logged when a Train enters the FROM_DEPOT state. */
    public static final String FROM_DEPOT_MESSAGE = "%s is travelling from"
        + " the depot to %s";
    /** The message logged when a Train enters the IN_STATION state. */
    public static final String IN_STATION_MESSAGE = "%s is in %s";
    /** The message logged when a Train enters the READY_DEPART state. */
    public static final String READY_DEPART_MESSAGE = "%s is ready to depart"
        + " for %s";
    /** The message logged when a Train enters the ON_ROUTE state. */
    public static final String ON_ROUTE_MESSAGE = "%s enroute to %s";
    /** The message logged when a Train enters the WAITING_ENTRY state. */
    public static final String WAITING_ENTRY_MESSAGE = "%s is awaiting entry"
        + " to %s";
    /** The message logged when a Train enters the PASSING_THROUGH state. */
    public static final String PASSING_THROUGH_MESSAGE = "%s is passing"
        + " through %s";
    
    /**
     * Create a message indicating a Passenger embarking a Train.
     *
     * @param passenger the embarking Passenger
     * @return the message to be logged
     */
    public static String embarkMsg(Passenger passenger) {
        return String.format(EMBARK_MESSAGE,
                             passenger.getId(),
                             passenger.getCargoWeight(),
                             passenger.getStart().getName(),
                             passenger.getDestination().getName());
    }

    /**
     * Creates a message indicating a Passenger disembarking a Train.
     *
     * @param passenger the disembarking Passenger
     * @return the message to be logged
     */
    public static String disembarkMsg(Passenger passenger) {
        return String.format(DISEMBARK_MESSAGE,
                             passenger.getId(),
                             passenger.getDestination().getName());
    }

    /**
     * Creates the appropriate message for the state train is entering
     * (the current state at the time this method is called).
     *
     * @param train the Train whose state has just changed
     * @return the message to be logged
     */
    public static String stateChangeMsg(Train train) {
        String message;

        switch (train.getState()) {
        case FROM_DEPOT:
            message = FROM_DEPOT_MESSAGE;
            break;
        case IN_STATION:
            message = IN_STATION_MESSAGE;
            break;
        case READY_DEPART:
            message = READY_DEPART_MESSAGE;
            break;
        case ON_ROUTE:
            message = ON_ROUTE_MESSAGE;
            break;
        case WAITING_ENTRY:
            message = WAITING_ENTRY_MESSAGE;
            break;
        case PASSING_THROUGH:
            message = PASSING_THROUGH_MESSAGE;
            break;
        default:
            return "";
        }

        return String.format(message, train.getName(),
                             train.getStation().getName());
    }
}
