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
                maze[x][y] = 128;
                x += dx;
                y += dy;
                if (x < 0 || y < 0 || x >= width || y >= height) break;
            }
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
    
    public static int[][] fixEdges(int[][] maze) {

        Random rnd = new Random();

        int width = maze.length;
        int height = maze[0].length;
        
        for (int p = 0; p < width; p++) {
            for (int q = 0; q < height; q++) {
                if (maze[p][q] >= 128) {

                    int quarter1 = 0;
                    int quarter2 = 0;
                    int quarter3 = 0;
                    int quarter4 = 0;

                    /*if (p == width-1 && q == height-1) quarter1 = 2;
                    else if (p == 0 && q == height-1) quarter2 = 2;
                    else if (p == width-1 && q == 0) quarter3 = 2;
                    else if (p == 0 && q == 0) quarter4 = 2;
                    else if (p == 0) { quarter2 = 4; quarter4 = 4; }
                    else if (q == 0) { quarter3 = 3; quarter4 = 3; }
                    else if (p == width-1) { quarter1 = 4; quarter3 = 4; }
                    else if (q == height-1) { quarter1 = 3; quarter2 = 3; }
                    else {*/

                    boolean north   = q > 0         && maze[p][q - 1] >= 128;
                    boolean south   = q < height-1  && maze[p][q + 1] >= 128;
                    boolean east    = p < width-1   && maze[p + 1][q] >= 128;
                    boolean west    = p > 0         && maze[p - 1][q] >= 128;

                    boolean north_east = q > 0          && p < width-1  && maze[p + 1][q - 1] >= 128;
                    boolean north_west = q > 0          && p > 0        && maze[p - 1][q - 1] >= 128;
                    boolean south_east = q < height-1   && p < width-1  && maze[p + 1][q + 1] >= 128;
                    boolean south_west = q < height-1   && p > 0        && maze[p - 1][q + 1] >= 128;

                    if (!north && !west)                quarter1 = 1;
                    if (!north_west && north && west)   quarter1 = 2;
                    if (!north && west)                 quarter1 = 3;
                    if (north && !west)                 quarter1 = 4;

                    if (!north && !east)                quarter2 = 1;
                    if (!north_east && north && east)   quarter2 = 2;
                    if (!north && east)                 quarter2 = 3;
                    if (north && !east)                 quarter2 = 4;

                    if (!south && !west)                quarter3 = 1;
                    if (!south_west && south && west)   quarter3 = 2;
                    if (!south && west)                 quarter3 = 3;
                    if (south && !west)                 quarter3 = 4;

                    if (!south && !east)                quarter4 = 1;
                    if (!south_east && south && east)   quarter4 = 2;
                    if (!south && east)                 quarter4 = 3;
                    if (south && !east)                 quarter4 = 4;

                    maze[p][q] += 256 * quarter1;
                    maze[p][q] += 256 * 16 * quarter2;
                    maze[p][q] += 256 * 256 * quarter3;
                    maze[p][q] += 256 * 4096 * quarter4;

                }
            }
        }

        return maze;

    }

}
