public class Wanderer {

    private static int noOfClearDirections(boolean[] clearDirections) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (clearDirections[i]) count++;
        }
        return count;
    }

    public static void pickRandomDirection(boolean[] clearDirections, ServerEntity entity) {

        if (noOfClearDirections(clearDirections) == 0) {
            entity.dx = 0;
            entity.dy = 0;
            return;
        }

        int d;
        do {
            d = entity.rnd.nextInt(4);
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

    public static XY calculateNext(ServerEntity entity, int[][] vicinity) {

        boolean[] clearDirections = new boolean[4];
        clearDirections[0] = vicinity[GameServer.VICINITY_CENTRE][GameServer.VICINITY_CENTRE - 1] <= 0;
        clearDirections[1] = vicinity[GameServer.VICINITY_CENTRE + 1][GameServer.VICINITY_CENTRE] <= 0;
        clearDirections[2] = vicinity[GameServer.VICINITY_CENTRE][GameServer.VICINITY_CENTRE + 1] <= 0;
        clearDirections[3] = vicinity[GameServer.VICINITY_CENTRE - 1][GameServer.VICINITY_CENTRE] <= 0;
        int noOfClearDirections = noOfClearDirections(clearDirections);

        boolean[] clearDiagonals = new boolean[4];
        clearDiagonals[0] = clearDirections[0] && clearDirections[1] && vicinity[GameServer.VICINITY_CENTRE + 1][GameServer.VICINITY_CENTRE - 1] <= 0;
        clearDiagonals[1] = clearDirections[1] && clearDirections[2] && vicinity[GameServer.VICINITY_CENTRE + 1][GameServer.VICINITY_CENTRE + 1] <= 0;
        clearDiagonals[2] = clearDirections[2] && clearDirections[3] && vicinity[GameServer.VICINITY_CENTRE - 1][GameServer.VICINITY_CENTRE + 1] <= 0;
        clearDiagonals[3] = clearDirections[3] && clearDirections[0] && vicinity[GameServer.VICINITY_CENTRE - 1][GameServer.VICINITY_CENTRE - 1] <= 0;
        int noOfClearDiagonals = noOfClearDirections(clearDiagonals);

        int target_x = GameServer.VICINITY_CENTRE + entity.dx;
        int target_y = GameServer.VICINITY_CENTRE + entity.dy;

        if ((noOfClearDirections == 3 && noOfClearDiagonals < 3) || vicinity[target_x][target_y] > 0 || (entity.dx == 0 && entity.dy == 0)) {
            if (noOfClearDirections > 1) {
                if (entity.dy > 0) clearDirections[0] = false;
                if (entity.dx < 0) clearDirections[1] = false;
                if (entity.dy < 0) clearDirections[2] = false;
                if (entity.dx > 0) clearDirections[3] = false;
            }
            pickRandomDirection(clearDirections, entity);
        }

        return new XY(entity.dx, entity.dy);

    }
}
