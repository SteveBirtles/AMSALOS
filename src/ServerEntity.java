import java.util.Random;

public class ServerEntity extends ClientEntity {

    static int nextID = 1;

    public Random randomiser;

    private int aiType;
    private double healthScale;

    public int dx;
    public int dy;
    public int adjacentEnemies;
    public int tombstoneAge;

    public ServerEntity(int type, double healthScale) {
        super(nextID, type, 1);
        nextID++;
        this.aiType = 0;
        this.randomiser = new Random(id);
        this.healthScale = healthScale;
        this.dx = 0;
        this.dy = 0;
    }

    public int getAIType() { return this.aiType; }
    public void setAiType(int aiType) { this.aiType = aiType; }

    public void setHealth(double health) {
        this.health = health;
    }

    public void changeHealth(double hitPoints) {
        health += hitPoints / healthScale;
        if (health < 0) health = 0;
    }

    public void calculateAdjacentEnemies(int[][] entityMap) {
        long last = 0;
        for (long l : xMap.keySet()) {
            if (l > last) last = l;
        }

        if (xMap.containsKey(last) && yMap.containsKey(last)) {

            int currentX = xMap.get(last);
            int currentY = yMap.get(last);

            adjacentEnemies = 0;

            if (currentX > 0 && entityMap[currentX - 1][currentY] > 0
                    && entityMap[currentX - 1][currentY] != getId()) adjacentEnemies++;

            if (currentY > 0 && entityMap[currentX][currentY - 1] > 0
                    && entityMap[currentX][currentY - 1] != getId()) adjacentEnemies++;

            if (currentX < GameServer.MAX_X - 1 && entityMap[currentX + 1][currentY] > 0
                    && entityMap[currentX + 1][currentY] != getId()) adjacentEnemies++;

            if (currentY < GameServer.MAX_Y - 1 && entityMap[currentX][currentY + 1] > 0
                    && entityMap[currentX][currentY + 1] != getId()) adjacentEnemies++;

        }
    }

}
