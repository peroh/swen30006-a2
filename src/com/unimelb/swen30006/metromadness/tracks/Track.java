package com.unimelb.swen30006.metromadness.tracks;

import java.awt.geom.Point2D;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.exceptions.FullTrackException;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * The Track class is a representation of the tracks that
 * exist on each line in the station. A Track can only have
 * one train on it at a time.
 *
 * Track is responsible for deciding whether trains can
 * enter the track (if it is occupied or not), and entering/
 * removing them from the track. The track is also resposible
 * for rendering itself on the simulation.
 */
public class Track {

    /** Radius of the track to be rendered */
    public static final float DRAW_RADIUS = 10f;
    /** The width of the line to be rendered for the track */
    public static final int LINE_WIDTH = 6;

    /** Start position of track when rendered */
    private Point2D.Float startPos;
    /** End position of track when rendered */
    private Point2D.Float endPos;
    /** Colour of the track */
    private Color colour;
    /** If the track is occupied */
    private boolean occupied;

    /**
     * Initialises a new Track with a given start and end position,
     * and a track colour.
     *
     * @param start the location of the start of the track
     * @param end the location of the end of the track
     * @param colour the colour of the track
     */
    public Track(Point2D.Float start, Point2D.Float end, Color colour) {
        this.startPos = start;
        this.endPos = end;
        this.colour = colour;
        this.occupied = false;
    }

    /**
     * Renders the Track based on its start and ending position and
     * the colour set.
     *
     * @param renderer the ShapeRenderer used to render the screen
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(this.getColour());
        renderer.rectLine(startPos.x, startPos.y, endPos.x, endPos.y,
                          LINE_WIDTH);
    }

    /**
     * Decides whether a train can enter the track by checking if
     * the track is occupied.
     *
     * @param train the Train requesting to enter the track
     * @return whether or not the train can enter the track
     */
    public boolean canEnter(Train train) {
        return !this.occupied;
    }

    /**
     * Calls the train to enter the track, thus making track occupied.
     *
     * @param train the Train to enter the track
     * @throws FullTrackException if train cannot enter the track
     */
    public void enter(Train train) throws FullTrackException {
        if (!this.canEnter(train)) {
            throw new FullTrackException();
        }
        
        this.occupied = true;
    }

    /**
     * Calls the train to leave the track, thus making the track unoccupied.
     *
     * @param train the Train to leave the track
     */
    public void leave(Train train) {
        this.occupied = false;
    }

    /**
     * Gets the starting position of the track.
     *
     * @return the 2D location of the start of the track
     */
    public Point2D.Float getStartPos() {
        return this.startPos;
    }

    /**
     * Get the end position of the track.
     *
     * @return the 2D location of the end of the track
     */
    public Point2D.Float getEndPos() {
        return this.endPos;
    }

    /**
     * Gets the colour of the track.
     *
     * @return the colour of the track
     */
    public Color getColour() {
        return this.colour;
    }
}
