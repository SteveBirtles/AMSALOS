import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GameServer extends AbstractHandler {

    public final static boolean debug = false;
    public final static boolean autoAddPlayers = false;

    public final static ArrayList<ServerEntity> worldEntities = new ArrayList<>();
    public final static int MAX_X = 401;
    public final static int MAX_Y = 17;
    public final static int SCREEN_COUNT = 20;
    public final static int SCREEN_WIDTH = 20;
    public final static int TOMBSTONE_LIFETIME = 40;
    public final static int VICINITY_SIZE = 17;
    public final static int VICINITY_CENTRE = 8;

    public final static int MAX_POWERUPS = 200;
    public final static int MAX_ENEMIES = 200;
    public final static int MAX_PLAYERS = 200;

    public long mapTimeStamp;

    private int[][] map = null;
    private final String[] encodedMap = new String[SCREEN_COUNT];
    private String fullEncodedMap = "";

    public class EntitySpawner extends TimerTask {

        public void run() {

            int enemyCount = 0;
            int powerUpCount = 0;
            int playerCount = 0;
            int tombstoneCount = 0;

            synchronized (worldEntities) {
                for (ServerEntity e: worldEntities) {
                    long last = 0;
                    for (long l: e.status.keySet()) {
                        if (l > last) last = l;
                    }
                    if (e.getType() > 128) {
                        powerUpCount++;
                    } else if (e.status.get(last).health <= 0) {
                        tombstoneCount++;
                    } else if (e.getType() < 16) {
                        playerCount++;
                    } else {
                        enemyCount++;
                    }
                }
            }

            Random rnd = new Random();
            int screen = rnd.nextInt(20) + 1;
            int goodOrBad = rnd.nextInt(2);

            switch (goodOrBad) {
                case 0:

                    if (autoAddPlayers && rnd.nextInt(10) == 0) {
                        if (playerCount < MAX_PLAYERS) {
                            ServerEntity e = createEntity(screen, rnd.nextInt(4) + 1, 4);
                            e.setName(Steve.QuickNameMaker.next(rnd));
                        }
                    }
                    else {
                        if (powerUpCount < MAX_POWERUPS) {
                            createEntity(screen, rnd.nextInt(9) + 129, 0);
                        }
                    }
                    break;
                case 1:
                    if (enemyCount < MAX_ENEMIES) {
                        createEntity(screen, rnd.nextInt(12) + 17, 3);
                    }
                    break;
            }

        }

    }

    public class EntityUpdater extends TimerTask {

        public void run() {
            ArrayList<ServerEntity> expired = new ArrayList<>();
            long present = System.currentTimeMillis() >> 8;

            Random rnd = new Random(present);

            long future = present + 4;
            long past = present - 2;

            synchronized (worldEntities) {

                int entityMap[][] = ServerEntity.generateCollisionMap(worldEntities, true);
                int treasureMap[][] = ServerEntity.generateTreasureMap(worldEntities);

                for (ServerEntity e : worldEntities) {

                    long first = future;
                    long last = 0;
                    for (long l : e.status.keySet()) {
                        if (l > last) last = l;
                        if (l < first) first = l;
                    }

                    //System.out.println(e.getId() + " : " + (future - last));

                    if (future != last) {

                        int currentX = e.status.get(last).x;
                        int currentY = e.status.get(last).y;

                        if (e.status.get(last).health == 0) {

                            if (e.getFoe()) e.tombstoneAge++;
                            e.status.put(future, new EntityStatus(currentX, currentY, e.status.get(last)));

                        } else if (e.adjacentAttackers > 0 && !(e.getSkill() == 3)) {

                            e.status.put(future, new EntityStatus(currentX, currentY, e.status.get(last)));
                            e.status.get(future).pause = 0;

                        } else {

                            int[][] vicinity = null;
                            XY target = null;
                            if (e.getAIType() > 2) {
                                vicinity = e.calculateVicinity(currentX, currentY, map, entityMap, treasureMap);
                            }

                            switch (e.getAIType()) {
                                case 0:

                                    e.dx = 0;
                                    e.dy = 0;

                                    e.status.put(future, new EntityStatus(currentX, currentY, e.status.get(last)));
                                    break;

                                case 1:

                                    e.dy = 0;
                                    if (e.dx == 0) e.dx = rnd.nextInt(2) == 0 ? -1 : 1;
                                    int newX = currentX + e.dx;
                                    if (newX < 0 || newX >= MAX_X || map[newX][currentY] % 256 >= 128
                                            || (entityMap[newX][currentY] != 0 && entityMap[newX][currentY] != e.getId())) {
                                        e.dx = -e.dx;
                                        newX = currentX;
                                    }

                                    e.status.put(future, new EntityStatus(newX, currentY, e.status.get(last)));

                                    break;

                                case 2:

                                    e.dx = 0;
                                    if (e.dy == 0) e.dy = rnd.nextInt(2) == 0 ? -1 : 1;
                                    int newY = currentY + e.dy;
                                    if (newY < 0 || newY >= MAX_Y || map[currentX][newY] % 256 >= 128
                                            || (entityMap[currentX][newY] != 0 && entityMap[currentX][newY] != e.getId())) {
                                        e.dy = -e.dy;
                                        newY = currentY;
                                    }

                                    e.status.put(future, new EntityStatus(currentX, newY, e.status.get(last)));

                                    break;

                                case 3:

                                    if (e.status.get(last).pause == 1) {
                                        e.status.get(last).pause = 2;
                                    } else {
                                        e.status.get(last).pause = 1;
                                    }

                                    if (e.status.get(last).pause == 1) {
                                        target = Wanderer.calculateNext(e, vicinity);
                                        target.x += currentX;
                                        target.y += currentY;
                                    } else {
                                        target = new XY(currentX, currentY);
                                    }

                                    break;

                                case 4:

                                    e.status.get(last).pause = 0;

                                    if (e.status.get(last).targetEntity == 0 || e.getSkill() == 2) {
                                        if (e.getSkill() != 3 || (e.getSkill() == 3 && e.status.get(last).health > 0.33)) {
                                            e.pickNextTarget(vicinity, entityMap, currentX, currentY);
                                        }
                                    }

                                    if (e.status.get(last).targetEntity != 0) {

                                        XY targetEntity = null;
                                        for (int x = 0; x < MAX_X; x++) {
                                            for (int y = 0; y < MAX_Y; y++) {
                                                if (Math.abs(entityMap[x][y]) == e.status.get(last).targetEntity) {
                                                    targetEntity = new XY(x - currentX, y - currentY);
                                                    break;
                                                }
                                            }
                                        }

                                        if (targetEntity != null) {

                                            target = Seeker.calculateNext(e, vicinity, targetEntity.x, targetEntity.y);

                                        } else {
                                            e.status.get(last).targetEntity = 0;
                                        }
                                    }

                                    if (e.status.get(last).targetEntity == 0) {
                                        target = Wanderer.calculateNext(e, vicinity);
                                    }

                                    target.x += currentX;
                                    target.y += currentY;

                                    break;
                            }

                            if (e.getAIType() == 3 || e.getAIType() == 4) {
                                if (target.x >= 0 && target.y >= 0 && target.x < MAX_X && target.y < MAX_Y) {
                                    if (entityMap[target.x][target.y] != 0) {
                                        e.status.put(future, new EntityStatus(currentX, currentY, e.status.get(last)));
                                        e.status.get(future).pause = 0;
                                    } else {
                                        e.status.put(future, new EntityStatus(target.x, target.y, e.status.get(last)));
                                        entityMap[target.x][target.y] = (e.getFoe() ? -1 : 1) * e.getId();
                                    }
                                }
                            }

                        }

                        if (first <= past) {

                            e.status.remove(first);

                        }
                    }
                }

                for (ServerEntity e : worldEntities) {

                    if (e.getType() >= 128) continue;

                    if (e.tombstoneAge > 40) {
                        expired.add(e);
                        continue;
                    }

                    long last = 0;
                    for (long l : e.status.keySet()) {
                        if (l > last) last = l;
                    }

                    if (e.status.get(last).health > 0) {
                        for (ServerEntity e2 : worldEntities) {

                            if (e.getId() == e2.getId()) continue;

                            if (e.getType() <= 16 && e2.getType() > 128) {
                                long last2 = 0;
                                for (long l : e.status.keySet()) {
                                    if (l > last2) last2 = l;
                                }

                                if (e.status.keySet().contains(last) && e2.status.keySet().contains(last2)) {
                                    if (e.status.get(last).x == e2.status.get(last2).x && e.status.get(last).y == e2.status.get(last2).y) {
                                        expired.add(e2);
                                        e.status.get(last).score += 10;
                                    }
                                }
                            }

                        }

                        if (e.status.get(last).targetEntity > 0) {
                            for (ServerEntity e2 : worldEntities) {

                                if (e2.getId() == e.status.get(last).targetEntity) {
                                    if (e.status.get(last).health <= 0) {
                                        e.status.get(last).targetEntity = 0;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (expired.size() > 0) {
                    worldEntities.removeAll(expired);
                }

                //int[][] attackMap = ServerEntity.generateAttackMap(worldEntities, true, present, future);
                int[][] attackMap = ServerEntity.generateCollisionMap(worldEntities, true);

                for (ServerEntity e : worldEntities) {

                    if (e.getType() >= 128) continue;

                    long time = future;

                    if (!e.status.containsKey(time)) continue;

                    if (e.status.get(time).health > 0) {
                        e.calculateAdjacentEntities(attackMap);

                        if (e.getFoe()) {
                            if (e.adjacentAttackers > 0) System.out.println("KILL");
                            e.changeHealth(-e.adjacentAttackers);
                        } else {
                            //System.out.println("Hurting friend: " + (-e.adjacentAttackers));
                            e.changeHealth(-e.adjacentAttackers);
                        }

                        if (e.status.get(time).health <= 0) {
                            e.status.get(time).pause = 0;
                            ArrayList<Integer> attackers = e.listAdjacentEntities(worldEntities);
                            for (ServerEntity e2 : worldEntities) {
                                if (attackers.contains(e2.getId())) {
                                    e2.status.get(time).score += 100;
                                }
                            }
                        }

                    }
                }

            }

        }

    }

    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {

        String[] ip = request.getRemoteAddr().split("\\.");

        boolean sendMap = false;
        boolean isPlayer = false;

        int position = 1;
        int addEntity = -1;
        int resetAll = -1;
        int aiType = 0;
        int skill = -1;
        String name = "";

        if (ip[0].equals("172") && ip[1].equals("16") && ip[2].equals("41")) {
            position = Integer.parseInt(ip[3]);
        }

        long time = System.currentTimeMillis() >> 8;

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        if (request.getRequestURI().equals("/favicon.ico")) return; // SKIP FAVICON REQUESTS;

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date dateobj = new Date();

        String requestText = "[ " + request.getRemoteAddr() + "  |  " + df.format(dateobj) + "  |  ";
        requestText += request.getMethod() + " ] \t " + request.getRequestURI() + " \t ";

        String method = request.getMethod().toUpperCase();

        if (request.getQueryString() != null) {

            System.out.println(request.getQueryString());

            for (String q : request.getQueryString().split("&")) {
                if (q.contains("=")) {

                    String variable = q.split("=")[0];
                    String value = q.split("=")[1];
                    requestText += "   " + variable + " = " + value;

                    if (method.equals("GET")) {

                        if (variable.equals("mapTimeStamp")) {
                            if (Long.parseLong(value) != mapTimeStamp) sendMap = true;
                        }

                        if (variable.equals("map") && !sendMap) sendMap = value.toLowerCase().equals("true");

                        if (variable.equals("screen")) {
                            int screen = Integer.parseInt(value);
                            if (screen != 0) {
                                position = screen;
                            }
                        }

                        if (variable.equals("player")) {
                            isPlayer = Boolean.parseBoolean(value);
                        }

                    }
                    else if (method.equals("POST")) {

                        if (variable.equals("add")) {
                            addEntity = Integer.parseInt(value);
                        }
                        else if (variable.equals("screen")) {
                            position = Integer.parseInt(value);
                        }
                        else if (variable.equals("reset")) {
                            resetAll = Integer.parseInt(value);
                        }
                        else if (variable.equals("aitype")) {
                            aiType = Integer.parseInt(value);
                        }
                        else if (variable.equals("skill")) {
                            skill = Integer.parseInt(value);
                        }
                        else if (variable.equals("name")) {
                            name = value.replace("_", " ");
                        }

                    }

                } else {
                    requestText += "   Invalid query string component (" + q + ")";
                }
            }

        } else {
            requestText += "   No query string supplied";
        }
        if (debug) System.out.println(requestText);

        if (method.equals("POST")) {

            if ( addEntity != -1) {
                ServerEntity e = createEntity(position, addEntity, aiType);
                e.setName(name);
                e.setSkill(skill);
            }
            else if (resetAll != -1) {
                long t = System.currentTimeMillis() >> 8;
                if (t - mapTimeStamp > 8) {
                    worldEntities.clear();
                    createMap(resetAll);
                }
            }

            response.getWriter().println("OK");

        }
        else if (method.equals("GET") && position != -1) {

            ArrayList<JSONObject> frames = new ArrayList<>();
            for (long t = time - 1; t < time + 4; t++) {

                ArrayList<JSONObject> entities = new ArrayList<>();

                synchronized (worldEntities) {
                    for (ServerEntity e : worldEntities) {
                        if (e.status.containsKey(t)) {
                            int x = e.status.get(t).x;
                            int y = e.status.get(t).y;
                            if (isPlayer || (x >= (position-1)*SCREEN_WIDTH - 2 && x <= position*SCREEN_WIDTH + 2)) {
                                JSONObject entity = new JSONObject();
                                entity.put("i", e.getId());
                                entity.put("t", e.getType());
                                if (e.status.get(t).health > 0) {
                                    entity.put("h", e.status.get(t).health);
                                }
                                else {
                                    entity.put("h", (double) (-e.tombstoneAge) / (TOMBSTONE_LIFETIME) );
                                }
                                entity.put("x", x);
                                entity.put("y", y);
                                entity.put("f", Boolean.toString(e.getFoe()));
                                entity.put("z", Math.abs(e.status.get(t).targetEntity));

                                entity.put("n", e.getName());
                                entity.put("s", e.status.get(t).score);
                                entity.put("p", e.status.get(t).pause);

                                entities.add(entity);
                            }
                        }
                    }
                }

                JSONObject frame = new JSONObject();
                frame.put("time", t);
                frame.put("position", position);
                frame.put("entities", entities);

                frames.add(frame);

            }

            JSONObject finaljson = new JSONObject();
            finaljson.put("frames", frames);

            if (sendMap) {
                if (isPlayer) {
                    finaljson.put("map", fullEncodedMap);
                }
                else {
                    finaljson.put("map", encodedMap[position - 1]);
                }

                finaljson.put("mapTimeStamp", mapTimeStamp);
            }


            String text = finaljson.toString();

            response.getWriter().println(text);
            if (debug) System.out.println(text);
        }
        else {
            response.getWriter().println("Error!");
        }

        baseRequest.setHandled(true);

    }

    public ServerEntity createEntity(int screenNo, int type, int aiType) {

        ServerEntity newE;

        long t = System.currentTimeMillis() >> 8;

        Random rnd = new Random(t);

        synchronized (worldEntities) {

            int entityMap[][] = ServerEntity.generateCollisionMap(worldEntities, false);

            boolean foe = (type >= 17 && type <= 32);

            int healthBias = foe ? 10 : 250;

            newE = new ServerEntity(type, healthBias, foe);
            newE.setAiType(aiType);

            int x, y, attempts = 0;
            do {
                attempts++;
                if (screenNo == 0) {
                    x = rnd.nextInt(MAX_X);
                } else {
                    x = rnd.nextInt(SCREEN_WIDTH) + (screenNo - 1) * SCREEN_WIDTH;
                }
                y = rnd.nextInt(MAX_Y);
            } while ((map[x][y] % 256 > 127 || entityMap[x][y] != 0) && attempts < 100);

            if (attempts >= 100) {
                //System.out.println("Can't find free space!");
                return null;
            }

            newE.status.put(t, new EntityStatus(x, y, 1, 0, 0, 0));

            boolean[] clearDirections = new boolean[4];
            clearDirections[0] = y > 0 && map[x][y - 1] % 256 < 128;
            clearDirections[1] = x < MAX_X - 1 && map[x + 1][y] % 256 < 128;
            clearDirections[2] = y < MAX_Y - 1 && map[x][y + 1] % 256 < 128;
            clearDirections[3] = x > 0 && map[x - 1][y] % 256 < 128;

            Wanderer.pickRandomDirection(clearDirections, newE);

            worldEntities.add(newE);
        }

        return newE;
    }


    public void startTimers() {

        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new EntityUpdater(), 0, 256);

        Timer timer2 = new Timer();
        timer2.scheduleAtFixedRate(new EntitySpawner(), 0, 100);

    }

    public void createMap(int type) {

        mapTimeStamp = System.currentTimeMillis() >> 8;

        switch (type) {
            case 0:
                map = new int[MAX_X][MAX_Y];
                for (int x = 0; x < MAX_X; x++) {
                    for (int y = 0; y < MAX_Y; y++) {
                        if (x == 0 || y == 0 || x == MAX_X-1 || y == MAX_Y-1) {
                            map[x][y] = 128;
                        } else {
                            map[x][y] = 0;
                        }
                    }
                }
                break;
            case 1:
                map = Nessy.Gen1.generate(MAX_X, MAX_Y, mapTimeStamp);
                break;
            case 2:
                map = Nessy.Gen2.generate(MAX_X, MAX_Y, true, mapTimeStamp);
                break;
            case 3:
                map = Lewis.MapGen.makeMap(mapTimeStamp);
                break;
        }

        map = Steve.QuickMazeMaker.fixEdges(map);

        for (int s = 0; s < SCREEN_COUNT; s++) {
            StringBuilder mapScreen = new StringBuilder();
            for (int y = 0; y < MAX_Y; y++) {
                for (int x = SCREEN_WIDTH*s; x < SCREEN_WIDTH*(s+1)+1; x++) {
                    mapScreen.append(map[x][y] + ",");
                }
            }
            encodedMap[s] = mapScreen.toString();
        }

        StringBuilder fullMap = new StringBuilder();
        for (int s = 0; s < SCREEN_COUNT; s++) {

            for (int y = 0; y < MAX_Y; y++) {
                for (int x = 0; x < MAX_X; x++) {
                    fullMap.append(map[x][y] + ",");
                }
            }
        }
        fullEncodedMap = fullMap.toString();

    }

    public static void main(String[] args) throws Exception {

        GameServer gameServer = new GameServer();
        gameServer.createMap(1);
        gameServer.startTimers();

        Server server = new Server(8081);
        server.setHandler(gameServer);
        server.start();
        server.join();

    }

}


