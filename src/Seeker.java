import java.util.ArrayList;
import java.util.HashMap;

public class Seeker {

    public static XY calculateNext(ServerEntity entity, int[][] vicinity, int[][] entityMap, int currentX, int currentY) {

        entity.dx = 0;
        entity.dy = 0;

        double bestDistance = GameServer.VICINITY_SIZE;
        int endX = 0;
        int endY = 0;
        boolean success = false;

        for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
            for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                if ((entity.targetEntity > 0 && vicinity[i][j] == 4) || vicinity[i][j] == 3) {
                    double distance = Math.sqrt(Math.pow(i - GameServer.VICINITY_CENTRE, 2)
                            + Math.pow(j - GameServer.VICINITY_CENTRE, 2));
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        endX = i;
                        endY = j;
                    }
                }
            }
        }

        if (bestDistance < GameServer.VICINITY_SIZE) {

            if (vicinity[endX][endY] == 3) {
                entity.targetEntity = Math.abs(entityMap[currentX + endX - GameServer.VICINITY_CENTRE][currentY + endY - GameServer.VICINITY_CENTRE]);
                System.out.println("Target: " + entity.getId() + " -> " + entity.targetEntity);
            }

            int INFINITY = 999999999;

            int startX = GameServer.VICINITY_CENTRE;
            int startY = GameServer.VICINITY_CENTRE;

            //System.out.println(startX + ", " + startY + " -> " + endX + ", " + endY);

            ArrayList<XY> closedSet = new ArrayList<>();
            ArrayList<XY> openSet = new ArrayList<>();
            HashMap<XY, XY> cameFrom = new HashMap<>();

            XY[][] node = new XY[GameServer.VICINITY_SIZE][GameServer.VICINITY_SIZE];
            for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
                for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                    node[i][j] = new XY(i, j);
                }
            }
            openSet.add(node[startX][startY]);

            int[][] gScore = new int[GameServer.VICINITY_SIZE][GameServer.VICINITY_SIZE];
            for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
                for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                    gScore[i][j] = INFINITY;
                }
            }
            gScore[startX][startY] = 0;

            int[][] fScore = new int[GameServer.VICINITY_SIZE][GameServer.VICINITY_SIZE];
            for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
                for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                    fScore[i][j] = INFINITY;
                }
            }
            fScore[startX][startY] = Math.abs(startX - endX) + Math.abs(startY - endY);

            XY current = null;
            while (openSet.size() > 0) {

                int bestFScore = INFINITY;
                for (XY openNode : openSet) {
                    if (fScore[openNode.x][openNode.y] < bestFScore) {
                        bestFScore = fScore[openNode.x][openNode.y];
                        current = openNode;
                    }
                }

                if (current.x == endX && current.y == endY) {

                    ArrayList<XY> path = new ArrayList<>();

                    //System.out.println("CameFrom: " + cameFrom.size());

                    XY last = node[startX][startY];
                    path.add(current);
                    while (cameFrom.containsKey(current)) {
                        last = current;
                        current = cameFrom.get(current);
                        path.add(current);
                        //System.out.println("  ... " + current.x + ", " + current.y);
                    }

                    entity.dx = last.x - startX;
                    entity.dy = last.y - startY;

                    //System.out.println("Found path: " + entity.dx + ", " + entity.dy);

                    success = true;
                    break;
                }

                openSet.remove(current);
                closedSet.add(current);

                for (int direction = 1; direction <= 4; direction++) {

                    int neighbourX = current.x;
                    int neighbourY = current.y;
                    switch (direction) {
                        case 1:
                            if (current.x == 0) continue;
                            neighbourX--;
                            break;
                        case 2:
                            if (current.y == 0) continue;
                            neighbourY--;
                            break;
                        case 3:
                            if (current.x == GameServer.VICINITY_SIZE-1) continue;
                            neighbourX++;
                            break;
                        case 4:
                            if (current.y == GameServer.VICINITY_SIZE-1) continue;
                            neighbourY++;
                            break;
                    }

                    if (vicinity[neighbourX][neighbourY] != 0) continue;

                    XY neighbour = node[neighbourX][neighbourY];

                    if (closedSet.contains(neighbour)) continue;

                    if (!openSet.contains(neighbour)) openSet.add(neighbour);

                    int newGScore = gScore[current.x][current.y] + 1;
                    if (newGScore >= gScore[neighbour.x][neighbour.y]) continue;

                    cameFrom.put(neighbour, current);
                    gScore[neighbour.x][neighbour.y] = newGScore;
                    fScore[neighbour.x][neighbour.y] = newGScore +
                            Math.abs(neighbour.x - endX) + Math.abs(neighbour.y - endY);

                }

            }

        }

        if (!success) {
            entity.dx = 0;
            entity.dy = 0;
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
