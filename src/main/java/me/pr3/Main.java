package me.pr3;

import me.pr3.api.ShikakuGameFactory;
import me.pr3.api.types.ShikakuGame;
import me.pr3.impl.basic.BasicSolver;

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

        BasicSolver basicSolver = new BasicSolver();

        Instant begin = Instant.now();
        basicSolver.solve(shikakuGame);
        Instant end = Instant.now();
        System.out.println("Solve took: " + Duration.between(begin, end).toMillis() + "ms");

    }
}