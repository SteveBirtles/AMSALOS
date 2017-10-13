public class Wander {

    public static final int WANDER_SIZE = 17;
    public static final int WANDER_CENTRE = Math.floorDiv(WANDER_SIZE, 2);
    
    private static int noOfClearDirections(boolean[] clearDirections) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (clearDirections[i]) count++;
        }
        return count;
    }

    public static void pickRandomDirection(boolean[] clearDirections, Entity entity) {

        if (noOfClearDirections(clearDirections) == 0) {
            entity.dx = 0;
            entity.dy = 0;
            return;
        }

        int d;
        do {
            d = entity.randomiser.nextInt(4);
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

    public static XY calculateNext(Entity entity, int[][] vicinity) {

        int target_x = WANDER_CENTRE + entity.dx;
        int target_y = WANDER_CENTRE + entity.dy;

        boolean[] clearDirections = new boolean[4];
        clearDirections[0] = vicinity[WANDER_CENTRE][WANDER_CENTRE - 1] == 0;
        clearDirections[1] = vicinity[WANDER_CENTRE + 1][WANDER_CENTRE] == 0;
        clearDirections[2] = vicinity[WANDER_CENTRE][WANDER_CENTRE + 1] == 0;
        clearDirections[3] = vicinity[WANDER_CENTRE - 1][WANDER_CENTRE] == 0;
        int noOfClearDirections = noOfClearDirections(clearDirections);

        boolean[] clearDiagonals = new boolean[4];
        clearDiagonals[0] = clearDirections[0] && clearDirections[1] && vicinity[WANDER_CENTRE + 1][WANDER_CENTRE - 1] == 0;
        clearDiagonals[1] = clearDirections[1] && clearDirections[2] && vicinity[WANDER_CENTRE + 1][WANDER_CENTRE + 1] == 0;
        clearDiagonals[2] = clearDirections[2] && clearDirections[3] && vicinity[WANDER_CENTRE - 1][WANDER_CENTRE + 1] == 0;
        clearDiagonals[3] = clearDirections[3] && clearDirections[0] && vicinity[WANDER_CENTRE - 1][WANDER_CENTRE - 1] == 0;
        int noOfClearDiagonals = noOfClearDirections(clearDiagonals);

        if ((noOfClearDirections == 3 && noOfClearDiagonals < 3) || vicinity[target_x][target_y] != 0) {
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
