import java.util.HashMap;

public class Entity {
    private int id;
    private int type;

    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public int dx;
    public int dy;

    public Entity(int id, int type) {
        this.id = id;
        this.type = type;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
        this.dx = 0;
        this.dy = 0;
    }

    public int getId() {
        return this.id;
    }
    public int getType() {
        return this.type;
    }

}
