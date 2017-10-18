import java.util.HashMap;

public class ClientEntity {

    int id;
    int type;
    boolean foe;
    private String name;

    public HashMap<Long, EntityStatus> status;

    public ClientEntity(int id, int type, boolean foe) {
        this.id = id;
        this.type = type;
        this.foe = foe;
        this.name = "";
        this.status = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }
    public int getType() {
        return this.type;
    }
    public boolean getFoe() { return this.foe; }
    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }

}

