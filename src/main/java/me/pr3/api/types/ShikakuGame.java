package me.pr3.api.types;


import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author tim
 */
public class ShikakuGame implements Cloneable{

    public Dimension bounds; //The bounds of the Rectangle board
    public List<Rectangle> rectangles; //The list of Rectangles, a solver will populate this list according to the numbers map
    public Map<Point, Integer> numbers; //Map of each number and its position as a 2d point

    public ShikakuGame(Dimension bounds){
        this.bounds = bounds;
        this.rectangles = new ArrayList<>();
        rectangles.add(new Rectangle(0,0, bounds.width, bounds.height));
        this.numbers = new HashMap<>();
    }

    public boolean isSolved(){
        //Check for spaces not occupied by any rectangle
        for ( int x = 0; x < bounds.width; x++) {
            for ( int y = 0; y < bounds.height; y++) {
                int finalX = x;
                int finalY = y;
                List<Rectangle> rectanglesContainingPoint = rectangles.stream().filter(rectangle -> rectangle.contains(finalX, finalY)).toList();
                int numberOfRectanglesOnCoordinate = rectanglesContainingPoint.size();
                if(numberOfRectanglesOnCoordinate != 1)return false;
            }
        }

        for (Map.Entry<Point, Integer> entry : numbers.entrySet()) {
            Point point = entry.getKey();
            Integer integer = entry.getValue();
            List<Rectangle> rectanglesContainingPoint = rectangles.stream().filter(rectangle -> rectangle.contains(point)).toList();
            assert rectanglesContainingPoint.size() == 1;
            Rectangle rectangle = rectanglesContainingPoint.get(0);
            if(rectangle.width * rectangle.height != integer)return false;
        }
        return true;
    }

    public void printBoard(){
        String[][] board = new String[10][10];
        for (String[] strings : board) {
            Arrays.fill(strings, 0, 10, "  ");
        }
        numbers.forEach((point, integer) -> {
            board[point.y][point.x] = integer >= 10 ? integer.toString() : " " + integer;
        });

        for (String[] strings : board) {
            System.out.println(Arrays.toString(strings));
        }
    }

    @Override
    public ShikakuGame clone() {
        try {
            ShikakuGame clone = (ShikakuGame) super.clone();
            clone.numbers = new HashMap<>();
            clone.numbers.putAll(numbers);
            clone.bounds = new Dimension(bounds);
            clone.rectangles = new ArrayList<>();
            clone.rectangles.addAll(rectangles);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
