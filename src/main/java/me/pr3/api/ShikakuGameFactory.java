package me.pr3.api;

import me.pr3.api.types.ShikakuGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author tim
 */
public class ShikakuGameFactory {

    public static ShikakuGame generateGame(Dimension bounds, int divisions) {
        ShikakuGame shikakuGame = new ShikakuGame(bounds);
        while (divisions > 0) {

           // doDebugOutput(shikakuGame);

            Rectangle rectangleToSplit = getRandomRectangleFromGame(shikakuGame);
            shikakuGame.rectangles.remove(rectangleToSplit);
            shikakuGame.rectangles.addAll(splitRectangle(rectangleToSplit));

            divisions--;
        }

        Random r = ThreadLocalRandom.current();
        for (Rectangle rectangle : shikakuGame.rectangles) {
           shikakuGame.numbers.put(getRandomPointInRectangle(rectangle), rectangle.width * rectangle.height);
        }

        shikakuGame.rectangles = new ArrayList<>();

        return shikakuGame;
    }

    public static Point getRandomPointInRectangle(Rectangle rect) {
        int x = ThreadLocalRandom.current().nextInt(rect.x, rect.x + rect.width);
        int y = ThreadLocalRandom.current().nextInt(rect.y, rect.y + rect.height);
        return new Point(x, y);
    }

    private static void doDebugOutput(ShikakuGame shikakuGame) {
        int[][] matrix = new int[shikakuGame.bounds.height][shikakuGame.bounds.width];
        int i = 1;
        for (Rectangle rectangle : shikakuGame.rectangles) {
            for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                    matrix[y][x] = i;
                }
            }
            i++;
        }
        for (int[] ints : matrix) {
            System.out.println(Arrays.toString(ints));
        }

        System.out.println("=================================================");
        for (Rectangle rectangle : shikakuGame.rectangles) {
            System.out.println(rectangle);
        }
    }


    private static Rectangle getRandomRectangleFromGame(ShikakuGame shikakuGame) {
        Random r = ThreadLocalRandom.current();
        int index = r.nextInt(shikakuGame.rectangles.size());
        return shikakuGame.rectangles.get(index);
    }


    private static List<Rectangle> splitRectangle(Rectangle original) {

        List<Rectangle> result = new ArrayList<>();
        boolean isVertical = ThreadLocalRandom.current().nextBoolean();

        int splitCoordinate;
        if (isVertical) {
            splitCoordinate = ThreadLocalRandom.current().nextInt(original.x, original.x + original.width);
        } else {
            splitCoordinate = ThreadLocalRandom.current().nextInt(original.y, original.y + original.height);
        }

        if (isVertical) {
            int leftWidth = Math.max(0, splitCoordinate - original.x);
            int rightWidth = Math.max(0, original.x + original.width - splitCoordinate);

            if (leftWidth > 0) {
                Rectangle leftRectangle = new Rectangle(original.x, original.y, leftWidth, original.height);
                result.add(leftRectangle);
            }

            if (rightWidth > 0) {
                Rectangle rightRectangle = new Rectangle(splitCoordinate, original.y, rightWidth, original.height);
                result.add(rightRectangle);
            }
        } else {
            int topHeight = Math.max(0, splitCoordinate - original.y);
            int bottomHeight = Math.max(0, original.y + original.height - splitCoordinate);

            if (topHeight > 0) {
                Rectangle topRectangle = new Rectangle(original.x, original.y, original.width, topHeight);
                result.add(topRectangle);
            }

            if (bottomHeight > 0) {
                Rectangle bottomRectangle = new Rectangle(original.x, splitCoordinate, original.width, bottomHeight);
                result.add(bottomRectangle);
            }
        }

        //Never create 1x1s
        if(result.stream().anyMatch(rec -> rec.width * rec.height <= 1))return List.of(original);

        return result;
    }

}
