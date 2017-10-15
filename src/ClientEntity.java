import java.util.HashMap;

public class ClientEntity {

    protected int id;
    protected int type;
    protected double health;
    protected int adjacentAttackers;
    protected boolean foe;

    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public ClientEntity(int id, int type, double health, int adjacentAttackers, boolean foe) {
        this.id = id;
        this.type = type;
        this.health = health;
        this.adjacentAttackers = adjacentAttackers;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
        this.foe = foe;
    }

    public int getId() {
        return this.id;
    }
    public int getType() {
        return this.type;
    }
    public boolean getFoe() { return this.foe; }

    public double getHealth() { return this.health; }
    public int getAdjacentAttackers() { return this.adjacentAttackers; }

}

