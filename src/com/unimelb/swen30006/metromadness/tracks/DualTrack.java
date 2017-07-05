package com.unimelb.swen30006.metromadness.tracks;

import java.awt.geom.Point2D.Float;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unimelb.swen30006.metromadness.exceptions.FullTrackException;
import com.unimelb.swen30006.metromadness.trains.Train;

/**
 * The DualTrack class is an extension of Track. It represents two tracks
 * that can each be occupied by a train, given they are traveling in
 * opposite directions. One track is 'forward' while the other is
 * 'backward'.
 *
 * DualTrack is responsible for deciding whether trains can
 * enter each track (depending on their direction), and entering/
 * removing them from each track. DualTrack is also responsible
 * for rendering itself on the simulation.
 */
public class DualTrack extends Track {

    /** The colour used to draw a dual track. */
    private static final Color RENDER_COLOUR = new Color(245f/255f,
        244f/255f, 245f/255f, 0.5f);

    /** If the track is occupied in the forward direction. */
    private boolean forwardOccupied;
    /** If the track is occupied in the backward direction. */
    private boolean backwardOccupied;

    /**
     * Constructor for DualTrack. Initialises with a start point,
     * end point and the colour of the DualTrack.
     *
     * @param start the starting location of the DualTrack
     * @param end the ending location of the DualTrack
     * @param colour the colour of the DualTrack
     */
    public DualTrack(Float start, Float end, Color colour) {
        super(start, end, colour);
        this.forwardOccupied = false;
        this.backwardOccupied = false;
    }

    /**
     * Renders the DualTrack based on its start and ending position and
     * the colour set.
     *
     * @param renderer the ShapeRenderer used to render the screen
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(this.getColour());
        renderer.rectLine(getStartPos().x, getStartPos().y,
                          getEndPos().x, getEndPos().y, LINE_WIDTH);
        renderer.setColor(RENDER_COLOUR.cpy().lerp(this.getColour(), 0.5f));
        renderer.rectLine(getStartPos().x, getStartPos().y,
                          getEndPos().x, getEndPos().y, LINE_WIDTH / 3);
        renderer.setColor(this.getColour());
    }

    /**
     * Calls the train to enter the track, making the direction that
     * the train is traveling in occupied.
     *
     * @param train the Train to enter the track
     * @throws FullTrackException if train cannot enter this track
     */
    @Override
    public void enter(Train train) throws FullTrackException {
        if (!this.canEnter(train)) {
            throw new FullTrackException();
        }

        if (train.isForward()) {
            this.forwardOccupied = true;
        } else {
            this.backwardOccupied = true;
        }
    }

    /**
     * Decides whether a train can enter the track by checking if
     * the track in the direction the train is traveling is occupied.
     *
     * @param train the Train requesting to enter the track
     * @return whether or not the train can enter the track
     */
    @Override
    public boolean canEnter(Train train) {
        if (train.isForward()) {
            return !this.forwardOccupied;
        } else {
            return !this.backwardOccupied;
        }
    }

    /**
     * Calls the train to leave the track, making the direction that
     * the train is traveling in unoccupied.
     *
     * @param train the Train to enter the track
     */
    @Override
    public void leave(Train train) {
        if (train.isForward()) {
            this.forwardOccupied = false;
        } else {
            this.backwardOccupied = false;
        }
    }
}
