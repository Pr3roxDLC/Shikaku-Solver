package me.pr3.impl.basic;

import me.pr3.api.ISolver;
import me.pr3.api.types.ShikakuGame;
import me.pr3.api.types.ShikakuGameState;

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
                    if (x * y == number) {
                        if (debug)
                            System.out.println("Found Valid rectangle: x: " + x + " y: " + y + " for number: " + number);
                        //Get all offsets
                        for (int xOffset = 0; xOffset < x; xOffset++) {
                            for (int yOffset = 0; yOffset < y; yOffset++) {
                                if (debug)
                                    System.out.println("Adding rectangle: " + new Rectangle(-xOffset, -yOffset, x, y));
                                possiblePlacements.get(number).add(new Rectangle(-xOffset, -yOffset, x, y));
                            }
                        }
                    }
                }
            }

        }

        Stack<ShikakuGameState> ambiguousSteps = new Stack<>();
        boolean addedNewRect = true;
        while (!entries.isEmpty()) {
            //If we dont have any unambiguous rectangles to fill we have to take a guess
            // if this happens, we push a copy of the current game state to a stack, so that if we reach a dead end
            // we can revert the step and try a different move until we run out of possible moves at which we will go
            // back another step and repeat
            if(!addedNewRect){
                Map.Entry<Point, Integer> biggestEntry = entries.get(0);

                ShikakuGame clonedGame = game.clone();
                if(ambiguousSteps.isEmpty()){
                    ambiguousSteps.push(new ShikakuGameState(clonedGame, new ArrayList<>(), biggestEntry.getKey()));
                }else{
                    ShikakuGameState lastPushedState = ambiguousSteps.pop();

                }

            }

            //Find unambiguous rectangles and fill them in
            addedNewRect = false;
            List<Map.Entry<Point, Integer>> pointsToRemove = new ArrayList<>();
            for (Map.Entry<Point, Integer> entry : entries) {
                boolean isUnambiguous = false;

                List<Point> points = new ArrayList<>(entries.stream().map(Map.Entry::getKey).toList());
                points.remove(entry.getKey());
                List<Rectangle> validRectangles = new ArrayList<>();

                //Check all possible placements for the current entry and create a list of all placements that would
                // be valid
                for (Rectangle rectangle : possiblePlacements.get(entry.getValue())) {
                    Rectangle translatedRectangle = (Rectangle) rectangle.clone();
                    translatedRectangle.translate(entry.getKey().x, entry.getKey().y);

                    if (!checkIfRectangleIsValid(game, translatedRectangle, points, bounds)) {
                        validRectangles.add(translatedRectangle);
                    }
                }

                //If we have exactly one valid placement, we know that the placement is unambiguous and we can therefor
                // add it to our game
                if (validRectangles.size() == 1) {
                    System.out.println("Found Unambiguous Rectangle Placement: " + validRectangles.get(0));
                    isUnambiguous = true;
                }

                if (isUnambiguous) {
                    addedNewRect = true;
                    game.rectangles.add(validRectangles.get(0));
                    pointsToRemove.add(entry);
                }
            }

            for (Map.Entry<Point, Integer> entry : pointsToRemove) {
                entries.remove(entry);
            }
            System.out.println("Iteration End");
        }
    }

    private boolean checkIfRectangleIsValid(ShikakuGame game, Rectangle rectangle, Collection<Point> points, Rectangle bounds){

        //Check for bounds
        if (!bounds.union(rectangle).equals(bounds)) return false;

        //Check for intersection with other numbers
        boolean intersectsWithPoints = false;
        for (Point point : points) {
            if (rectangle.contains(point)) intersectsWithPoints = true;
        }
        if (intersectsWithPoints) return false;

        //Check for interaction with other rectangles
        boolean intersectWithOtherRectangle = false;
        for (Rectangle otherRectangle : game.rectangles) {
            if (rectangle.intersects(otherRectangle)) {
                intersectWithOtherRectangle = true;
            }
        }
        return !intersectWithOtherRectangle;
    }


    public void enableDebug() {
        this.debug = true;
    }
}
