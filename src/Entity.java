import java.util.HashMap;

public class Entity {
    private int id;
    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public Entity(int id) {
        this.id = id;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }

}
