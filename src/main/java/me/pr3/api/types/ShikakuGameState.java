package me.pr3.api.types;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tim
 */
public class ShikakuGameState {
    public ShikakuGame state;
    public Set<Rectangle> rectanglesTriedBefore;
    public Point lastTriedNumber;
    public List<Map.Entry<Point, Integer>> entries;


    public ShikakuGameState(ShikakuGame state, Set<Rectangle> rectanglesTriedBefore, Point lastTriedNumber, List<Map.Entry<Point, Integer>> entries) {
        this.state = state;
        this.rectanglesTriedBefore = rectanglesTriedBefore;
        this.lastTriedNumber = lastTriedNumber;
        this.entries = entries;
    }

    public ShikakuGame getState() {
        return state;
    }

    public void setState(ShikakuGame state) {
        this.state = state;
    }

    public Set<Rectangle> getRectanglesTriedBefore() {
        return rectanglesTriedBefore;
    }

    public void setRectanglesTriedBefore(Set<Rectangle> rectanglesTriedBefore) {
        this.rectanglesTriedBefore = rectanglesTriedBefore;
    }

    public Point getLastTriedNumber() {
        return lastTriedNumber;
    }

    public void setLastTriedNumber(Point lastTriedNumber) {
        this.lastTriedNumber = lastTriedNumber;
    }

    public List<Map.Entry<Point, Integer>> getEntries() {
        return entries;
    }

    public void setEntries(List<Map.Entry<Point, Integer>> entries) {
        this.entries = entries;
    }
}
