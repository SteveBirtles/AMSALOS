public class EntityStatus {
    public int x;
    public int y;
    public double health;
    public int kills;
    public int pause;
    public int targetEntity;
    public int adjacentAttackers;

    public EntityStatus(int x, int y, double health, int kills, int pause, int targetEntity, int adjacentAttackers) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.kills = kills;
        this.pause = pause;
        this.targetEntity = targetEntity;
        this.adjacentAttackers = adjacentAttackers;
    }

    public EntityStatus(int x, int y, EntityStatus previous) {
        this.x = x;
        this.y = y;
        this.health = previous.health;
        this.kills = previous.kills;
        this.pause = previous.pause;
        this.targetEntity = previous.targetEntity;
        this.adjacentAttackers = previous.adjacentAttackers;
    }

}
