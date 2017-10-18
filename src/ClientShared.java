import Steve.QuickMazeMaker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientShared {

    public static int failCount = 0;
    public static long mapTimeStamp = 0;
    static int viewportPosition = 0;

    public static void requestPost(String serverAddress, String query) {

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081?" + query);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            int responseCode = 0;
            try {
                responseCode = con.getResponseCode();
            }
            catch (ConnectException ce) {
                System.out.println("Unable to connect to server...");
            }
            if (responseCode != 200) {
                System.out.println("HTTP POST ERROR: " + responseCode);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int[][] getUpdate(String serverAddress, int[][] map, int screen, boolean isPlayer, ArrayList<ClientEntity> currentEntities) {

        long clientTime = System.currentTimeMillis() >> 8;
        HashMap<Integer, ClientEntity> entities = new HashMap<>();

        URL url;
        HttpURLConnection con;

        try {
            url = new URL( "http://" + serverAddress + ":8081"
                    + "?index=" + clientTime
                    + "&player=" + Boolean.toString(isPlayer)
                    + "&mapTimeStamp=" + mapTimeStamp
                    + "&map=" + (map == null ? "true" : "false")
                    + "&screen=" + screen);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = 0;
            try {
                responseCode = con.getResponseCode();
            }
            catch (ConnectException ce) {
                System.out.println("Unable to connect to server...");
                failCount++;
                if (failCount > 10) System.exit(-10);
            }

            if (responseCode == 200) {
                failCount = 0;
                InputStream inputStream = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String inputjson = "";
                while (br.ready()) {
                    inputjson = br.readLine();
                }

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(inputjson);
                JSONObject jsonObject = (JSONObject) obj;

                if (jsonObject.containsKey("map")) {

                    int maxX;
                    int maxY;

                    if (isPlayer) {
                        maxX = GameServer.MAX_X;
                        maxY = GameServer.MAX_Y;
                    } else {
                        maxX = GameClient.MAX_X;
                        maxY = GameClient.MAX_Y;
                    }

                    map = QuickMazeMaker.emptyMap(maxX, maxY);

                    String mapString = jsonObject.get("map").toString();

                    int x = 0;
                    int y = 0;
                    for (String value : mapString.split(",")) {
                        map[x][y] = Integer.parseInt(value);
                        x++;
                        if (x == maxX) {
                            x = 0;
                            y++;
                            if (y == maxY) break;
                        }
                    }

                }

                if (jsonObject.containsKey("mapTimeStamp")) {
                    mapTimeStamp = Long.parseLong(jsonObject.get("mapTimeStamp").toString());
                    System.out.println("Recieved: " + mapTimeStamp);
                }

                if (jsonObject.containsKey("frames")) {

                    JSONArray frameArray = (JSONArray) jsonObject.get("frames");
                    for (Object frameObject : frameArray) {
                        JSONObject frame = (JSONObject) frameObject;

                        long time;

                        if (frame.containsKey("time") && frame.containsKey("position") && frame.containsKey("entities")) {

                            time = Long.parseLong(frame.get("time").toString());

                            //CLIENT
                            viewportPosition = Integer.parseInt(frame.get("position").toString()) - 1;

                            JSONArray entityArray = (JSONArray) frame.get("entities");

                            for (Object entityObject : entityArray) {
                                JSONObject entity = (JSONObject) entityObject;
                                int id = Integer.parseInt(entity.get("i").toString());
                                int type = Integer.parseInt(entity.get("t").toString());
                                int x = Integer.parseInt(entity.get("x").toString());
                                int y = Integer.parseInt(entity.get("y").toString());
                                double health = Double.parseDouble(entity.get("h").toString());
                                int adjacentAttackers = Integer.parseInt(entity.get("a").toString());
                                boolean foe = Boolean.parseBoolean(entity.get("f").toString());
                                int target = Integer.parseInt(entity.get("z").toString());
                                String name = entity.get("n").toString();
                                int score = Integer.parseInt(entity.get("s").toString());
                                int pause = Integer.parseInt(entity.get("p").toString());

                                if (entities.containsKey(id)) {

                                    long last = 0;
                                    for (long l: entities.get(id).status.keySet()) {
                                        if (l > last) last = l;
                                    }

                                    EntityStatus lastStatus = entities.get(id).status.get(last);

                                    entities.get(id).status.put(time, new EntityStatus(x, y, lastStatus));

                                } else {

                                    ClientEntity newE = new ClientEntity(id, type, foe);

                                    newE.status.put(time, new EntityStatus(x, y, health, score, pause, target, adjacentAttackers));
                                    newE.setName(name);

                                    entities.put(id, newE);

                                }
                            }
                        }
                    }
                }

                synchronized (currentEntities) {
                    currentEntities.clear();
                    for (ClientEntity e : entities.values()) {
                        currentEntities.add(e);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;

    }
}
