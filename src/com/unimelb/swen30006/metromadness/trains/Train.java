package com.unimelb.swen30006.metromadness.trains;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.LogUtil;
import com.unimelb.swen30006.metromadness.exceptions.FullTrainException;
import com.unimelb.swen30006.metromadness.passengers.Passenger;
import com.unimelb.swen30006.metromadness.stations.Station;
import com.unimelb.swen30006.metromadness.tracks.Line;
import com.unimelb.swen30006.metromadness.tracks.Track;


/**
 * A Train represents a single train in the MetroMadness simulation, which
 * deals with commuting between stations, and with embarking/disembarking
 * passengers at a given station.
 */
public class Train {

    /**
     * A State represents one of the many states a Train can be in at
     * any one time.
     */
    public enum State {
        /**
         * Indicates that a Train has stopped at a station and will
         * embark/disembark passengers.
         */
        IN_STATION,
        
        /**
         * Indicates that a Train is ready to depart a station.
         */
        READY_DEPART,
        
        /**
         * Indicates that the Train is on a track on route to
         * its next stop.
         */
        ON_ROUTE,
        
        /**
         * Indicates that a Train has arrived a station and is
         * waiting to either stop or pass through.
         */
        WAITING_ENTRY,
        
        /**
         * Indicates a Train is on its way from the depot to its
         * first station.
         */
        FROM_DEPOT,
        
        /**
         * Indicates that a Train is at a Station but is just
         * passing through (as opposed to stopping).
         * 
         * This is relevant for CargoTrains going through non-cargo
         * Stations.
         */
        PASSING_THROUGH
    }

    /** The colour that a train traveling forward will be rendered. */
    private static final Color FORWARD_COLOUR = Color.ORANGE;
    /** The colour that a train traveling backwards will be rendered. */
    private static final Color BACKWARD_COLOUR = Color.VIOLET;
    /** The coefficient for scaling a trains size when it has passengers. */ 
    private static final float SCALING_MULTIPLIER = 1 / 7f;
    /** The constant term used for scaling a trains size. */ 
    private static final float SCALING_CONSTANT = 60 / 7f;
    /** The trains base width when rendered. */
    private static final float TRAIN_WIDTH = 4;
    /** The speed at which the train moves along a track. */
    private static final float TRAIN_SPEED = 50f;
    /** Number of seconds a train will wait before departing a station. */
    private static final float WAIT_TIME = 2;
    /** The distance a train is from a Station before it enters/stops. */
    private static final float DISTANCE_THRESHOLD = 10;

    /** The logger used to log messages to the console and other outputs. */
    private static Logger logger = LogManager.getLogger();

    /** The name of this train. */
    private String name;
    /** Whether or not train has disembarked passengers since stopping. */
    private boolean disembarked;
    /** The maximum passenger capacity of a train. */
    private int maxPassengers;
    /** The trains current state. */
    private State state;
    /** Whether or not the train is currently traveling forward. */
    private boolean forward;
    /** The trains position within the simulation. */
    private Point2D.Float position;

    /** The line on which this Train travels. */
    private Line line;
    /**
     * The station that this Train is currently in or
     * on route towards.
     */
    private Station station;
    /** The track this station will enter or is currently on. */
    private Track track;
    /** A list of all the passengers on this train. */
    private ArrayList<Passenger> passengers;

    /**
     * How long this train has before it should depart the current station.
     */
    private float departureTimer;


    /**
     * Initialises a Train with a given name, line, starting station,
     * direction and maximum passenger capacity.
     * 
     * @param name the Trains name
     * @param line the Line the train travels on
     * @param start the starting Station for this train
     * @param forward whether or not the Train is traveling forward
     * @param maxPassengers the passenger capacity of this train
     */
    public Train(String name, Line line, Station start,
                 boolean forward, int maxPassengers) {
        this.name = name;
        this.line = line;
        this.station = start;
        this.state = State.FROM_DEPOT;
        this.forward = forward;
        
        this.passengers = new ArrayList<Passenger>();
        this.maxPassengers = maxPassengers;
    }

    /**
     * The main logic loop for a Train, dealing with Trains departing and
     * entering stations as well as moving along lines.
     * 
     * @param delta time between last update in seconds
     * @throws Exception if interfacing with Stations, Trains or Tracks
     *         fails for some reason
     */
    public void update(float delta) throws Exception {
        State originalState = this.state;
        
        switch (this.state) {
        case FROM_DEPOT:
            this.processArrival();
            break;
            
        case IN_STATION:
            this.position = (Point2D.Float) station.getPosition().clone();

            if (!this.disembarked) {
                this.calculateDirection();
                this.disembarkPassengers();
                this.embarkPassengers();
                this.departureTimer = WAIT_TIME;
            }
            
            if (this.departureTimer <= 0) {
                this.processDeparture();
            }
            
            this.departureTimer -= delta;
            break;

        case READY_DEPART:
            if (this.track.canEnter(this)) {
                this.track.enter(this);
                this.state = State.ON_ROUTE;
            }
            break;

        case ON_ROUTE:
            if (this.nearStation()) {
                this.state = State.WAITING_ENTRY;
            } else {
                this.move(delta);
            }
            break;

        case WAITING_ENTRY:
            // If the train did enter or stop at the station
            if (processArrival()) {
                this.track.leave(this);
            }
            break;
            
        case PASSING_THROUGH:
            this.position = (Point2D.Float) station.getPosition().clone();
            this.calculateDirection();
            this.processDeparture();
            break;
            
        }
        
        if (originalState != this.state) {
            logger.info(LogUtil.stateChangeMsg(this));
        }
    }
    
    /**
     * Processes this trains arrival at a station, called
     * by update when the Train is in either the
     * FROM_DEPOT or WAITING_ENTRY states.
     * 
     * If a train cannot stop it will try to enter (i.e.
     * just pass through).
     * 
     * @return whether or not did stop OR enter
     * @throws Exception if stopping or entering a station fails
     */
    private boolean processArrival() throws Exception {
        if (this.station.canStop(this)) {
            this.station.stop(this);
            this.state = State.IN_STATION;
            this.disembarked = false;
        } else if (station.canEnter(this)) {
            this.station.enter(this);
            this.state = State.PASSING_THROUGH;
        } else {
            return false;
        }
        
        return true;
    }
    
    /**
     * Processes this trains departure from the current
     * station. This is called by update when the train is in the
     * IN_STATION or PASSING_THROUGH state.
     * 
     * Deals with changing directions and choosing the next
     * station/track.
     * 
     * @throws Exception if can't retrieve next track or station
     */
    private void processDeparture() throws Exception {
        // Leave the current station
        this.station.leave(this);
        
        // Get which track we have to tack on route to which station
        this.track = this.line.nextTrack(this.station, this.forward);
        this.station = this.line.nextStation(this.station, this.forward);
        this.state = State.READY_DEPART;
    }
 
    /**
     * Checks whether we're at the start or the end of the line
     * and must turn around.
     */
    private void calculateDirection() {
        if (this.line.endOfLine(this.station)) {
            this.forward = false;
        } else if (this.line.startOfLine(this.station)) {
            this.forward = true;
        }
    }

    /**
     * Iterates through all waiting passengers and embarks those who
     * should embark on the given train.
     *
     * @throws Exception if embarkment fails
     */
    public void embarkPassengers() throws Exception {
        // Use an iterator so that we can remove passengers while iterating
        Iterator<Passenger> waitingIterator =
                station.getWaitingPassengers().iterator();
        
        while (waitingIterator.hasNext()) {
            Passenger passenger = waitingIterator.next();

            // If the Passenger should and can embark then tell the train
            // it is embarking
            if (passenger.shouldEmbark(this) && this.canEmbark(passenger)) {
                this.embark(passenger);
                waitingIterator.remove();
            }
        }
    }
    
    /**
     * Iterates through all passengers currently on the train, disembarking
     * and removing from the train those that choose to disembark.
     * 
     * It will only disembark passengers if they have not yet been disembarked.
     */
    public void disembarkPassengers()  {
        if (this.disembarked) {
            return;
        }
        
        // Use an iterator so we can remove passengers while we loop through
        Iterator<Passenger> passengersIterator = this.passengers.iterator();
        while (passengersIterator.hasNext()) {
            Passenger passenger = passengersIterator.next();
            
            // If a passenger should disembark at this station then
            // process this actual and remove it from the list of passengers
            if (passenger.shouldDisembark(this)) {
                this.disembark(passenger);
                passengersIterator.remove();
            }
        }
        
        this.disembarked = true;
    }

    /**
     * Embarks a singular passenger onto the train. This will be overridden
     * by the CargoTrain subclass.
     * 
     * @param passenger the Passenger wishing to embark
     * @throws FullTrainException if there is no room for
     *         the passenger to embark
     */
    public void embark(Passenger passenger)
    throws FullTrainException {
        if (!this.canEmbark(passenger)) {
            throw new FullTrainException();
        }
        
        logger.info(LogUtil.embarkMsg(passenger));
        this.passengers.add(passenger);
    }


    /**
     * Disembarks a given passenger from the train. This is not responsible
     * for removing the passenger from the passengers ArrayList.
     * 
     * Will be overridden in the CargoTrain subclass.
     * 
     * @param passenger the Passenger wishing to disembark.
     */
    public void disembark(Passenger passenger) {
        logger.info(LogUtil.disembarkMsg(passenger));
    }

    /**
     * Returns whether or not a given passenger is able to embark this train.
     * 
     * Will be overridden in the CargoTrain subclass.
     * 
     * @param passenger the passenger wishing to embark
     * @return whether or not the passenger can embark this train
     */
    public boolean canEmbark(Passenger passenger) {
        return (passengers.size() < maxPassengers);
    }

    /**
     * Returns whether or not this train is close enough to its target
     * station to enter the station.
     * 
     * @return whether this train is close enough to enter the station
     */
    private boolean nearStation() {
        Point2D.Float stationPosition = station.getPosition();
        double distance = this.position.distance(stationPosition);
        
        return (distance < DISTANCE_THRESHOLD);
    }


    /**
     * Moves the train TRAIN_SPEED * delta towards its destination station.
     * 
     * @param delta time in seconds between last update
     */
    private void move(float delta) {
        // Work out where we're going
        float angle = angleAlongLine(this.position,
                                     this.station.getPosition());
        float newX = this.position.x
                     + (float) (Math.cos(angle) * delta * TRAIN_SPEED);
        float newY = this.position.y
                     + (float) (Math.sin(angle) * delta * TRAIN_SPEED);
        
        this.position.setLocation(newX, newY);
    }

    /**
     * Returns a String representation of the Train.
     *
     * @return string representation of Train.
     */
    @Override
    public String toString() {
        return "Train [line=" + this.line.getName()
                + ", departureTimer=" + departureTimer
                + ", pos=" + position + ", forward=" + forward
                + ", state=" + state
                + ", disembarked=" + disembarked + "]";
    }

    /**
     * Returns whether or not the train is in a state indicating
     * that it is currently in a station.
     * 
     * @return whether the Train is in a station
     */
    private boolean inStation() {
        return (this.state == State.IN_STATION
             || this.state == State.READY_DEPART
             || this.state == State.PASSING_THROUGH);
    }

    /**
     * Returns the angle at which the train must travel towards
     * the destination station, given the current and destination
     * positions as Point2D.Float objects.
     * 
     * @param current the current train position
     * @param destination the position which the train wishes to move towards
     * @return the angle the train must move at
     */
    private float angleAlongLine(Point2D.Float current,
                                 Point2D.Float destination) {
        double deltaX = destination.getX() - current.getX();
        double deltaY = destination.getY() - current.getY();
        return (float) Math.atan2(deltaY, deltaX);
    }
    
    /**
     * Returns the name of this Train.
     * 
     * @return train's name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns maximum passenger capacity of this train.
     * 
     * @return train's passenger capacity
     */
    public int getMaxPassengers() {
        return this.maxPassengers;
    }
    
    /**
     * Returns the train's current state.
     * 
     * @return train's current state
     */
    public State getState() {
        return this.state;
    }
    
    /**
     * Returns whether or not the train is currently moving forward.
     * 
     * @return whether or not the train is going forward
     */
    public boolean isForward() {
        return this.forward;
    }
    
    /**
     * Returns the current position of the train.
     * 
     * @return the current position of the train
     */
    public Point2D.Float getPosition() {
        return this.position;
    }
    
    /**
     * Returns the line the train is moving along.
     * 
     * @return the train's line
     */
    public Line getLine() {
        return this.line;
    }
    
    /**
     * Returns the station the train is heading towards
     * or is currently in.
     * 
     * @return the train's current/destination station
     */
    public Station getStation() {
        return this.station;
    }
    
    /**
     * Get the track that the train is currently or about
     * to move along.
     * 
     * @return the track the train is/will be traveling on
     */
    public Track getTrack() {
        return this.track;
    }
    
    /**
     * Returns a mutable ArrayList of the passengers currently
     * on this train.
     * 
     * @return list of passengers on this train
     */
    public ArrayList<Passenger> getPassengers() {
        return this.passengers;
    }
    
    /**
     * Renders the Train at its current position, if it is not in a
     * station. Changes colour depending on its direction and changes
     * size depending on the number of passengers.
     *
     * @param renderer the ShapeRenderer used to render to the screen
     */
    public void render(ShapeRenderer renderer) {
        // We don't want to render trains currently in or passing
        // through a station.
        if (!this.inStation()) {
            // Determine colour based on direction
            Color colour = this.forward ? FORWARD_COLOUR : BACKWARD_COLOUR;
            // Calculate a scaling percentage to indicate how many
            // passengers are on the train
            float percentage = this.renderScalingPercentage();
    
            // Set colour and render according to the calculated percentage
            renderer.setColor(colour.cpy().lerp(Color.MAROON, percentage));
            renderer.circle(this.position.x, this.position.y,
                            TRAIN_WIDTH * (1 + percentage));
        }
    }
    
    /**
     * Calculates a percentage of how much the Train width and colour
     * should be changed depending on how full the train is and
     * what the total capacity is.
     * 
     * NOTE: This equation was created to scale linearly while
     * still giving the same values for the provided train sizes
     * with capacities of 10 and 80.
     * 
     * @return a scaling coefficient for rendering the train
     */
    private float renderScalingPercentage() {
        float denominator = this.maxPassengers * SCALING_MULTIPLIER
                            + SCALING_CONSTANT;
        return this.passengers.size() / denominator;
    }


}
