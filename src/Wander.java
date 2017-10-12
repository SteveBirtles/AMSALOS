import java.util.Random;

public class Wander {

    private static int noOfClearDirections(boolean[] clearDirections) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (clearDirections[i]) count++;
        }
        return count;
    }

    public static void pickRandomDirection(boolean[] clearDirections, Entity entity, Random rnd) {

        if (noOfClearDirections(clearDirections) == 0) {
            entity.dx = 0;
            entity.dy = 0;
            return;
        }

        int d;
        do {
            d = rnd.nextInt(4);
        } while (clearDirections[d] == false);

        switch (d) {
            case 0: // NORTH
                entity.dx = 0;
                entity.dy = -1;
                break;
            case 1: // EAST
                entity.dx = 1;
                entity.dy = 0;
                break;
            case 2: // SOUTH
                entity.dx = 0;
                entity.dy = 1;
                break;
            case 3: // WEST
                entity.dx = -1;
                entity.dy = 0;
                break;
        }
    }

    public static XY calculateNext(int currentX, int currentY, Entity entity, int[][] map, Random rnd) {

        int target_x = currentX + entity.dx;
        int target_y = currentY + entity.dy;

        boolean[] clearDirections = new boolean[4];
        clearDirections[0] = currentY > 0 && map[currentX][currentY - 1] % 256 < 128;
        clearDirections[1] = currentX < GameServer.MAX_X - 1 && map[currentX + 1][currentY] % 256 < 128;
        clearDirections[2] = currentY < GameServer.MAX_Y - 1 && map[currentX][currentY + 1] % 256 < 128;
        clearDirections[3] = currentX > 0 && map[currentX - 1][currentY] % 256 < 128;
        int noOfClearDirections = noOfClearDirections(clearDirections);

        boolean[] clearDiagonals = new boolean[4];
        clearDiagonals[0] = clearDirections[0] && clearDirections[1] && map[currentX + 1][currentY - 1] % 256 < 128;
        clearDiagonals[1] = clearDirections[1] && clearDirections[2] && map[currentX + 1][currentY + 1] % 256 < 128;
        clearDiagonals[2] = clearDirections[2] && clearDirections[3] && map[currentX - 1][currentY + 1] % 256 < 128;
        clearDiagonals[3] = clearDirections[3] && clearDirections[0] && map[currentX - 1][currentY - 1] % 256 < 128;
        int noOfClearDiagonals = noOfClearDirections(clearDiagonals);

        if (target_x < 0 || target_y < 0
                || target_x >= GameServer.MAX_X || target_y >= GameServer.MAX_Y
                || (noOfClearDirections == 3 && noOfClearDiagonals < 3)
                || map[target_x][target_y] % 256 > 127) {
            if (noOfClearDirections > 1) {
                if (entity.dy > 0) clearDirections[0] = false;
                if (entity.dx < 0) clearDirections[1] = false;
                if (entity.dy < 0) clearDirections[2] = false;
                if (entity.dx > 0) clearDirections[3] = false;
            }
            pickRandomDirection(clearDirections, entity, rnd);
            return new XY(currentX + entity.dx, currentY + entity.dy);
        } else {
            return new XY(target_x, target_y);
        }

    }
}
