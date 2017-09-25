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

public class GameServer extends AbstractHandler {

    public final ArrayList<Entity> worldentities = new ArrayList<>();
    public final int SCREENS = 1;

    public class EntityUpdater extends TimerTask {

        public void run() {

            long present = System.currentTimeMillis() >> 8;
            long future = present + 4;
            long past = present - 2;

            synchronized (worldentities) {
                for (Entity e: worldentities) {

                    //System.out.println("KEYS: " + e.xMap.keySet());

                    long first = future;
                    long last = 0;
                    for (long l: e.xMap.keySet()) {
                        if (l > last) last = l;
                        if (l < first) first = l;
                    }

                    //System.out.println(last);

                    if (future != last && e.xMap.containsKey(last) && e.yMap.containsKey(last)) {

                        int x = e.xMap.get(last);
                        int y = e.yMap.get(last);

                        Random rnd = new Random();
                        switch (rnd.nextInt(4)) {
                            case 0: x++; if (x > 19) x = 19; break;
                            case 1: y++; if (y > 15) y = 15; break;
                            case 2: x--; if (x < 0) x = 0; break;
                            case 3: y--; if (y < 0) y = 0; break;
                        }

                        //if (x >= 20 * SCREENS) x -= 20 * SCREENS;

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

        int position = Integer.parseInt(request.getRemoteAddr().split("\\.")[3]);

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

                    //if (variable.equals("index")) position = Integer.parseInt(value);

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
            for (int i = 1; i <= 10; i++) {
                Random rnd = new Random();
                Entity newE = new Entity(i);
                newE.xMap.put(t, rnd.nextInt(20 * SCREENS));
                newE.yMap.put(t, rnd.nextInt(16));
                worldentities.add(newE);
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new EntityUpdater(), 0, 256);

    }

    public static void main(String[] args) throws Exception {

        GameServer gameServer = new GameServer();
        gameServer.createEntities();

        Server server = new Server(8081);
        server.setHandler(gameServer);
        server.start();
        server.join();

    }

}
