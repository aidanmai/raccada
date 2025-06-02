package org.raccada.archive;

import java.util.Random;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.oauth2.Oauth2;

public class OauthServlet extends HttpServlet {

    private static final String clientId = "REDACTED";
    private static final String clientSecret = "REDACTED";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer fullUrlBuf = request.getRequestURL();
        HttpSession session = request.getSession();

        String redirectUri;
        String serverName = request.getServerName();
        if(serverName.equals("localhost")) {
            redirectUri = "http://localhost:8080/d56b699830e77ba53855679ba1d252da";
        } else {
            redirectUri = "https://nereusarchive.org/d56b699830e77ba53855679ba1d252da";
        }

        if (request.getQueryString() != null) {
            fullUrlBuf.append('?').append(request.getQueryString());
            AuthorizationCodeResponseUrl authResponse = new AuthorizationCodeResponseUrl(fullUrlBuf.toString());
            if (authResponse.getError() != null) {
                session.setAttribute("error", "An unknown authentication error occurred");
                response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                return;
            } else {
                String code = authResponse.getCode();
                NetHttpTransport transport = new NetHttpTransport();
                GsonFactory factory = new GsonFactory();

                GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    transport, factory,
                    clientId, clientSecret,
                    code, redirectUri)
                    .execute();
                GoogleCredential credential = new GoogleCredential().setFromTokenResponse(tokenResponse);
                request.getSession().setAttribute("token", tokenResponse.getAccessToken());

                Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new GsonFactory(), credential).setApplicationName("Oauth2").build();
                Userinfoplus userinfo = oauth2.userinfo().get().setFields("email,id,hd").execute();
                String email = userinfo.getEmail();
                String id = userinfo.getId();
                String hd = userinfo.getHd();
                if(hd.equals("ucsd.edu")) {
                    session.setAttribute("email", email);
                    session.setAttribute("id", id);
                    response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                    return;
                } else {
                    session.setAttribute("error", "Only UCSD students can sign the logbook!");
                    response.sendRedirect("/10272e4971534cc9e54abe4c550b6336");
                    return;
                }
            }
        } else {
            String url = new GoogleAuthorizationCodeRequestUrl(
                clientId, redirectUri,
                Arrays.asList("https://www.googleapis.com/auth/userinfo.email"))
                .setState("/email")
                .set("hd", "ucsd.edu")
                .set("prompt", "login")
                .build();
            response.sendRedirect(url);
        }
    }
}