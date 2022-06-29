package com.um.mariaros.impl;
import com.um.mariaros.impl.ServletHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.inject.Inject;

import org.json.JSONObject;

import java.util.Date;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.net.URI;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.net.*;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.sal.api.message.I18nResolver;

public class MyPluginServletNuevoRequisito extends HttpServlet {
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final ApplicationLinkService applicationLinkService;
    @ComponentImport
    private final I18nResolver i18n;

    @Inject
    public MyPluginServletNuevoRequisito(UserManager userManager, LoginUriProvider loginUriProvider,
            ApplicationLinkService applicationLinkService, I18nResolver i18n) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.applicationLinkService = applicationLinkService;
        this.i18n = i18n;

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = userManager.getRemoteUsername(request);
        if (username == null) {
            redirectToLogin(request, response);
            return;
        }

        StringBuffer stringBuffer = new StringBuffer();

        for (final ApplicationLink applicationLink : this.applicationLinkService
                .getApplicationLinks(JiraApplicationType.class)) {
            try {
                ServletHelper helper = new ServletHelper(applicationLink, i18n);
                stringBuffer.append(
                        "<html><head><title>"
                        + i18n.getText("servlet.new-requirement-label")
                        +"</title><meta name='decorator' content='atl.general'></head><body><hr>");
                stringBuffer.append(helper.getFormularioNuevoRequisito());
                stringBuffer.append("</body><footer /></html>");

            } catch (Exception e) {
                stringBuffer.append("Error received\n");
                stringBuffer.append(e.toString());
            }

        }

        response.setContentType("text/html");
        response.getWriter().write(stringBuffer.toString());

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = userManager.getRemoteUsername(request);
        if (username == null) {
            redirectToLogin(request, response);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(
                "<html><head><title>"
                + i18n.getText("servlet.new-requirement-label")
                +"</title><meta name='decorator' content='atl.general'></head><body><hr>");

        String projectId = request.getParameter("pid");
        String issuetype = request.getParameter("issuetype");
        String reporter = request.getParameter("reporter");
        String summary = request.getParameter("summary");
        String description = request.getParameter("description");
        String assignee = request.getParameter("assignee");
        String priority = request.getParameter("priority");
        String originalestimate = request.getParameter("timetracking_originalestimate");
        String remainingestimate = request.getParameter("timetracking_remainingestimate");
        String body = "{"
                + "  \"fields\": {"
                + "       \"project\":"
                + "       {"
                + "          \"key\": \"" + projectId + "\""
                + "       },"
                + "       \"summary\": \"" + summary + "\","
                + "       \"description\": \"" + description + "\","
                + "       \"assignee\":"
                + "       {"
                + "          \"name\": \"" + assignee + "\""
                + "       },"
                + "       \"reporter\":"
                + "       {"
                + "          \"name\": \"" + reporter + "\""
                + "       },"
                + "       \"issuetype\": {"
                + "          \"name\": \"" + issuetype + "\""
                + "       },"
                + "       \"priority\":"
                + "       {"
                + "          \"id\": \"" + priority + "\""
                + "       },"
                + "       \"timetracking\":"
                + "       {"
                + "          \"originalEstimate\": \"" + originalestimate + "\","
                + "          \"remainingEstimate\": \"" + remainingestimate + "\""
                + "       }"
                + "   }"
                + "}";

        for (final ApplicationLink applicationLink : this.applicationLinkService
                .getApplicationLinks(JiraApplicationType.class)) {

            try {
                String resp = applicationLink.createImpersonatingAuthenticatedRequestFactory()
                        .createRequest(Request.MethodType.POST, "http://localhost:2990/jira/rest/api/2/issue")
                        .addHeader("Content-Type", "application/json")
                        .setRequestBody(body)
                        .execute();

                JSONObject json = new JSONObject(resp);
                String key = json.getString("key");
                String link = json.getString("self");

                stringBuffer
                        .append("<p>"
                        + i18n.getText("servlet.new-requirement-created-label")
                        +" <a href='/confluence/plugins/servlet/verrequisitos?key="
                                + key + "'>" + key + "</a></p><br/>");
                stringBuffer.append("<form action='/confluence/plugins/servlet/nuevorequisito'>");
                stringBuffer.append("<input class='aui-button' type='submit' value='"
                + i18n.getText("servlet.go-back-button")
                +"' />");
                stringBuffer.append("</form>");

            } catch (Exception e) {
                e.printStackTrace();
                stringBuffer.append("<p>"
                + i18n.getText("servlet.new-requirement-error-label")
                +"</p><br/>");
                stringBuffer.append("<form action='/confluence/plugins/servlet/nuevorequisito'>");
                stringBuffer.append("<input class='aui-button' type='submit' value='"
                + i18n.getText("servlet.go-back-button")
                +"' />");
                stringBuffer.append("</form>");
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
