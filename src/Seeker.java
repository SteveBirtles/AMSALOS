import org.omg.IOP.ENCODING_CDR_ENCAPS;

public class Seeker {

    public static XY calculateNext(ServerEntity entity, int[][] vicinity) {

        entity.dx = 0;
        entity.dy = 0;

        boolean[] clearDirections = new boolean[4];
        clearDirections[0] = vicinity[GameServer.VICINITY_CENTRE][GameServer.VICINITY_CENTRE - 1] == 0;
        clearDirections[1] = vicinity[GameServer.VICINITY_CENTRE + 1][GameServer.VICINITY_CENTRE] == 0;
        clearDirections[2] = vicinity[GameServer.VICINITY_CENTRE][GameServer.VICINITY_CENTRE + 1] == 0;
        clearDirections[3] = vicinity[GameServer.VICINITY_CENTRE - 1][GameServer.VICINITY_CENTRE] == 0;

        double bestDistance = GameServer.VICINITY_SIZE;
        int bestX = 0;
        int bestY = 0;

        for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
            for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                if (vicinity[i][j] == 3) {
                    double distance = Math.sqrt(Math.pow(i - GameServer.VICINITY_CENTRE, 2)
                            + Math.pow(j - GameServer.VICINITY_CENTRE, 2));
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestX = i;
                        bestY = j;
                    }
                }
            }
        }

        if (bestDistance < GameServer.VICINITY_SIZE) {
            int deltaX = bestX - GameServer.VICINITY_CENTRE;
            int deltaY = bestY - GameServer.VICINITY_CENTRE;
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                entity.dx = deltaX < 0 ? -1 : 1;
            } else {
                entity.dy = deltaY < 0 ? -1 : 1;
            }
        }
        else {
            return Wanderer.calculateNext(entity, vicinity);
        }

        int target_x = GameServer.VICINITY_CENTRE + entity.dx;
        int target_y = GameServer.VICINITY_CENTRE + entity.dy;

        if (vicinity[target_x][target_y] != 0) {
            entity.dx = 0;
            entity.dy = 0;
        }

        return new XY(entity.dx, entity.dy);

    }

}
