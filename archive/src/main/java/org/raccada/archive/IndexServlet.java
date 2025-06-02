package org.raccada.archive;

import java.io.IOException;
import java.util.Calendar;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class IndexServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String destination = (String) session.getAttribute("destination");

		if(destination != null) {
			session.setAttribute("destination", null);
			ArrayList<String> requestParams = (ArrayList<String>) session.getAttribute("requestParams");
			if(requestParams != null) {
				for(int i = 0; i < requestParams.size(); i++) {
					String key = requestParams.get(i);
					request.setAttribute(key, session.getAttribute(key));
					session.setAttribute(key, null);
				}
				session.setAttribute("requestParams", null);
			}

			request.getRequestDispatcher(destination).forward(request, response);
		} else {
			long unixTime = System.currentTimeMillis() / 1000L;
			long countdown = 1744009200 - 1;

			if (countdown > unixTime) {
				request.getRequestDispatcher("/countdown.jsp").forward(request, response);
			} else {
				session.setAttribute("real", true);
				request.getRequestDispatcher("/password.jsp").forward(request, response);
			}
		}
	}
}