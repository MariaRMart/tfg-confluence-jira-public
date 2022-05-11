package com.um.mariaros.impl;
import com.um.mariaros.impl.ServletHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.inject.Inject;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.net.URI;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.net.*;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.ApplicationLink;


public class MyPluginServletVerRequisitos extends HttpServlet {
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final ApplicationLinkService applicationLinkService;

    @Inject
    public MyPluginServletVerRequisitos(UserManager userManager, LoginUriProvider loginUriProvider,
            ApplicationLinkService applicationLinkService) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.applicationLinkService = applicationLinkService;
    }

    // Si doGet recibe un parámetro con una 'key' a una incidencia, muestra una tabla con esa incidencia concreta;
    // si no, muestra una tabla con todos los requisitos. 
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = userManager.getRemoteUsername(request);
        if (username == null) {
            redirectToLogin(request, response);
            return;
        }

        final StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(
                "<html><head><title>Gestión de requisitos</title><meta name='decorator' content='atl.general'></head><body>");
        String key = request.getParameter("key");

        // Conexión con Jira
        for (final ApplicationLink applicationLink : this.applicationLinkService
                .getApplicationLinks(JiraApplicationType.class)) {

            try {
                ServletHelper helper = new ServletHelper(applicationLink);
                // Si no recibe el parámetro key muestra todo
                // TODO: usar la REST API de Jira en lugar de pedir el XML
                if (key == null) {
                    String url = "http://localhost:2990/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml"
                            + "?jqlQuery=issuetype+%3D+'Requisito+del+sistema'+OR+issuetype+%3D+'Historia+de+usuario'+&tempMax=1000";

                    Request req = applicationLink.createImpersonatingAuthenticatedRequestFactory()
                            .createRequest(Request.MethodType.GET, url);

                    req.executeAndReturn(new ReturningResponseHandler() {
                        public Object handle(Response res) throws ResponseException {
                            if (res.isSuccessful()) {
                                stringBuffer.append(helper.parseXMLTable(res.getResponseBodyAsString()));
                            }
                            return null;
                        }
                    });

                } else {
                    
                    // Si recibe una key, usa su valor para la petición a Jira
                    // TODO: usar la REST API de Jira en lugar de pedir el XML
                    String url = "http://localhost:2990/jira/si/jira.issueviews:issue-xml/" + key + "/" + key + ".xml";

                    Request req = applicationLink.createImpersonatingAuthenticatedRequestFactory()
                            .createRequest(Request.MethodType.GET, url);

                    req.executeAndReturn(new ReturningResponseHandler() {
                        public Object handle(Response res) throws ResponseException {
                            if (res.isSuccessful()) {
                                stringBuffer.append(helper.parseXMLIssue(res.getResponseBodyAsString()));
                            }
                            return null;
                        }
                    });
                }

            } catch (Exception e) {
                stringBuffer.append("<p>Error al cargar la información</p>");
                e.printStackTrace();
            }

        }

        stringBuffer.append("</body><footer /></html>");
        response.setContentType("text/html");
        response.getWriter().write(stringBuffer.toString());

    }

    // Si llega una petición para reutilizar un requisito desde la tabla de un requisito concreto
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = userManager.getRemoteUsername(request);
        if (username == null) {
            redirectToLogin(request, response);
            return;
        }

        final StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(
                "<html><head><title>Gestión de requisitos</title><meta name='decorator' content='atl.general'></head><body>");
        
        // key del requisito a reutilizar
        String key = request.getParameter("key");

        // Conexión con Jira
        for (final ApplicationLink applicationLink : this.applicationLinkService
                .getApplicationLinks(JiraApplicationType.class)) {

            try {
                // hace la petición con la key, el proyecto en el que reutilizar
                String url = "http://localhost:2990/jira/plugins/servlet/reqreuseservlet?key="
                        + request.getParameter("key")
                        + "&proyectoR=" + request.getParameter("proyecto");
                String enlaces = request.getParameter("enlaces");

                if (enlaces != null) {
                    url += "&enlaces=[" + enlaces + "]";
                } else {
                    url += "&enlaces=[]";
                }

                Request req = applicationLink.createImpersonatingAuthenticatedRequestFactory()
                        .createRequest(Request.MethodType.GET, url);

                req.executeAndReturn(new ReturningResponseHandler() {
                    public Object handle(Response res) throws ResponseException {
                        if (res.isSuccessful()) {
                            stringBuffer.append("<p>Requisito reutilizado.</p><br/>");
                            stringBuffer.append("<form action='/confluence/plugins/servlet/verrequisitos'>");
                            stringBuffer.append("<input class='aui-button' type='submit' value='Volver' />");
                            stringBuffer.append("</form>");
                        }
                        return null;
                    }
                });

            } catch (Exception e) {
                stringBuffer.append("<p>Error al reutilizar el requisito.</p>");
                stringBuffer.append("<form action='/confluence/plugins/servlet/verrequisitos'>");
                stringBuffer.append("<input class='aui-button' type='submit' value='Volver' />");
                stringBuffer.append("</form>");
                e.printStackTrace();
            }

        }

        stringBuffer.append("</body><footer /></html>");
        response.setContentType("text/html");
        response.getWriter().write(stringBuffer.toString());

    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
