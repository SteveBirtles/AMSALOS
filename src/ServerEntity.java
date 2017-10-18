import java.util.ArrayList;
import java.util.Random;

public class ServerEntity extends ClientEntity {

    static int nextID = 1;

    public Random rnd;
    public int skill = -1;

    private int aiType;
    private double healthScale;

    public int dx;
    public int dy;
    public int tombstoneAge;
    public int adjacentFriends;
    public int adjacentFoes;

    public ServerEntity(int type, double healthScale, boolean foe) {
        super(nextID, type, foe);
        nextID++;
        this.aiType = 0;
        this.rnd = new Random(getId());
        this.healthScale = healthScale;
        this.dx = 0;
        this.dy = 0;
    }

    public void pickNextTarget(int[][] vicinity, int[][] entityMap, int currentX, int currentY) {

        double bestDistance = GameServer.VICINITY_SIZE;
        int endX = 0;
        int endY = 0;

        for (int i = 0; i < GameServer.VICINITY_SIZE; i++) {
            for (int j = 0; j < GameServer.VICINITY_SIZE; j++) {
                if (i == GameServer.VICINITY_CENTRE && j == GameServer.VICINITY_CENTRE) continue;
                if (vicinity[i][j] == 3) {
                    double distance = Math.abs(i - GameServer.VICINITY_CENTRE) + Math.abs(j - GameServer.VICINITY_CENTRE);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        endX = i;
                        endY = j;
                    }
                }
            }
        }

        long last = 0;
        for (long l : status.keySet()) {
            if (l > last) last = l;
        }

        if (bestDistance < GameServer.VICINITY_SIZE) {
            status.get(last).targetEntity = Math.abs(entityMap[currentX + endX - GameServer.VICINITY_CENTRE][currentY + endY - GameServer.VICINITY_CENTRE]);
        }
        else {
            status.get(last).targetEntity = 0;
        }

    }

    public int getAIType() { return this.aiType; }
    public void setAiType(int aiType) { this.aiType = aiType; }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public void changeHealth(double hitPoints) {
        long last = 0;
        for (long l : status.keySet()) {
            if (l > last) last = l;
        }
        status.get(last).health += hitPoints / healthScale;
        if (status.get(last).health < 0) status.get(last).health = 0;
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
                        if (entityMap[u][v] >= 128) {
                            vicinity[i][j] = 4;
                        }
                        else if (entityMap[u][v] > 0) {
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

    public ArrayList<Integer> listAdjacentEntities(ArrayList<ServerEntity> allEntities) {
        ArrayList<Integer> adjacentEntities = new ArrayList<>();
        long last = 0;
        for (long l : status.keySet()) {
            if (l > last) last = l;
        }
        if (status.containsKey(last)) {
            int thisX = status.get(last).x;
            int thisY = status.get(last).y;
            for (ServerEntity e : allEntities) {
                if (e.getId() == getId()) continue;
                if (e.status.containsKey(last)) {
                    int thatX = e.status.get(last).x;
                    int thatY = e.status.get(last).y;
                    if (Math.abs(thisX - thatX) + Math.abs(thisY - thatY) == 1) {
                        adjacentEntities.add(e.getId());
                    }
                }
            }
        }
        return adjacentEntities;
    }

    public void calculateAdjacentEntities(int[][] entityMap, long time) {

        adjacentFriends = 0;
        adjacentFoes = 0;

        if (status.containsKey(time)) {

            int currentX = status.get(time).x;
            int currentY = status.get(time).y;

            if (currentX > 0 && entityMap[currentX - 1][currentY] != 0
                    && Math.abs(entityMap[currentX - 1][currentY]) != getId()) {
                if (entityMap[currentX - 1][currentY] > 0) {
                    if (entityMap[currentX - 1][currentY] <= 128) adjacentFriends++;
                } else if (entityMap[currentX - 1][currentY] < 0) {
                    adjacentFoes++;
                }
            }

            if (currentY > 0 && entityMap[currentX][currentY - 1] != 0
                    && Math.abs(entityMap[currentX][currentY - 1]) != getId()) {
                if (entityMap[currentX][currentY - 1] > 0) {
                    if (entityMap[currentX][currentY - 1] <= 128) adjacentFriends++;
                } else if (entityMap[currentX][currentY - 1] < 0) {
                    adjacentFoes++;
                }
            }

            if (currentX < GameServer.MAX_X - 1 && entityMap[currentX + 1][currentY] != 0
                    && Math.abs(entityMap[currentX + 1][currentY]) != getId()) {
                if (entityMap[currentX + 1][currentY] > 0) {
                    if (entityMap[currentX + 1][currentY] <= 128) adjacentFriends++;
                } else if (entityMap[currentX + 1][currentY] < 0) {
                    adjacentFoes++;
                }
            }

            if (currentY < GameServer.MAX_Y - 1 && entityMap[currentX][currentY + 1] != 0
                    && Math.abs(entityMap[currentX][currentY + 1]) != getId()) {
                if (entityMap[currentX][currentY + 1] > 0) {
                    if (entityMap[currentX][currentY + 1] <= 128) adjacentFriends++;
                } else if (entityMap[currentX][currentY + 1] < 0) {
                    adjacentFoes++;
                }
            }
        }

        status.get(time).adjacentAttackers = getFoe() ? adjacentFriends : adjacentFoes;

    }



    public static int[][] generateEntityMap(ArrayList<ServerEntity> worldEntities, boolean ignorePowerups) {

        int[][] entityMap = new int[GameServer.MAX_X][GameServer.MAX_Y];

        for (ClientEntity e: worldEntities) {

            if (ignorePowerups && e.getType() > 128) continue;

            long last = 0;
            for (long l: e.status.keySet()) {
                if (l > last) last = l;
            }

            if (e.status.get(last).health <= 0) continue;

            if (e.status.containsKey(last)) {
                int currentX = e.status.get(last).x;
                int currentY = e.status.get(last).y;
                if (currentX >= 0 && currentY >= 0 && currentX < GameServer.MAX_X && currentY < GameServer.MAX_Y) {
                    if (entityMap[currentX][currentY] != 0) {
                        System.out.println("Entity collision error (" + currentX + ", " + currentY + ") @" + last + ": Entities " + e.getId() + " and " + entityMap[currentX][currentY] + ".");
                    } else {
                        entityMap[currentX][currentY] = (e.getFoe() ? -1 : 1) * e.getId();
                    }
                }
            }

        }

        return entityMap;
    }


}
