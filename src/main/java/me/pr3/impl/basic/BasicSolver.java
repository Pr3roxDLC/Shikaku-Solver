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
        List<Map.Entry<Point, Integer>> initialEntries = new ArrayList<>(game.numbers.entrySet());
        initialEntries.sort(Comparator.<Map.Entry<Point, Integer>>comparingInt(Map.Entry::getValue).reversed());
        Set<Integer> usedNumbers = initialEntries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());

        //Pre-Generate All Possible Placements for every number used on the board
        Map<Integer, List<Rectangle>> possiblePlacements = getAllPossiblePlacements(usedNumbers);

        Rectangle bounds = new Rectangle(game.bounds);

        Stack<ShikakuGameState> ambiguousSteps = new Stack<>();

        List<Map.Entry<Point, Integer>> currentEntries = new ArrayList<>(initialEntries);
        boolean addedNewRect = true;
        while (!currentEntries.isEmpty()) {
            //If we dont have any unambiguous rectangles to fill we have to take a guess
            // if this happens, we push a copy of the current game state to a stack, so that if we reach a dead end
            // we can revert the step and try a different move until we run out of possible moves at which we will go
            // back another step and repeat
            if (!addedNewRect) {
                Map.Entry<Point, Integer> biggestEntry = currentEntries.get(0);

                ShikakuGame clonedGame = game.clone();
                if (ambiguousSteps.isEmpty()) {
                    //     System.out.println("Added First Ambiguous Placement");
                    ambiguousSteps.push(new ShikakuGameState(clonedGame, new ArrayList<>(), biggestEntry.getKey(), new ArrayList<>(currentEntries)));
                } else {
                    List<Rectangle> allPossiblePlacementsForBiggestEntry = possiblePlacements.get(biggestEntry.getValue());
                    ShikakuGameState lastPushedState = ambiguousSteps.pop();
                    //Check if we haven't tried all possible placements for currently the biggest entry yet
                    if (lastPushedState.rectanglesTriedBefore.size() != allPossiblePlacementsForBiggestEntry.size()) {
                        //Get next rectangle and check if it is valid
                        Rectangle rectangle = allPossiblePlacementsForBiggestEntry.get(lastPushedState.rectanglesTriedBefore.size());
                        Rectangle translatedRectangle = (Rectangle) rectangle.clone();
                        translatedRectangle.translate(biggestEntry.getKey().x, biggestEntry.getKey().y);

                        List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
                        points.remove(biggestEntry.getKey());
                        System.out.println("Checking for Valid Placement: " + lastPushedState.rectanglesTriedBefore.size() + " out of: " + allPossiblePlacementsForBiggestEntry.size());
                        if (checkIfRectangleIsValid(clonedGame, translatedRectangle, points, bounds)) {
                            System.out.println("Added Valid Ambiguous Rectangle: " + translatedRectangle);
                            game.rectangles.add(translatedRectangle);
                            currentEntries.remove(biggestEntry);
                            lastPushedState.state = game.clone();
                            lastPushedState.rectanglesTriedBefore.add(rectangle);
                            ambiguousSteps.push(lastPushedState);
                            ambiguousSteps.push(new ShikakuGameState(game.clone(), new ArrayList<>(), currentEntries.get(0).getKey(), new ArrayList<>(currentEntries)));
                            addedNewRect = true;
                            continue;
                        }
                        System.out.println("Tried unsuccessful Rectangle");
                        lastPushedState.state = game.clone();
                        lastPushedState.rectanglesTriedBefore.add(rectangle);
                        lastPushedState.entries = new ArrayList<>(currentEntries);
                        ambiguousSteps.push(lastPushedState);
                    } else {
                        System.out.println("Dropped Last Ambiguous Placement");
                        game = lastPushedState.state;
                        currentEntries = new ArrayList<>(lastPushedState.entries);
                        addedNewRect = true;
                        continue;
                    }

                }

            }

            //Find unambiguous rectangles and fill them in
            addedNewRect = false;
            List<Map.Entry<Point, Integer>> pointsToRemove = new ArrayList<>();
            for (Map.Entry<Point, Integer> entry : currentEntries) {
                boolean isUnambiguous = false;

                List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
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
                currentEntries.remove(entry);
            }
            //  System.out.println("Iteration End");
        }
    }

    private Map<Integer, List<Rectangle>> getAllPossiblePlacements(Set<Integer> usedNumbers) {
        Map<Integer, List<Rectangle>> possiblePlacements = new HashMap<>();
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
        return possiblePlacements;
    }

    private boolean checkIfRectangleIsValid(ShikakuGame game, Rectangle rectangle, Collection<Point> points, Rectangle bounds) {

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
