package Steve;

import java.util.Random;

public class QuickMazeMaker {

    public static int[][] makeMake(int width, int height) {

        Random rnd = new Random();

        int[][] maze = emptyMap(width, height);

        for (int k = 0; k < 250; k++) {
            int l = rnd.nextInt(5) + 5;
            int dx = rnd.nextInt(3) - 1;
            int dy = (dx == 0) ? (rnd.nextInt(2) == 0 ? 1 : -1) : 0;
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            for (int z = 0; z < l; z++) {
                //System.out.print("(" + x + ", " + y + "), ");
                maze[x][y] = 128;
                x += dx;
                y += dy;
                if (x < 0 || y < 0 || x >= width || y >= height) break;
            }
            //System.out.println();
        }

        for (int x = 0; x < width; x++) {
            maze[x][0] = 129;
            maze[x][height-1] = 129;
        }
        for (int y = 0; y < height; y++) {
            maze[0][y] = 129;
            maze[width-1][y] = 129;
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
