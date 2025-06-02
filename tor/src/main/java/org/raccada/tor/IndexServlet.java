package org.raccada.archive;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.*;
import com.google.gson.stream.*;

public class IndexServlet extends HttpServlet {

	private static final Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rootFolder = getServletContext().getRealPath("/files");
		String requestPath = request.getRequestURI();

		File requestFile = new File(rootFolder + requestPath);

		if(requestFile.isDirectory()) {
			JsonReader reader = new JsonReader(new FileReader(new File(requestFile, "manifest.json")));
			JsonObject manifest = gson.fromJson(reader, JsonObject.class);

			if(manifest.has("corrupt")) {
				throw new IOException("folder " + rootFolder + requestPath + " contains corrupted data");
			}

			request.setAttribute("path", manifest.get("path").getAsString());

			JsonArray contents = manifest.getAsJsonArray("contents");
			StringBuilder pre = new StringBuilder();

			long unixTime = System.currentTimeMillis() / 1000L;
			boolean unlocked = true;
			if(manifest.has("unlockDate") && manifest.get("unlockDate").getAsLong() > unixTime) {
				long unlockDate = manifest.get("unlockDate").getAsLong();
				pre.append("This folder will be unlocked on " + formatDate(unlockDate));
				unlocked = false;
			}

			pre.append("<pre><img src=\"/resources/blank.png\"> Name                    Last modified      Size  Description<hr>");
			for(int i = 0; i < contents.size(); i++) {
				JsonObject fileJson = contents.get(i).getAsJsonObject();
				if(unlocked || fileJson.get("type").getAsString().equals("parent")) pre.append(toRow(fileJson, requestPath));
			}
			pre.append("<hr></pre>");

			request.setAttribute("pre", pre);
			request.getRequestDispatcher("/explorer.jsp").forward(request, response);
		} else if(requestFile.isFile()) {
			downloadFile(requestFile, response);
		} else {
			throw new IOException("file " + rootFolder + requestPath + " is not a file");
		}
	}

	private String toRow(JsonObject fileJson, String requestPath) {
		StringBuilder output = new StringBuilder();

		String type = fileJson.get("type").getAsString();
		String icon;
		if(type.equals("parent")) icon = "/resources/back.png";
		else if(type.equals("folder")) icon = "/resources/folder.png";
		else if(type.equals("pdf")) icon = "/resources/text.png";
		else icon = "/resources/unknown.png";
		output.append("<img src=\"").append(icon).append("\">");

		String name = fileJson.get("name").getAsString();
		String url;
		if(type.equals("parent")) url = fileJson.get("parentPath").getAsString();
		else if (requestPath.equals("/")) url = requestPath + name;
		else url = requestPath + "/" + name;
		if(fileJson.has("corrupt")) {
			String paddedRedact = pad("[DATA CORRUPTED]", 23, false);
			output.append(paddedRedact);
		} else {
			String paddedName = pad(name, 23, false);
			if(name.length() > 23) {
				name = name.substring(0,23);
				output.append(paddedName.replace(name, "<a href=\"" + url + "\">" + name + "</a>"));
			} else {
				output.append(paddedName.replace(name, "<a href=\"" + url + "\">" + name + "</a>"));
			}
		}

		long date = fileJson.get("lastModified").getAsLong();
		if(date == -1) output.append("       -        ");
		else output.append(formatDate(date));

		String size = fileJson.get("size").getAsString();
		output.append(pad(size, 7, true));

		String description = fileJson.get("description").getAsString();
		output.append(description);
		output.append("\n");

		return output.toString();
	}

	private String formatDate(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		return dateFormat.format(new Date(date * 1000l));
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
            for(int i = 0; i < leftPad; i++) result.append(" ");
            result.append(str);
            for(int i = 0; i < rightPad; i++) result.append(" ");
        } else {
            result.append(" ");
            result.append(str);
            for(int i = 0; i < diff + 1; i++) result.append(" ");
        }
        return result.toString();
    }

	private void downloadFile(File file, HttpServletResponse response) throws IOException {
		if (file.exists() && file.isFile()) {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

			FileInputStream fileInputStream = new FileInputStream(file);
			OutputStream outputStream = response.getOutputStream();

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
			response.flushBuffer();
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + file.getName());
		}
	}
}