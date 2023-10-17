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
public class BasicSolverRewrite implements ISolver {

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

                //First time branching
                if (stack.isEmpty()) {
                    Map.Entry<Point, Integer> biggestEntry = currentEntries.get(0);
                    List<Rectangle> placementsForBiggestEntry = possiblePlacements.get(biggestEntry.getValue());

                    List<Point> points = new ArrayList<>(currentEntries.stream().map(Map.Entry::getKey).toList());
                    points.remove(biggestEntry.getKey());

                    List<Rectangle> alreadyTriedRectangles = new ArrayList<>();

                    boolean foundPlacementForInitialAmbiguousStep = false;
                    ShikakuGame gameStateBeforeFirstBranch = game.clone();
                    for (Rectangle rectangle : placementsForBiggestEntry) {
                        Rectangle translatedRectangle = new Rectangle(rectangle);
                        translatedRectangle.translate(biggestEntry.getKey().x, biggestEntry.getKey().y);
                        alreadyTriedRectangles.add(translatedRectangle);
                        //System.out.println("Checking Ambiguous Placements: " + alreadyTriedRectangles.size() + " of: " + placementsForBiggestEntry.size()  + " for: " + biggestEntry.getKey());
                        if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                            System.out.println("Found Ambiguous Rectangle Placement: " + translatedRectangle);
                            game.rectangles.add(translatedRectangle);
                            hasLastRunCreatedChanges = true;
                            foundPlacementForInitialAmbiguousStep = true;
                            filledInEntries.add(biggestEntry);
                            break; //We only want to add exactly one
                        }
                    }
                    if (!foundPlacementForInitialAmbiguousStep) {
                        throw new IllegalStateException("Unable to place first Ambiguous Step, This should never happen");
                    }
                    List<Map.Entry<Point, Integer>> updatedEntries = new ArrayList<>(currentEntries);
                    updatedEntries.remove(biggestEntry);
                    stack.push(new ShikakuGameState(gameStateBeforeFirstBranch, alreadyTriedRectangles, biggestEntry.getKey(), updatedEntries));
                } else {
                    ShikakuGameState lastPushedState = stack.pop();
                    game = lastPushedState.state.clone();

                    Map.Entry<Point, Integer> biggestEntry = lastPushedState.entries.get(0);
                    List<Rectangle> placementsForBiggestEntry = possiblePlacements.get(game.numbers.get(lastPushedState.lastTriedNumber));

                    List<Point> points = new ArrayList<>(lastPushedState.entries.stream().map(Map.Entry::getKey).toList());
                    points.remove(biggestEntry.getKey());

                    ArrayList<Map.Entry<Point, Integer>> updatedEntries = new ArrayList<>(lastPushedState.entries);
                    updatedEntries.remove(biggestEntry);

                    //Iterate over all the ones left and if we find one that is valid, push the updated gamestate to the
                    // stack, if none are found restore the Game from the lastPushedState etc and rerun
                    boolean foundAnotherValidPlacement = false;
                    for (Rectangle rectangle : placementsForBiggestEntry) {
                        Rectangle translatedRectangle = new Rectangle(rectangle);
                        translatedRectangle.translate(biggestEntry.getKey().x, biggestEntry.getKey().y);
                        if (lastPushedState.rectanglesTriedBefore.contains(translatedRectangle)) continue;
                        lastPushedState.rectanglesTriedBefore.add(translatedRectangle);
                        System.out.println("Checking Ambiguous Placements: " + lastPushedState.rectanglesTriedBefore.size() + " of: " + placementsForBiggestEntry.size() + " for: " + biggestEntry.getKey());
                        if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                            System.out.println("Found Ambiguous Rectangle Placement: " + translatedRectangle);
                            game.rectangles.add(translatedRectangle);
                            hasLastRunCreatedChanges = true;
                            filledInEntries.add(biggestEntry);
                            foundAnotherValidPlacement = true;
                            break;
                        }
                    }


                    //We found another placement
                    if (foundAnotherValidPlacement) {
                        //Update the lastPushedState with the new placement
                        stack.push(new ShikakuGameState(
                                lastPushedState.state,
                                lastPushedState.rectanglesTriedBefore,
                                lastPushedState.lastTriedNumber,
                                new ArrayList<>(currentEntries)));
                        //Push the next placement



                        Map.Entry<Point, Integer> nextBiggestEntry = updatedEntries.get(0);
                        List<Rectangle> placementsForNextBiggestEntry = possiblePlacements.get(nextBiggestEntry.getValue());
                        List<Rectangle> alreadyTriedRectangles = new ArrayList<>();

                        List<Point> pointsForNextBiggest = new ArrayList<>(updatedEntries.stream().map(Map.Entry::getKey).toList());
                        pointsForNextBiggest.remove(nextBiggestEntry.getKey());

                        boolean foundPlacement = false;
                        for (Rectangle rectangle : placementsForNextBiggestEntry) {
                            Rectangle translatedRectangle = new Rectangle(rectangle);
                            translatedRectangle.translate(nextBiggestEntry.getKey().x, nextBiggestEntry.getKey().y);
                            alreadyTriedRectangles.add(translatedRectangle);
                            //System.out.println("Checking Ambiguous Placements: " + alreadyTriedRectangles.size() + " of: " + placementsForNextBiggestEntry.size() + " for: " + biggestEntry.getKey());
                            if (checkIfRectangleIsValid(game, translatedRectangle, pointsForNextBiggest, new Rectangle(game.bounds))) {
                                System.out.println("Found Ambiguous Rectangle Placement: " + translatedRectangle);
                                game.rectangles.add(translatedRectangle);
                                hasLastRunCreatedChanges = true;
                                foundPlacement = true;
                                filledInEntries.add(biggestEntry);
                                break; //We only want to add exactly one
                            }
                        }

                        if (foundPlacement) {
                            stack.push(new ShikakuGameState(
                                    game.clone(), alreadyTriedRectangles, nextBiggestEntry.getKey(), updatedEntries
                            ));
                        }else{
                            //If no other valid placement was found, restore the game state
                            System.out.println("1 Dropped Last Ambiguous Placement StackSize: " + stack.size());
                            game = lastPushedState.state;
                            currentEntries = new ArrayList<>(lastPushedState.entries);
                        }


                    } else {
                        //If no other valid placement was found, restore the game state
                        System.out.println("2 Dropped Last Ambiguous Placement StackSize: " + stack.size());
                        game = lastPushedState.state;
                        currentEntries = new ArrayList<>(lastPushedState.entries);
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


    private Set<Integer> getNumbersUsedByGame(List<Map.Entry<Point, Integer>> initialEntries) {
        return initialEntries.stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    private List<Map.Entry<Point, Integer>> getEntriesSortedBySize(ShikakuGame game) {
        List<Map.Entry<Point, Integer>> initialEntries = new ArrayList<>(game.numbers.entrySet());
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
