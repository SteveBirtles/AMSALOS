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

    public final static ArrayList<ServerEntity> worldEntities = new ArrayList<>();
    public final static int MAX_X = 401;
    public final static int MAX_Y = 17;
    public final static int SCREEN_COUNT = 20;
    public final static int SCREEN_WIDTH = 20;
    public final static int TOMBSTONE_LIFETIME = 40;
    public final static int VICINITY_SIZE = 17;
    public final static int VICINITY_CENTRE = 8;

    public long mapTimeStamp;

    private int[][] map = null;
    private final String[] encodedMap = new String[SCREEN_COUNT];
    private String fullEncodedMap = "";

    public class EntitySpawner extends TimerTask {

        public void run() {
            Random rnd = new Random();
            int screen = rnd.nextInt(20) + 1;
            int type;
            if (rnd.nextInt(25) == 0) {
                type = rnd.nextInt(4) + 1;
            } else {
                type = rnd.nextInt(12) + 5;
            }
            createEntities(1, screen, type, type <= 4 ? 4 : 3);
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

                for (ServerEntity e: worldEntities) {
                    if (e.tombstoneAge > 40) expired.add(e);
                    if (e.targetEntity > 0) {
                        for (ServerEntity e2: worldEntities) {
                            if (e2.getId() == e.targetEntity) {
                                if (e2.health <= 0) {
                                    e.targetEntity = 0;
                                    break;
                                }
                            }
                        }
                    }
                }
                worldEntities.removeAll(expired);

                int entityMap[][] = ServerEntity.generateEntityMap(worldEntities);

                for (ServerEntity e: worldEntities) {
                    e.calculateAdjacentEntities(entityMap);

                    e.changeHealth(-e.getAdjacentAttackers());
                }

                for (ServerEntity e: worldEntities) {

                    long first = future;
                    long last = 0;
                    for (long l: e.xMap.keySet()) {
                        if (l > last) last = l;
                        if (l < first) first = l;
                    }

                    if (future != last && e.xMap.containsKey(last) && e.yMap.containsKey(last)) {

                        int currentX = e.xMap.get(last);
                        int currentY = e.yMap.get(last);

                        if (e.adjacentAttackers > 0) {

                            e.xMap.put(future, currentX);
                            e.yMap.put(future, currentY);

                        }
                        else if (e.getHealth() == 0) {

                            if (e.foe) e.tombstoneAge++;
                            e.xMap.put(future, currentX);
                            e.yMap.put(future, currentY);

                        } else {

                            int[][] vicinity = null;
                            XY target = null;
                            if (e.getAIType() > 2) {
                                vicinity = e.calculateVicinity(currentX, currentY, map, entityMap);
                            }

                            switch (e.getAIType()) {
                                case 0:
                                    e.dx = 0;
                                    e.dy = 0;
                                    e.xMap.put(future, currentX);
                                    e.yMap.put(future, currentY);
                                    break;
                                case 1:
                                    e.dy = 0;
                                    if (e.dx == 0) e.dx = rnd.nextInt(2) == 0 ? -1 : 1;
                                    int newX = currentX + e.dx;
                                    if (newX < 0 || newX >= MAX_X || map[newX][currentY] % 256 >= 128
                                            || (entityMap[newX][currentY] != 0 && entityMap[newX][currentY] != e.getId())) {
                                        newX = currentX;
                                        e.dx = -e.dx;
                                    }
                                    e.xMap.put(future, newX);
                                    e.yMap.put(future, currentY);
                                    break;
                                case 2:
                                    e.dx = 0;
                                    if (e.dy == 0) e.dy = rnd.nextInt(2) == 0 ? -1 : 1;
                                    int newY = currentY + e.dy;
                                    if (newY < 0 || newY >= MAX_Y || map[currentX][newY] % 256 >= 128
                                            || (entityMap[currentX][newY] != 0 && entityMap[currentX][newY] != e.getId())) {
                                        newY = currentY;
                                        e.dy = -e.dy;
                                    }
                                    e.xMap.put(future, currentX);
                                    e.yMap.put(future, newY);
                                    break;
                                case 3:
                                    target = Wanderer.calculateNext(e, vicinity);

                                    target.x += currentX;
                                    target.y += currentY;
                                    e.xMap.put(future, target.x);
                                    e.yMap.put(future, target.y);
                                    if (target.x >= 0 && target.y >= 0 && target.x < MAX_X && target.y < MAX_Y) {
                                        entityMap[target.x][target.y] = (e.foe ? -1 : 1) * e.getId();
                                    }
                                    break;

                                case 4:

                                    if (e.targetEntity == 0) {
                                        e.pickNextTarget(vicinity, entityMap, currentX, currentY);
                                    }

                                    if (e.targetEntity != 0) {

                                        XY targetEntity = null;
                                        for (int x = 0; x < MAX_X; x++) {
                                            for (int y = 0; y < MAX_Y; y++) {
                                                if (Math.abs(entityMap[x][y]) == e.targetEntity) {
                                                    targetEntity = new XY(x - currentX, y - currentY);
                                                    break;
                                                }
                                            }
                                        }

                                        if (targetEntity != null) {
                                            target = Seeker.calculateNext(e, vicinity, targetEntity.x, targetEntity.y);
                                        } else {
                                            e.targetEntity = 0;
                                        }
                                    }

                                    if (e.targetEntity == 0) {
                                        target = Wanderer.calculateNext(e, vicinity);
                                    }

                                    target.x += currentX;
                                    target.y += currentY;
                                    e.xMap.put(future, target.x);
                                    e.yMap.put(future, target.y);
                                    if (target.x >= 0 && target.y >= 0 && target.x < MAX_X && target.y < MAX_Y) {
                                        entityMap[target.x][target.y] = (e.foe ? -1 : 1) * e.getId();
                                    }
                                    break;
                            }

                        }
                    }

                    if (first <= past && e.xMap.containsKey(last) && e.yMap.containsKey(last)) {

                        e.xMap.remove(first);
                        e.yMap.remove(first);
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
                createEntities(1, position, addEntity, aiType);
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
                        if (e.xMap.containsKey(t)) {
                            int x = e.xMap.get(t);
                            int y = e.yMap.get(t);
                            if (isPlayer || (x >= (position-1)*SCREEN_WIDTH - 2 && x <= position*SCREEN_WIDTH + 2)) {
                                JSONObject entity = new JSONObject();
                                entity.put("i", e.getId());
                                entity.put("t", e.getType());
                                if (e.getHealth() > 0) {
                                    entity.put("h", e.getHealth());
                                }
                                else {
                                    entity.put("h", (double) (-e.tombstoneAge) / (TOMBSTONE_LIFETIME) );
                                }
                                entity.put("a", e.adjacentAttackers);
                                entity.put("x", x);
                                entity.put("y", y);
                                entity.put("f", Boolean.toString(e.getFoe()));
                                entity.put("z", Math.abs(e.targetEntity));
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

    public void createEntities(int entityCount, int screenNo, int type, int aiType) {

        long t = System.currentTimeMillis() >> 8;

        Random rnd = new Random(t);

        synchronized (worldEntities) {

            int entityMap[][] = ServerEntity.generateEntityMap(worldEntities);

            for (int i = 1; i <= entityCount; i++) {

                boolean foe = type > 4;

                ServerEntity newE = new ServerEntity(type, foe ? 10 : 250, foe);
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
                } while (map[x][y]%256 > 127 && entityMap[x][y] == 0 && attempts < 100);

                newE.xMap.put(t, x);
                newE.yMap.put(t, y);

                boolean[] clearDirections = new boolean[4];
                clearDirections[0] = y > 0 && map[x][y-1]%256 < 128;
                clearDirections[1] = x < MAX_X-1 && map[x+1][y]%256 < 128;
                clearDirections[2] = y < MAX_Y-1 && map[x][y+1]%256 < 128;
                clearDirections[3] = x > 0 && map[x-1][y]%256 < 128;

                Wanderer.pickRandomDirection(clearDirections, newE);

                worldEntities.add(newE);
            }
        }

    }

    public void startTimers() {

        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new EntityUpdater(), 0, 256);

        Timer timer2 = new Timer();
        timer2.scheduleAtFixedRate(new EntitySpawner(), 0, 1000);

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


