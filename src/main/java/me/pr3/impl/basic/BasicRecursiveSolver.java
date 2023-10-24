package me.pr3.impl.basic;

import me.pr3.api.ISolver;
import me.pr3.api.types.ShikakuGame;
import me.pr3.api.ui.ShikakuGamePainter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tim
 */
public class BasicRecursiveSolver implements ISolver {

    boolean debug = false;

    private Map<Integer, java.util.List<Rectangle>> possiblePlacements = null;


    @Override
    public ShikakuGame solve(ShikakuGame game) {
        java.util.List<Map.Entry<Point, Integer>> initialEntries = getEntriesSortedBySize(game);
        Set<Integer> usedNumbers = getNumbersUsedByGame(initialEntries);
        possiblePlacements = getAllPossiblePlacements(usedNumbers);

        SolverResult result = recursiveSolve(game.clone(), initialEntries);
        return result.game;
    }


    public SolverResult recursiveSolve(ShikakuGame game, List<Map.Entry<Point, Integer>> entries) {




        List<Map.Entry<Point, Integer>> filledInEntries = new ArrayList<>();

        boolean foundUnambiguousPlacement;

        do {
            foundUnambiguousPlacement = false;
            for (Map.Entry<Point, Integer> entry : entries) {
                Point point = entry.getKey();
                Integer number = entry.getValue();
                List<Rectangle> possiblePlacementsForNumber = possiblePlacements.get(number);
                //Get all points we have to consider for this placement
                List<Point> points = new ArrayList<>(entries.stream().map(Map.Entry::getKey).toList());
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
                    foundUnambiguousPlacement = true;
                    game.rectangles.add(validRectangles.get(0));
                    System.out.println("Found Unambiguous Rectangle Placement: " + validRectangles.get(0));
                    filledInEntries.add(entry);
                }else if(validRectangles.size() == 0){
                    System.out.println("Found Entry with no possible placements, dropping last placement");
                    return new SolverResult(game, Result.FAILED);
                }
            }

            entries.removeAll(filledInEntries);
        } while (foundUnambiguousPlacement);

        if(entries.size() == 0){
            return new SolverResult(game, Result.SOLVED);
        }

        Map.Entry<Point, Integer> biggestEntry = entries.get(0);
        entries.remove(0);

        Point point = biggestEntry.getKey();
        Integer number = biggestEntry.getValue();
        List<Rectangle> possiblePlacementsForNumber = possiblePlacements.get(number);
        //Get all points we have to consider for this placement
        List<Point> points = new ArrayList<>(entries.stream().map(Map.Entry::getKey).toList());
        points.remove(biggestEntry.getKey());
        //Count the number of valid placement options
        for (Rectangle rectangle : possiblePlacementsForNumber) {
            Rectangle translatedRectangle = new Rectangle(rectangle);
            translatedRectangle.translate(point.x, point.y);
            if (checkIfRectangleIsValid(game, translatedRectangle, points, new Rectangle(game.bounds))) {
                ShikakuGame clonedGame = game.clone();
                clonedGame.rectangles.add(translatedRectangle);
                SolverResult result = recursiveSolve(clonedGame, new ArrayList<>(entries));
                System.out.println("Found Ambiguous Step: " + rectangle);
                if (result.result == Result.SOLVED) {
                    return result;
                }
            }
        }

        boolean solved = game.isSolved();
        if (!solved) System.out.println("Reached Dead End, undoing last step");
        return new SolverResult(game, solved ? Result.SOLVED : Result.FAILED);
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

    private record SolverResult(ShikakuGame game, Result result) {
    }

    private enum Result {
        SOLVED,
        FAILED
    }
}
