package me.pr3.impl.basic;

import me.pr3.api.ISolver;
import me.pr3.api.types.ShikakuGame;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tim
 */
public class BasicSolver implements ISolver {

    private boolean debug = false;

    @Override
    public void solve(ShikakuGame game) {
        List<Map.Entry<Point, Integer>> entries = new ArrayList<>(game.numbers.entrySet());
        entries.sort(Comparator.<Map.Entry<Point, Integer>>comparingInt(Map.Entry::getValue).reversed());
        Set<Integer> usedNumbers = entries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());
        Map<Integer, List<Rectangle>> possiblePlacements = new HashMap<>();

        Rectangle bounds = new Rectangle(game.bounds);

        //Pre-Generate All Possible Placements for every number used on the board
        for (Integer number : usedNumbers) {
            possiblePlacements.put(number, new ArrayList<>());
            //I'm sure there is a better way to calculate this, but it works and only runs once per solve so its no big
            // problem
            for (int x = 0; x <= number; x++) {
                for (int y = 0; y <= number; y++) {
                    if(x * y == number){
                        if(debug)System.out.println("Found Valid rectangle: x: " + x + " y: " + y + " for number: " + number );
                        //Get all offsets
                        for (int xOffset = 0; xOffset < x; xOffset++) {
                            for (int yOffset = 0; yOffset < y; yOffset++) {
                                if(debug)System.out.println("Adding rectangle: " + new Rectangle(-xOffset, -yOffset, x, y));
                                possiblePlacements.get(number).add(new Rectangle(-xOffset, -yOffset, x, y));
                            }
                        }
                    }
                }
            }

        }

        for (Map.Entry<Point, Integer> entry : entries) {
            boolean isUnambiguous = false;

            List<Point> points = new ArrayList<>(entries.stream().map(Map.Entry::getKey).toList());
            points.remove(entry.getKey());
            List<Rectangle> validRectangles = new ArrayList<>();

            for (Rectangle rectangle : possiblePlacements.get(entry.getValue())) {
                Rectangle translatedRectangle = (Rectangle) rectangle.clone();
                translatedRectangle.translate(entry.getKey().x, entry.getKey().y);

              //Check for bounds
                if(bounds.union(translatedRectangle).equals(bounds)){
                    boolean intersectsWithPoints = false;
                    for (Point point : points) {
                        //Check for intersection with other numbers
                        if(translatedRectangle.contains(point))intersectsWithPoints = true;
                    }
                    if(intersectsWithPoints)continue;
                    boolean intersectWithOtherRectangle = false;
                    for (Rectangle otherRectangle : game.rectangles) {
                        //Check for interaction with other rectangles
                        if(translatedRectangle.intersects(otherRectangle)){
                            intersectWithOtherRectangle = true;
                        }
                    }
                    if(!intersectWithOtherRectangle){
                        validRectangles.add(translatedRectangle);
                    }
                }
            }

            if(validRectangles.size() == 1){
                System.out.println("Found Unambiguous Rectangle Placement: " + validRectangles.get(0));
            }

        }


    }


    public void enableDebug(){
        this.debug = true;
    }
}
