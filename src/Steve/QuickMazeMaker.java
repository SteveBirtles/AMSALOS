package Steve;

import java.util.Random;

public class QuickMazeMaker {

    public static int[][] makeMake(int width, int height) {

        Random rnd = new Random();

        int[][] maze = new int[width][];
        for (int x = 0; x < width; x++) {
            maze[x] = new int[height];
            for (int y = 0; y < height; y++) {
                maze[x][y] = rnd.nextInt(4);
            }
        }

        return maze;
    }

    public static int[][] emptyMap(int width, int height) {

        int[][] maze = new int[width][];
        for (int x = 0; x < width; x++) {
            maze[x] = new int[height];
        }

        return maze;
    }

}
