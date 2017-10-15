import org.eclipse.jetty.server.Server;

import java.util.ArrayList;
import java.util.Random;

public class ServerEntity extends ClientEntity {

    static int nextID = 1;

    public Random rnd;

    private int aiType;
    private double healthScale;

    public int dx;
    public int dy;
    public int tombstoneAge;
    public int adjacentFriends;
    public int adjacentFoes;

    public ServerEntity(int type, double healthScale, boolean foe) {
        super(nextID, type, 1, 0, foe);
        nextID++;
        this.aiType = 0;
        this.rnd = new Random(id);
        this.healthScale = healthScale;
        this.dx = 0;
        this.dy = 0;
    }

    public int getAIType() { return this.aiType; }
    public void setAiType(int aiType) { this.aiType = aiType; }

    public void changeHealth(double hitPoints) {
        health += hitPoints / healthScale;
        if (health < 0) health = 0;
    }

    public int[][] calculateVicinity(int currentX, int currentY, int[][] map, int[][] entityMap) {

        int[][] vicinity = new int[GameServer.VICINITY_SIZE][GameServer.VICINITY_SIZE];

        for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
            for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                int u = currentX - GameServer.VICINITY_CENTRE + i;
                int v = currentY - GameServer.VICINITY_CENTRE + j;
                if (u >= 0 && v >= 0 && u < GameServer.MAX_X && v < GameServer.MAX_Y) {
                    if (map[u][v] % 256 >= 128) {
                        vicinity[i][j] = 1;
                    } else if (entityMap[u][v] != 0 && entityMap[u][v] != getId()) {
                        if (entityMap[u][v] > 0) {
                            vicinity[i][j] = 2;
                        }
                        else {
                            vicinity[i][j] = 3;
                        }

                    }
                }
            }
        }
        return vicinity;
    }

    public void calculateAdjacentEntities(int[][] entityMap) {
        long last = 0;
        for (long l : xMap.keySet()) {
            if (l > last) last = l;
        }

        adjacentFriends = 0;
        adjacentFoes = 0;

        if (xMap.containsKey(last) && yMap.containsKey(last)) {

            int currentX = xMap.get(last);
            int currentY = yMap.get(last);

            if (currentX > 0 && entityMap[currentX - 1][currentY] != 0
                    && Math.abs(entityMap[currentX - 1][currentY]) != getId()) {
                if (entityMap[currentX - 1][currentY] > 0) {
                    adjacentFriends++;
                } else {
                    adjacentFoes++;
                }
            }

            if (currentY > 0 && entityMap[currentX][currentY - 1] != 0
                    && Math.abs(entityMap[currentX][currentY - 1]) != getId()) {
                if (entityMap[currentX][currentY - 1] > 0) {
                    adjacentFriends++;
                } else {
                    adjacentFoes++;
                }
            }

            if (currentX < GameServer.MAX_X - 1 && entityMap[currentX + 1][currentY] != 0
                    && Math.abs(entityMap[currentX + 1][currentY]) != getId()) {
                if (entityMap[currentX + 1][currentY] > 0) {
                    adjacentFriends++;
                } else {
                    adjacentFoes++;
                }
            }

            if (currentY < GameServer.MAX_Y - 1 && entityMap[currentX][currentY + 1] != 0
                    && Math.abs(entityMap[currentX][currentY + 1]) != getId()) {
                if (entityMap[currentX][currentY + 1] > 0) {
                    adjacentFriends++;
                } else {
                    adjacentFoes++;
                }
            }
        }

        adjacentAttackers = foe ? adjacentFriends : adjacentFoes;

    }



    public static int[][] generateEntityMap(ArrayList<ServerEntity> worldEntities) {

        int[][] entityMap = new int[GameServer.MAX_X][GameServer.MAX_Y];

        for (ClientEntity e: worldEntities) {

            if (e.getHealth() == 0) continue;

            long last = 0;
            for (long l: e.xMap.keySet()) {
                if (l > last) last = l;
            }

            if (e.xMap.containsKey(last) && e.yMap.containsKey(last)) {
                int currentX = e.xMap.get(last);
                int currentY = e.yMap.get(last);
                if (currentX >= 0 && currentY >= 0 && currentX < GameServer.MAX_X && currentY < GameServer.MAX_Y) {
                    entityMap[currentX][currentY] = (e.foe ? -1 : 1) * e.getId();
                }
            }

        }

        return entityMap;
    }


}
