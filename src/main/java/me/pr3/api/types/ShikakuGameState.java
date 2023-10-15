package me.pr3.api.types;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * @author tim
 */
public class ShikakuGameState {
    public ShikakuGame state;
    public List<Rectangle> unsuccessfulRectangles;
    public Point lastTriedNumber;

    public ShikakuGameState(ShikakuGame state, List<Rectangle> unsuccessfulRectangles, Point lastTriedNumber) {
        this.state = state;
        this.unsuccessfulRectangles = unsuccessfulRectangles;
        this.lastTriedNumber = lastTriedNumber;
    }

    public ShikakuGame getState() {
        return state;
    }

    public void setState(ShikakuGame state) {
        this.state = state;
    }

    public List<Rectangle> getUnsuccessfulRectangles() {
        return unsuccessfulRectangles;
    }

    public void setUnsuccessfulRectangles(List<Rectangle> unsuccessfulRectangles) {
        this.unsuccessfulRectangles = unsuccessfulRectangles;
    }

    public Point getLastTriedNumber() {
        return lastTriedNumber;
    }

    public void setLastTriedNumber(Point lastTriedNumber) {
        this.lastTriedNumber = lastTriedNumber;
    }
}
