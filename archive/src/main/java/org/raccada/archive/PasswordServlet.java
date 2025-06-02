package org.raccada.archive;

import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.*;

public class PasswordServlet extends HttpServlet {

    private static String LOG_PATH = "/opt/nereus/resources/logs/";
    private static Gson gson = new Gson();
    private static Random rand = new Random();
    private static JsonObject hints;

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseJson = new JsonObject();
        String msg = "";
        try {
            HttpSession session = request.getSession();
            if (hints == null) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/hints.json");
                InputStreamReader reader = new InputStreamReader(inputStream);
                hints = JsonParser.parseReader(reader).getAsJsonObject();
            }

            JsonObject payload = getPayload(request);

            String password = payload.get("password").getAsString();
            msg = password;
            password = password.toUpperCase();
            String key = payload.get("key").getAsString();

            if(!generateKey(password).equals(key)) {
                writeLog(request, "[INVALID KEY] " + msg);
                response.sendError(405);
                return;
            }
            if(session.getAttribute("real") == null) {
                writeLog(request, "[INVALID SESSION] " + msg);
                response.sendError(405);
                return;
            }

            Integer attemptCount = (Integer) session.getAttribute("attemptCount");
            Long firstAttemptTime = (Long) session.getAttribute("firstAttemptTime");
            if (attemptCount == null) {
                attemptCount = 0;
            }
            if (firstAttemptTime == null) {
                firstAttemptTime = System.currentTimeMillis();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - firstAttemptTime > 60000) {
                attemptCount = 0;
                firstAttemptTime = currentTime;
            }
            if (attemptCount >= 6) {
                response.setStatus(429);
                writeLog(request, "[RATELIM] " + msg);
                return;
            }
            attemptCount++;
            session.setAttribute("attemptCount", attemptCount);
            session.setAttribute("firstAttemptTime", firstAttemptTime);

            if (password.equals("LOGBOOK")) {
                responseJson.addProperty("success", true);
                responseJson.addProperty("url", "/10272e4971534cc9e54abe4c550b6336");
                msg = "[SUCCESS] " + msg;
            } else if (password.equals("L!GHTBENEATHRISINGTIDES")) {
                responseJson.addProperty("success", true);
                responseJson.addProperty("url", "/888d0ee361af3603736f12331e7b20a2");
                msg = "[SUCCESS] " + msg;
            } else {
                responseJson.addProperty("success", false);
                if (hints.has(password)) {
                    JsonArray possibleHints = hints.getAsJsonArray(password);
                    String givenHint = possibleHints.get(rand.nextInt(possibleHints.size())).getAsString();
                    responseJson.addProperty("response", givenHint);
                    msg = "[MSG] " + msg;
                }
            }
        } catch(Exception e) {
            response.sendError(405);
            return;
        }
        writeLog(request, msg);
        sendPayload(responseJson, response);
	}

    public JsonObject getPayload(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        JsonObject payload = gson.fromJson(sb.toString(), JsonObject.class);
        return payload;
    }

    private static void writeLog(HttpServletRequest request, String message) throws IOException {
        String sessionId = request.getSession().getId().substring(0,5);
        Date date = new Date(System.currentTimeMillis());

        SimpleDateFormat logFileFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss");
        logFileFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        timestampFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        String logFile = LOG_PATH + logFileFormat.format(date);
        String timestamp = timestampFormat.format(date);
        File f = new File(logFile);
        if(!f.exists()) {
            f.createNewFile();
        } else {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.newLine();
            writer.write("[" + timestamp + "] [" + sessionId + "] " + message);
            writer.close();
        }
    }

    public void sendPayload(JsonObject payload, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(payload));
    }


    public static String stringToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        byte[] bytes = text.getBytes();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(" ");
        }
        return binary.toString();
    }

    public String generateKey(String password) throws Exception {
        String key = "67160f88bf3863f134d530dd3f03d70ec19a8cb737a2b92793232a828b8ea2bc";

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] pwBytes = password.getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[keyBytes.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (keyBytes[i] ^ pwBytes[i % pwBytes.length]);
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e) {
            throw new Exception(e);
        }
        byte[] hashBytes = digest.digest(result);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}