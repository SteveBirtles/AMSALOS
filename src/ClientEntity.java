import java.util.HashMap;

public class ClientEntity {

    protected int id;
    protected int type;
    protected double health;
    protected int adjacentEnemies;

    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public ClientEntity(int id, int type, double health, int adjacentEnemies) {
        this.id = id;
        this.type = type;
        this.health = health;
        this.adjacentEnemies = adjacentEnemies;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }
    public int getType() {
        return this.type;
    }

    public double getHealth() { return this.health; }
    public int getAdjacentEnemies() { return this.adjacentEnemies; }

}

