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
public class BasicSolverRewriteRewrite implements ISolver {

    private final boolean debug = false;

    @Override
    public void solve(ShikakuGame game) {
        List<Map.Entry<Point, Integer>> initialEntries = getEntriesSortedBySize(game);
        Set<Integer> usedNumbers = getNumbersUsedByGame(initialEntries);
        Map<Integer, List<Rectangle>> possiblePlacements = getAllPossiblePlacements(usedNumbers);

        Stack<ShikakuGameState> stack = new Stack<>();

        List<Map.Entry<Point, Integer>> currentEntries = new ArrayList<>(initialEntries);

        boolean hasLastRunCreatedChanges = true;
        while (!currentEntries.isEmpty()) {

            //List of entries that got filled in this iteration
            List<Map.Entry<Point, Integer>> filledInEntries = new ArrayList<>();

            //Check for unambiguous rectangles
            if (hasLastRunCreatedChanges) {
                hasLastRunCreatedChanges = false;

                //Iterate over all remaining numbers and check all of their placements
                for (Map.Entry<Point, Integer> entry : currentEntries) {
                    Point point = entry.getKey();
                    Integer number = entry.getValue();
                    List<Rectangle> possiblePlacementsForNumber = possiblePlacements.get(number);

                    //Get all points we have to consider for this placement
                    List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
                    points.remove(entry.getKey());

                    List<Rectangle> validRectangles = new ArrayList<>();

                    //Count the number of valid placement options
                    for (Rectangle rectangle : possiblePlacementsForNumber) {
                        Rectangle translatedRectangle = new Rectangle(rectangle);
                        translatedRectangle.translate(point.x, point.y);

                        if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                            validRectangles.add(translatedRectangle);
                        }
                    }

                    //If we have exactly one valid option fill it in
                    if (validRectangles.size() == 1) {
                        hasLastRunCreatedChanges = true;
                        game.rectangles.add(validRectangles.get(0));
                        System.out.println("Found Unambiguous Rectangle Placement: " + validRectangles.get(0));
                        filledInEntries.add(entry);
                    }

                }

            } else {
                //Check if there are any other possible placement
                Set<Rectangle> rectanglesAlreadyTried = new HashSet<>();
                Map.Entry<Point, Integer> firstPossibleEntry = null;
                Rectangle firstPossiblePlacement = null;

                for (Map.Entry<Point, Integer> entry : currentEntries) {
                    Point point = entry.getKey();
                    Integer number = entry.getValue();
                    List<Rectangle> possiblePlacementsForNumber = possiblePlacements.get(number);

                    //Get all points we have to consider for this placement
                    List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
                    points.remove(entry.getKey());
                    rectanglesAlreadyTried = new HashSet<>();
                    //Count the number of valid placement options
                    for (Rectangle rectangle : possiblePlacementsForNumber) {
                        Rectangle translatedRectangle = new Rectangle(rectangle);
                        translatedRectangle.translate(point.x, point.y);
                        rectanglesAlreadyTried.add(translatedRectangle);
                        if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                            firstPossibleEntry = entry;
                            firstPossiblePlacement = translatedRectangle;
                            break;
                        }
                    }
                    if (firstPossibleEntry != null) break;
                }

                if (firstPossibleEntry != null) {
                    System.out.println("Found Ambiguous Rectangle Placement: " + firstPossiblePlacement);
                    stack.push(new ShikakuGameState(game.clone(), rectanglesAlreadyTried, firstPossibleEntry.getKey(), new ArrayList<>(currentEntries)));
                    game.rectangles.add(firstPossiblePlacement);
                    currentEntries.remove(firstPossibleEntry);
                    hasLastRunCreatedChanges = true;
                } else {
                    ShikakuGameState lastPushedState = stack.pop();

                    game = lastPushedState.state;
                    currentEntries = lastPushedState.entries;

                    List<Rectangle> possiblePlacementsForNumber = possiblePlacements.get(game.numbers.get(lastPushedState.lastTriedNumber));

                    List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
                    points.remove(lastPushedState.lastTriedNumber);

                    Rectangle nextValidPlacementForLastTried = null;

                    for (Rectangle rectangle : possiblePlacementsForNumber) {
                        Rectangle translatedRectangle = new Rectangle(rectangle);
                        translatedRectangle.translate(lastPushedState.lastTriedNumber.x, lastPushedState.lastTriedNumber.y);
                        if (lastPushedState.rectanglesTriedBefore.contains(translatedRectangle)) continue;
                        lastPushedState.rectanglesTriedBefore.add(translatedRectangle);
                        if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                            nextValidPlacementForLastTried = translatedRectangle;
                            game.rectangles.add(nextValidPlacementForLastTried);
                            hasLastRunCreatedChanges = true;
                            break;
                        }
                    }

                    if (nextValidPlacementForLastTried != null) {
                        stack.push(new ShikakuGameState(game.clone(), lastPushedState.rectanglesTriedBefore, lastPushedState.lastTriedNumber, lastPushedState.entries));
                    } else {
                        System.out.println("Dropped Last Placement: " + lastPushedState.lastTriedNumber + " StackSize: " + stack.size());
                        hasLastRunCreatedChanges = true;
                    }

                }
            }

            //Filled in entries are locked in and dont have to be checked anymore
            for (Map.Entry<Point, Integer> entryToRemove : filledInEntries) {
                currentEntries.remove(entryToRemove);
            }

        }

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

    private Set<Integer> getNumbersUsedByGame(java.util.List<Map.Entry<Point, Integer>> initialEntries) {
        return initialEntries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    private java.util.List<Map.Entry<Point, Integer>> getEntriesSortedBySize(ShikakuGame game) {
        java.util.List<Map.Entry<Point, Integer>> initialEntries = new ArrayList<>(game.numbers.entrySet());
        initialEntries.sort(Comparator.<Map.Entry<Point, Integer>>comparingInt(Map.Entry::getValue).reversed());
        return initialEntries;
    }

    private Map<Integer, java.util.List<Rectangle>> getAllPossiblePlacements(Set<Integer> usedNumbers) {
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

}
