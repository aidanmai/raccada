package org.raccada.archive;

import java.util.Random;
import java.util.Date;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TimeZone;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.*;
import com.google.gson.stream.*;

public class LogbookServlet extends HttpServlet {

    private static final String LOGBOOK_PATH = "/opt/nereus/resources/logbook.json";
    private static final Gson gson = new Gson();

    private static final int NUMBER_MAX_LEN = 3;
    private static final int USERNAME_MAX_LEN = 20;
    private static final int DATE_MAX_LEN = 14;
    private static final int MESSAGE_MAX_LEN = 80;
    private static final int MIN_EDIT_DURATION = 3600*24;

    private static String TABLE_ROW;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject logbook;
        try {
            logbook = getLogbookJson();
        } catch (FileNotFoundException e) {
            throw new IOException(e.toString());
        }
        
        JsonArray output = new JsonArray();
        output.add(getTableRow());
        output.add(renderRow("N", "USERNAME", "ENTRY DATE", "MESSAGE", false));
        output.add(getTableRow());

        HttpSession session = request.getSession();
        ArrayList<String> requestParams = new ArrayList<String>();

        JsonArray entries = logbook.getAsJsonArray("entries");
        for (int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            String email = entry.get("email").getAsString();
            String id = entry.get("id").getAsString();
            String username = entry.get("username").getAsString();
            String message = entry.get("message").getAsString();
            long lastModified = entry.get("lastModified").getAsLong();
            boolean censored = entry.get("censor").getAsBoolean();

            if(censored) {
                username = "[DATA LOST]";
                message = "[DATA EXPUNGED]";
            }

            if(id.equals(session.getAttribute("id"))) {
                String number = String.format("%03d", i+1);
                session.setAttribute("number", number);
                requestParams.add("number");
                session.setAttribute("username", username);
                requestParams.add("username");
                session.setAttribute("message", message);
                requestParams.add("message");
                long time = System.currentTimeMillis() / 1000L;
                session.setAttribute("canEdit", time - lastModified >= MIN_EDIT_DURATION);
                requestParams.add("canEdit");
                session.setAttribute("censor", censored);
                requestParams.add("censor");
            }

            output.add(renderRow(i + 1, username, entry.get("registered").getAsInt(), message));
        }
        if(entries.size() == 0) output.add("|" + pad("It's very quiet in here...", getTableRow().length() - 4, true) + "|");
        output.add(getTableRow());
        session.setAttribute("entries", output);
        requestParams.add("entries");

        session.setAttribute("requestParams", requestParams);
        session.setAttribute("destination", "/logbook.jsp");
        response.sendRedirect("/");
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contentType = request.getContentType();
        HttpSession session = request.getSession();
        if(contentType != null && contentType.startsWith("application/json")) {
            JsonObject payload = getPayload(request);
            JsonObject responseJson = new JsonObject();
            try {
                String type = payload.get("type").getAsString();
                String email = (String) session.getAttribute("email");
                String id = (String) session.getAttribute("id");

                JsonObject logbook;
                try {
                    logbook = getLogbookJson();
                } catch (FileNotFoundException e) {
                    throw new IOException(e.toString());
                }

                responseJson.addProperty("success", true);
                JsonArray entries = logbook.getAsJsonArray("entries");
                if(type.equals("num")) {
                    responseJson.addProperty("num", entries.size());
                } else if(type.equals("checkUsername")) {
                    String username = payload.get("username").getAsString().toUpperCase();
                    for (int i = 0; i < entries.size(); i++) {
                        JsonObject entry = entries.get(i).getAsJsonObject();
                        if(!entry.get("id").getAsString().equals(id) && entry.get("username").getAsString().toUpperCase().equals(username)) {
                            responseJson.addProperty("valid", false);
                        }
                    }
                    if(!responseJson.has("valid")) responseJson.addProperty("valid", true);
                } else {
                    throw new Exception();
                }
            } catch(Exception e) {
                responseJson = new JsonObject();
                responseJson.addProperty("success", false);
                responseJson.addProperty("message", e.toString());
            }
            sendPayload(responseJson, response);
            return;
        } else if(contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {

            String username = request.getParameter("username").replaceAll(" +", " ").trim();
            String message = request.getParameter("message").replaceAll(" +", " ").trim();
            String email = (String) session.getAttribute("email");
            String id = (String) session.getAttribute("id");
            if(email == null) {
                session.setAttribute("error", "You are not signed in!");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }

            long time = System.currentTimeMillis() / 1000L;

            JsonObject logbook;
            try {
                logbook = getLogbookJson();
            } catch (FileNotFoundException e) {
                session.setAttribute("error", "An unknown error occurred");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }

            Pattern usernamePattern = Pattern.compile("^[a-z0-9 \\-_]*$", Pattern.CASE_INSENSITIVE);
            Pattern messagePattern = Pattern.compile("^[a-z0-9 !?.,;:'\"(){}\\[\\]<>\\-_\\\\\\/+=@#$%^&\\*]*$", Pattern.CASE_INSENSITIVE);

            if(!usernamePattern.matcher(username).find()) {
                session.setAttribute("error", "Username contains invalid characters");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }
            if(!messagePattern.matcher(message).find()) {
                session.setAttribute("error", "Message contains invalid characters");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }
            if(username.length() < 3) {
                session.setAttribute("error", "Username must be at least 3 characters");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }
            if(username.length() > 20) {
                session.setAttribute("error", "Username must be max 20 characters");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }
            if(message.length() > 80) {
                session.setAttribute("error", "Message must be max 80 characters");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }

            JsonArray entries = logbook.getAsJsonArray("entries");
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                if(!entry.get("id").getAsString().equals(id) && entry.get("username").getAsString().toUpperCase().equals(username.toUpperCase())) {
                    session.setAttribute("error", "Username \"" + username + "\" is already taken");
                    response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                    return;
                }
                if(entry.get("email").getAsString().equals(email) && (time - entry.get("lastModified").getAsLong() < MIN_EDIT_DURATION)) {
                    session.setAttribute("error", "You must wait 1 day before editing your entry again!");
                    response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                    return;
                }
                if(entry.get("email").getAsString().equals(email) && entry.get("censor").getAsBoolean()) {
                    session.setAttribute("error", "The submitted entry has been deemed inappropriate and has been expunged from the system.<br>In order for the collective work of The Nereus Archive to succeed, members must be respectful.");
                    response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                    return;
                }
            }

            JsonObject newLogbook = new JsonObject();
            JsonObject newEntry = new JsonObject();
            boolean isNewEntry = true;
            newEntry.addProperty("email", email);
            newEntry.addProperty("id", id);
            newEntry.addProperty("username", username);
            newEntry.addProperty("message", message);
            newEntry.addProperty("lastModified", time);
            newEntry.addProperty("censor", false);
            int n = entries.size() + 1;

            JsonArray newEntries = new JsonArray();
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                if(entry.get("id").getAsString().equals(id)) {
                    isNewEntry = false;
                    n = i + 1;
                    newEntry.addProperty("registered", entry.get("registered").getAsInt());
                    newEntries.add(newEntry);
                } else {
                    newEntries.add(entry);
                }
            }
            if(isNewEntry) {
                newEntry.addProperty("registered", time);
                newEntries.add(newEntry);
                session.setAttribute("success", "Your entry has been successfully submitted! Your entry is Entry No." + String.format("%03d", n) + ".");
            } else {
                session.setAttribute("success", "Entry No." + String.format("%03d", n) + " has been successfully edited!");
            }
            newLogbook.addProperty("numEntries", newEntries.size());
            newLogbook.add("entries", newEntries);

            Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
            try(FileWriter writer = new FileWriter(LOGBOOK_PATH)) {
                gsonWriter.toJson(newLogbook, writer);
                writer.close();
            } catch (IOException e) {
                session.setAttribute("error", "An unknown error occurred");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            }

            response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
            return;

        } else {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "are you sure you should be here?");
            sendPayload(responseJson, response);
            return;
        }
	}

    private JsonObject getLogbookJson() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(LOGBOOK_PATH));
        return gson.fromJson(reader, JsonObject.class);
    }

    private String pad(String str, int maxLength, boolean center) {
        if(str.length() > maxLength) {
            str = str.substring(0, maxLength);
        }
        int diff = (int) maxLength - str.length();

        StringBuilder result = new StringBuilder();
        if(center) {
            int leftPad = 1 + (int) diff / 2;
            int rightPad = leftPad + (diff % 2);
            for(int i = 0; i < leftPad; i++) result.append("&nbsp;");
            result.append(str);
            for(int i = 0; i < rightPad; i++) result.append("&nbsp;");
        } else {
            result.append("&nbsp;");
            result.append(str);
            for(int i = 0; i < diff + 1; i++) result.append("&nbsp;");
        }
        return result.toString();
    }

    private String getTableRow() {
        if(TABLE_ROW == null) {
            StringBuilder tr = new StringBuilder();
            for(int len : new int[]{NUMBER_MAX_LEN, USERNAME_MAX_LEN, DATE_MAX_LEN, MESSAGE_MAX_LEN}) {
                tr.append("+");
                for(int i = 0; i < len + 2; i++) tr.append("-");
            }
            tr.append("+");
            TABLE_ROW = tr.toString();
        }
        return TABLE_ROW;
    }
    
    private String renderRow(int num, String username, int date, String message) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return renderRow(String.format("%03d", num), username, DATE_FORMAT.format(new Date(date * 1000l)), message, true);
    }

    private String renderRow(String num, String username, String date, String message, boolean dataRow) {
        StringBuilder row = new StringBuilder();
        String[] params = new String[]{num, username, date, message};
        int[] max_lens = new int[]{NUMBER_MAX_LEN, USERNAME_MAX_LEN, DATE_MAX_LEN, MESSAGE_MAX_LEN};
        boolean[] center;
        if(dataRow) {
            center = new boolean[]{true, true, true, false};
        } else {
            center = new boolean[]{true, true, true, true};
        }

        for(int i = 0; i < 4; i++) {
            row.append("|");
            row.append(pad(params[i], max_lens[i], center[i]));
        }
        row.append("|");
        return row.toString();
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

    public void sendPayload(JsonObject payload, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(payload));
    }
}