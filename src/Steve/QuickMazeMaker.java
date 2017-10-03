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
        
        int width = maze.length;
        int height = maze[0].length;
        
        for (int p = 0; p < width; p++) {
            for (int q = 0; q < height; q++) {
                if (maze[p][q] == 128) {

                    if (p == 0) {
                        maze[p][q] = (maze[p+1][q] >= 128) ? 143 : 135;
                        continue;
                    }
                    if (q == 0) {
                        maze[p][q] = (maze[p][q+1] >= 128) ? 143 : 141;
                        continue;
                    }
                    if (p == width-1) {
                        maze[p][q] = (maze[p-1][q] >= 128) ? 143 : 139;
                        continue;
                    }
                    if (q == height-1) {
                        maze[p][q] = (maze[p][q-1] >= 128) ? 143 : 142;
                        continue;
                    }

                    boolean north = maze[p][q-1] >= 128;
                    boolean south = maze[p][q+1] >= 128;
                    boolean east = maze[p+1][q] >= 128;
                    boolean west = maze[p-1][q] >= 128;

                    if (north && !south && !east && !west) maze[p][q] = 129;
                    else if (!north && south && !east && !west) maze[p][q] = 130;
                    else if (north && south && !east && !west) maze[p][q] = 131;
                    else if (!north && !south && !east && west) maze[p][q] = 132;
                    else if (north && !south && !east && west) maze[p][q] = 133;
                    else if (!north && south && !east && west) maze[p][q] = 134;
                    else if (north && south && !east && west) maze[p][q] = 135;
                    else if (!north && !south && east && !west) maze[p][q] = 136;
                    else if (north && !south && east && !west) maze[p][q] = 137;
                    else if (!north && south && east && !west) maze[p][q] = 138;
                    else if (north && south && east && !west) maze[p][q] = 139;
                    else if (!north && !south && east && west) maze[p][q] = 140;
                    else if (north && !south && east && west) maze[p][q] = 141;
                    else if (!north && south && east && west) maze[p][q] = 142;
                    else if (north && south && east && west) maze[p][q] = 143;
                }
            }
        }

        return maze;

    }

}
