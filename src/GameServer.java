import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

public class GameServer extends AbstractHandler {

    public final ArrayList<Entity> worldentities = new ArrayList<>();

    public class EntityUpdater extends TimerTask {

        public void run() {

            //System.out.println("Updating entities");

            synchronized (worldentities) {
                for (Entity e: worldentities) {
                    e.x++;
                    if (e.x > 160) e.x -= 160;
                }
            }

        }

    }

    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {

        int position = Integer.parseInt(request.getRemoteAddr().split("\\.")[3]);

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
            for (int t = 1; t <= 1; t++) {

                ArrayList<JSONObject> entities = new ArrayList<>();

                synchronized (worldentities) {
                    for (Entity e : worldentities) {
                        JSONObject entity = new JSONObject();
                        entity.put("id", e.id);
                        entity.put("x", e.x);
                        entity.put("y", e.y);
                        entities.add(entity);
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
        }
        else {
            response.getWriter().println("Error!");
        }

        baseRequest.setHandled(true);

    }

    public void createEntities() {

        synchronized (worldentities) {
            for (int i = 1; i <= 10; i++) {
                worldentities.add(new Entity(i, 1, i ));
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new EntityUpdater(), 0, 250);

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
