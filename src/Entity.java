import java.util.HashMap;
import java.util.Random;

public class Entity {

    public Random randomiser;

    private int id;
    private int type;
    private int aiType;

    public HashMap<Long, Integer> xMap;
    public HashMap<Long, Integer> yMap;

    public int dx;
    public int dy;

    public Entity(int id, int type) {
        this.id = id;
        this.type = type;
        this.aiType = 0;
        this.xMap = new HashMap<>();
        this.yMap = new HashMap<>();
        this.dx = 0;
        this.dy = 0;
        this.randomiser = new Random(id);
    }

    public int getId() {
        return this.id;
    }
    public int getType() {
        return this.type;
    }
    public int getAIType() {
        return this.aiType;
    }
    public void setAiType(int aiType) {this.aiType = aiType; }

}

