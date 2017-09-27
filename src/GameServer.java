import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.simple.JSONObject;
import static Nessy.Gen1.generate;

public class GameServer extends AbstractHandler {

    public final ArrayList<Entity> worldentities = new ArrayList<>();
    public final int ENTITY_COUNT = 100;
    public final int MAX_X = 401;
    public final int MAX_Y = 17;
    public final int SCREEN_COUNT = 20;
    public final int SCREEN_WIDTH = 20;

    private int[][] map = null;
    private final String[] encodedMap = new String[SCREEN_COUNT];

    public class EntityUpdater extends TimerTask {

        public void run() {

            Random rnd = new Random();

            long present = System.currentTimeMillis() >> 8;
            long future = present + 4;
            long past = present - 2;

            synchronized (worldentities) {
                for (Entity e: worldentities) {

                    long first = future;
                    long last = 0;
                    for (long l: e.xMap.keySet()) {
                        if (l > last) last = l;
                        if (l < first) first = l;
                    }

                    if (future != last && e.xMap.containsKey(last) && e.yMap.containsKey(last)) {

                        int x = e.xMap.get(last);
                        int y = e.yMap.get(last);

                        int target_x = x + e.dx;
                        int target_y = y + e.dy;

                        boolean[] clearDirections = new boolean[4];
                        clearDirections[0] = y > 0 && map[x][y-1] < 128;
                        clearDirections[1] = x < MAX_X-1 && map[x+1][y] < 128;
                        clearDirections[2] = y < MAX_Y-1 && map[x][y+1] < 128;
                        clearDirections[3] = x > 0 && map[x-1][y] < 128;

                        if (target_x < 0 || target_y < 0
                            || target_x >= MAX_X || target_y >= MAX_Y
                            || map[target_x][target_y] > 127)
                        {
                            e.pickRandomDirection(clearDirections, rnd);
                        }
                        else {

                            x = target_x;
                            y = target_y;

                        }

                        e.xMap.put(future, x);
                        e.yMap.put(future, y);

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

        int position = 1;
        boolean sendMap = false;

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

        if (request.getQueryString() != null) {

            for (String q : request.getQueryString().split("&")) {
                if (q.contains("=")) {
                    String variable = q.split("=")[0];
                    String value = q.split("=")[1];
                    requestText += "   " + variable + " = " + value;

                    if (variable.equals("map")) sendMap = value.toLowerCase().equals("true");

                } else {
                    requestText += "   Invalid query string component (" + q + ")";
                }
            }

        } else {
            requestText += "   No query string supplied";
        }
        System.out.println(requestText);

        if (position != -1) {

            ArrayList<JSONObject> frames = new ArrayList<>();
            for (long t = time - 1; t < time + 4; t++) {

                //System.out.println(t);

                ArrayList<JSONObject> entities = new ArrayList<>();

                synchronized (worldentities) {
                    for (Entity e : worldentities) {

                        if (e.xMap.containsKey(t)) {
                            JSONObject entity = new JSONObject();
                            entity.put("id", e.getId());
                            entity.put("type", e.getType());
                            entity.put("x", e.xMap.get(t));
                            entity.put("y", e.yMap.get(t));
                            entities.add(entity);
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

            if (sendMap) finaljson.put("map", encodedMap[position-1]);

            String text = finaljson.toString();

            response.getWriter().println(text);
            System.out.println(text);
        }
        else {
            response.getWriter().println("Error!");
        }

        baseRequest.setHandled(true);

    }

    public void createEntities() {

        long t = System.currentTimeMillis() >> 8;

        synchronized (worldentities) {
            for (int i = 1; i <= ENTITY_COUNT; i++) {
                Random rnd = new Random();
                Entity newE = new Entity(i, rnd.nextInt(6) + 1);

                int x, y;
                do {
                    x = rnd.nextInt(MAX_X);
                    y = rnd.nextInt(MAX_Y);
                } while (map[x][y] > 127);

                newE.xMap.put(t, x);
                newE.yMap.put(t, y);

                boolean[] clearDirections = new boolean[4];
                clearDirections[0] = y > 0 && map[x][y-1] < 128;
                clearDirections[1] = x < MAX_X-1 && map[x+1][y] < 128;
                clearDirections[2] = y < MAX_Y-1 && map[x][y+1] < 128;
                clearDirections[3] = x > 0 && map[x-1][y] < 128;

                newE.pickRandomDirection(clearDirections, rnd);

                worldentities.add(newE);
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new EntityUpdater(), 0, 256);

    }

    public void createMap() {

        map = generate(MAX_X, MAX_Y);

        for (int s = 0; s < SCREEN_COUNT; s++) {
            StringBuilder mapScreen = new StringBuilder();
            for (int y = 0; y < MAX_Y; y++) {
                for (int x = SCREEN_WIDTH*s; x < SCREEN_WIDTH*(s+1)+1; x++) {
                    mapScreen.append(map[x][y] + ",");
                }
            }
            encodedMap[s] = mapScreen.toString();
        }


    }

    public static void main(String[] args) throws Exception {

        GameServer gameServer = new GameServer();
        gameServer.createMap();
        gameServer.createEntities();

        Server server = new Server(8081);
        server.setHandler(gameServer);
        server.start();
        server.join();

    }

}

