public class EntityStatus {
    public int x;
    public int y;
    public double health;
    public int score;
    public int pause;
    public int targetEntity;

    public EntityStatus(int x, int y, double health, int score, int pause, int targetEntity) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.score = score;
        this.pause = pause;
        this.targetEntity = targetEntity;
    }

    public EntityStatus(int x, int y, EntityStatus previous) {
        this.x = x;
        this.y = y;
        this.health = previous.health;
        this.score = previous.score;
        this.pause = previous.pause;
        this.targetEntity = previous.targetEntity;
    }

}
