import java.util.HashMap;
import java.util.Random;

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

    public static int noOfClearDirections(boolean[] clearDirections) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (clearDirections[i]) count++;
        }
        return count;
    }

    public void pickRandomDirection(boolean[] clearDirections, Random rnd) {

        if (noOfClearDirections(clearDirections) == 0) {
            dx = 0;
            dy = 0;
            return;
        }

        int d;
        do {
            d = rnd.nextInt(4);
        } while (clearDirections[d] == false);

        switch (d) {
            case 0: // NORTH
                this.dx = 0;
                this.dy = -1;
                break;
            case 1: // EAST
                this.dx = 1;
                this.dy = 0;
                break;
            case 2: // SOUTH
                this.dx = 0;
                this.dy = 1;
                break;
            case 3: // WEST
                this.dx = -1;
                this.dy = 0;
                break;
        }
    }

}

