import java.util.HashMap;

public class ClientEntity {

    protected int id;
    protected int type;
    protected double health;
    protected int adjacentAttackers;
    protected boolean foe;

    protected String name;
    protected int kills;

    public int targetEntity;
    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public ClientEntity(int id, int type, double health, int adjacentAttackers, boolean foe, int target) {
        this.id = id;
        this.type = type;
        this.health = health;
        this.adjacentAttackers = adjacentAttackers;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
        this.foe = foe;
        this.targetEntity = target;
        this.kills = 0;
        this.name = "";
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

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }

    public void setKills(int kills) { this.kills = kills; }
    public int getKills() { return this.kills; }

}

