package me.pr3;

import me.pr3.api.ShikakuGameFactory;
import me.pr3.api.types.ShikakuGame;
import me.pr3.impl.basic.BasicSolverRewriteRewrite;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * @author tim
 */
public class Main {
    public static void main(String[] args) {
        ShikakuGame shikakuGame = ShikakuGameFactory.generateGame(new Dimension(10, 10), 100);

        shikakuGame.printBoard();

        BasicSolverRewriteRewrite basicSolver = new BasicSolverRewriteRewrite();

        Instant begin = Instant.now();
        basicSolver.solve(shikakuGame);
        Instant end = Instant.now();

        System.out.println("Solve took: " + Duration.between(begin, end).toMillis() + "ms isSolved:" + shikakuGame.isSolved());

        doDebugOutput(shikakuGame);

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
}