package com.um.mariaros.impl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import java.io.StringReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.message.I18nResolver;

public class ServletHelper {

    private final ApplicationLink applicationLink;
    private final I18nResolver i18n;

    public ServletHelper(ApplicationLink applicationLink, I18nResolver i18n) {
        this.applicationLink = applicationLink;
        this.i18n = i18n;
    }

    private String executeGetCall(String url) {
        try {
            return applicationLink.createImpersonatingAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.GET, url)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public String getOpciones(String data, char p_u) {
        String result = "<option value=''>"
                + i18n.getText("servlet.select-option") + "</option>";
        JSONArray jsonarray = new JSONArray(data);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String value = (p_u == 'u') ? jsonobject.getString("name") : jsonobject.getString("key");
            result += "<option value='" + value + "'>["
                    + jsonobject.getString("key") + "] "
                    + jsonobject.getString("name") + " </option>";
        }
        return result;
    }

    private String trimDate(String date) {
        return date.substring(0, 10);
    }

    public String getFormularioNuevoRequisito() {
        String opcionesProyecto = "";
        String opcionesUsuario = "";
        try {
            String respProyecto = executeGetCall("http://localhost:2990/jira/rest/api/2/project");
            String respUsuario = executeGetCall("http://localhost:2990/jira/rest/api/2/user/search?username=.");
            opcionesProyecto = getOpciones(respProyecto, 'p');
            opcionesUsuario = getOpciones(respUsuario, 'u');
        } catch (Exception e) {
            e.printStackTrace();
        }

        String formulario = "<div class='aui-page-panel-inner'>"
                + "<section class='aui-page-panel-content' style='text-align: center;'>"

                + "<form class='aui' method='post' style='display: inline-block;'>"
                // Proyecto
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='pid'>"
                + i18n.getText("servlet.project-label")
                + "<span class='aui-icon icon-required'>"
                + i18n.getText("servlet.obligatory-label")
                + "</span></label>"
                + "<select class='select' id='pid' name='pid'>"
                + opcionesProyecto
                + "</select>"
                + "</div>"

                // Tipo de requisito
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='issuetype'>" + i18n.getText("servlet.issuetype-label") 
                + "<span class='aui-icon icon-required'>"
                + i18n.getText("servlet.obligatory-label")
                +"</span></label>"
                + "<select class='select' id='issuetype' name='issuetype'>"
                + "<option value='Requisito del sistema'><img alt='' src='images/icono-requisito.svg'/>"
                + i18n.getText("servlet.system-requirement-label")
                +"</option>"
                + "<option value='Historia de usuario'>"+ i18n.getText("servlet.user-story-label") +"</option>"
                + "</select>"
                + "</div>"
                
                // Informador
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='reporter'>"
                + i18n.getText("servlet.reporter-label")
                +"</label>"
                + "<select class='select' id='reporter' name='reporter'>"
                + opcionesUsuario
                + "</select>"
                + "</div>"

                // Resumen
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='summary'>"
                + i18n.getText("servlet.summary-label")
                + "<span class='aui-icon icon-required'>"
                + i18n.getText("servlet.obligatory-label")
                +"</span></label>"
                + "<input class='text long-field' id='summary' name='summary' type='text' value=''>"
                + "</div>"

                // Descripción
                + "<fieldset style='text-align: left;'>"
                + "<legend><span>"
                + i18n.getText("servlet.description-label")
                + "</span></legend>"
                + "<div class='field-group'>"
                + "<label for='description'>"
                + i18n.getText("servlet.description-label")
                + "</label>"
                + "<textarea class='textarea long-field' name='description' id='description'></textarea>"
                + "</div>"
                + "</fieldset>"

                // Responsable
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='assignee'>"
                + i18n.getText("servlet.assignee-label")
                + "</label>"
                + "<select class='select' id='assignee' name='assignee'>"
                + opcionesUsuario
                + "</select>"
                + "</div>"

                // Prioridad
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='priority'>"
                + i18n.getText("servlet.priority-label")
                + "</label>"
                + "<select class='select' id='priority' name='priority'>"
                + "<option value='1'>"
                + i18n.getText("servlet.priority-very-high-label")
                + "</option>"
                + "<option value='2'>"
                + i18n.getText("servlet.priority-high-label")
                + "</option>"
                + "<option value='3' selected>"
                + i18n.getText("servlet.priority-medium-label")
                + "</option>"
                + "<option value='4'>"
                + i18n.getText("servlet.priority-low-label")
                + "</option>"
                + "</select>"
                + "</div>"

                // Estimación original
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='timetracking_originalestimate'>"
                + i18n.getText("servlet.original-estimate-label")
                + "</label>"
                + "<input class='text short-field' id='timetracking_originalestimate' name='timetracking_originalestimate' type='text' value=''>"
                + "<span class='aui-form example'> "
                + i18n.getText("servlet.estimate-example-label")
                + "</span>"
                + "<div class='description'>"
                + i18n.getText("servlet.original-estimate-description-label")
                + "</div>"
                + "</div>"

                // Estimación restante
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='timetracking_remainingestimate'>"
                + i18n.getText("servlet.remaining-estimate-label")
                + "</label>"
                + "<input class='text short-field' id='timetracking_remainingestimate' name='timetracking_remainingestimate' type='text' value=''>"
                + "<span class='aui-form example'> "
                + i18n.getText("servlet.estimate-example-label")
                + "</span>"
                + "<div class='description'>"
                + i18n.getText("servlet.estimate-description-label")
                + "</div>"
                + "</div>"

                + "<hr/>"
                // Botones de enviar y cancelar
                + "<div class='buttons'>"
                + "<input accesskey='e' class='aui-button' id='issue-create-submit' name='Create' type='submit' value='"
                + i18n.getText("servlet.create-button-value")
                + "'>"
                + "<a accesskey='`' class='aui-button aui-button-link cancel' href='/confluence/plugins/servlet/nuevorequisito' id='issue-create-cancel'>"
                + i18n.getText("servlet.cancel-button-value")
                + "</a>"
                + "</div>"

                + "</form>"
                + "</section>"
                + "</div>";

        return formulario;
    }

    // Tabla con todas las incidencias
    public String parseJSONTable(String jsonData) {
        // Icono que se muestra en la tabla, en la columna "Ver en Jira"
        String svgLink = "https://bitbucket.org/atlassian/confluence-icons/raw/957f0b7c3d284cba8f7ef6ef1b0958e3691986bb/src/main/resources/assets/icons/sidebar-open-link.svg";
        String result = "";
        try {
            result += "<script>"
                    + new String(Files.readAllBytes(Paths.get("../../../classes/js/confluencePluginJira.js")),
                            StandardCharsets.UTF_8)
                    + "</script>"
                    + "<input type='text' id='myInput' class='aui text medium-field' onkeyup='searchTable()' placeholder='"
                    + i18n.getText("servlet.search-placeholder")
                    +"...' "
                    + "style='width:20vw; padding: 6px; boder-radius:5px; border: 1px solid LightGray; border-radius: 3px;'>"
                    + "<hr><table class='aui' id='myTable'><thead><tr id='tableHeader'>"
                    + "<th onclick='sortTable(0)' class='table-title'>"
                    + i18n.getText("servlet.type-placeholder")
                    +"</th>"
                    + "<th onclick='sortTable(1)' class='table-title'>"
                    + i18n.getText("servlet.key-placeholder")
                    +"</th>"
                    + "<th onclick='sortTable(2)' class='table-title'>"
                    + i18n.getText("servlet.summary-label")
                    +"</th>"
                    + "<th onclick='sortTable(3)' class='table-title'>"
                    + i18n.getText("servlet.assignee-label")
                    +"</th>"
                    + "<th onclick='sortTable(4)' class='table-title'>"
                    + i18n.getText("servlet.reporter-label")
                    +"</th>"
                    + "<th onclick='sortTable(5)' class='table-title'>"
                    + i18n.getText("servlet.priority-trimmed-label")
                    +"</th>"
                    + "<th onclick='sortTable(6)' class='table-title'>"
                    + i18n.getText("servlet.status-label")
                    +"</th>"
                    + "<th onclick='sortTable(7)' class='table-title'>"
                    + i18n.getText("servlet.resolution-label")
                    +"</th>"
                    + "<th onclick='sortTable(8)' class='table-title'>"
                    + i18n.getText("servlet.created-label")
                    +"</th>"
                    + "<th onclick='sortTable(9)' class='table-title'>"
                    + i18n.getText("servlet.updated-label")
                    +"</th>"
                    + "<th onclick='sortTable(10)' class='table-title'>"
                    + i18n.getText("servlet.project-label")
                    +"</th>"
                    + "<th onclick='sortTable(11)' class='table-title'>"
                    + i18n.getText("servlet.jiralink-label")
                    +"</th>"
                    + "</tr></thead><tbody>";
            JSONObject json = new JSONObject(jsonData);
            JSONArray jsonarray = new JSONArray(json.getString("issues"));
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject root = jsonarray.getJSONObject(i);
                JSONObject fields = new JSONObject(jsonarray.getJSONObject(i).getString("fields"));

                JSONObject issuetype = new JSONObject(fields.getString("issuetype"));
                result += "<tr>";
                result += "<td><img src='"
                        + issuetype.getString("iconUrl")
                        + "' alt='" + issuetype.getString("name")
                        + "' title='" + issuetype.getString("name")
                        + "'/></td>";
                String key = root.getString("key");
                result += "<td>"
                        + "<a href='/confluence/plugins/servlet/verrequisitos?key=" + key + "'>"
                        + key + "</a></td>";

                result += "<td><a href='/confluence/plugins/servlet/verrequisitos?key=" + key + "'>"
                        + fields.getString("summary") + "</a></td>";

                String assigneeString = fields.getString("assignee");
                String assignee = i18n.getText("servlet.not-assigned-label");
                if (!assigneeString.equals("null")) {
                    JSONObject assigneeJSON = new JSONObject(fields.getString("assignee"));
                    assignee = assigneeJSON.getString("name");
                }
                result += "<td>" + assignee + "</td>";

                JSONObject reporter = new JSONObject(fields.getString("reporter"));
                result += "<td>" + reporter.getString("name") + "</td>";

                JSONObject priority = new JSONObject(fields.getString("priority"));
                result += "<td><img src='"
                        + priority.getString("iconUrl")
                        + "' alt='" + priority.getString("name")
                        + "' title='" + priority.getString("name")
                        + "'witdh='20px' height='20px'/></td>";

                JSONObject status = new JSONObject(fields.getString("status"));
                result += "<td>" + status.getString("name") + "</td>";

                String resolution = "Sin resolver";
                if (!fields.getString("resolution").equals("null")) {
                    JSONObject resolutionJSON = new JSONObject(fields.getString("resolution"));
                    resolution = resolutionJSON.getString("name");
                }
                result += "<td>" + resolution + "</td>";

                result += "<td>" + trimDate(fields.getString("created")) + "</td>";
                String updatedString = fields.getString("updated");
                String updated = fields.getString("created");
                if (!updatedString.equals("null"))
                    updated = updatedString;
                result += "<td>" + trimDate(updated) + "</td>";

                JSONObject project = new JSONObject(fields.getString("project"));
                result += "<td>" + project.getString("name") + "</td>";
                result += "<td><a target='_blank' href='http://localhost:2990/jira/browse/" + key + "'><img src='"
                        + svgLink + "'/></a></td>";
                result += "</tr>";
            }
            result += "</tbody></table>";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    // Muestra una tabla para una incidencia concreta
    public String parseJSONIssue(String jsonData) {
        String result = "";
        JSONObject issue = new JSONObject(jsonData);
        JSONObject fields = new JSONObject(issue.getString("fields"));
        // TABLA
        result += "<hr><table class='aui' style='max-width: 700px; margin-left: auto; margin-right: auto;'>";
        result += "<tr><td style='background-color: #0065ff;' colspan='2'><h3 style='color: white; font-weight: bold;'>["
                + issue.getString("key") + "] " + fields.getString("summary") + "</h3></td></tr>";

        // PROYECTO
        JSONObject project = new JSONObject(fields.getString("project"));
        result += "<tr><td>"
        + i18n.getText("servlet.project-label")
        +"</td><td>"
                + project.getString("name") + "</td></tr>";

        // HEADER: DETALLES
        result += "<tr><td colspan='2' style='background-color: #deebff'><h4>"
        + i18n.getText("servlet.details-label")
        +"</h4></td></tr>";

        // TIPO
        JSONObject issuetype = new JSONObject(fields.getString("issuetype"));
        result += "<tr><td>"
        + i18n.getText("servlet.issuetype-label")
        +"</td><td><img src='"
                + issuetype.getString("iconUrl")
                + "' alt='" + issuetype.getString("name")
                + "' title='" + issuetype.getString("name")
                + "'/> " + issuetype.getString("name") + "</td></tr>";

        // RESUMEN
        result += "<tr><td>"
        + i18n.getText("servlet.summary-label")
        +"</td><td>" + fields.getString("summary") + "</td></tr>";

        // PRIORIDAD
        JSONObject priority = new JSONObject(fields.getString("priority"));
        result += "<tr><td>"
        + i18n.getText("servlet.priority-label")
        +"</td><td>"
                + "<img width='16px' src='"
                + priority.getString("iconUrl")
                + "'/>"
                + priority.getString("name") + "</td></tr>";

        // ESTADO
        JSONObject status = new JSONObject(fields.getString("status"));
        result += "<tr><td>"
        + i18n.getText("servlet.status-label")
        +"</td><td>" + status.getString("name") + "</td></tr>";

        // RESOLUCIÓN
        if (fields.getString("resolution").equals("null")) {
            result += "<tr><td>"
            + i18n.getText("servlet.resolution-label")
            +"</td><td>"
            + i18n.getText("servlet.unresolved-label")
            +"</td></tr>";

        } else {
            JSONObject resolution = new JSONObject(fields.getString("resolution"));
            result += "<tr><td>"
            + i18n.getText("servlet.resolution-label")
            +"</td><td>" + resolution.getString("name") + "</td></tr>";
        }

        // HEADER: DESCRIPCIÓN
        result += "<tr><td colspan='2' style='background-color: #deebff'><h4>"
        + i18n.getText("servlet.description-label")
        +"</h4></td></tr>";
        // DESCRIPCIÓN
        if (fields.getString("description").equals("null")) {
            result += "<tr><td colspan='2'></td></tr>";
        } else {
            result += "<tr><td colspan='2'>" + fields.getString("description") + "</td></tr>";
        }

        // HEADER: PERSONAS
        result += "<tr><td colspan='2' style='background-color: #deebff'><h4>"
        + i18n.getText("servlet.people-label")
        +"</h4></td></tr>";
        // Responsable
        if (fields.getString("assignee").equals("null")) {
            result += "<tr><td>"
            + i18n.getText("servlet.assignee-label")
            +"</td><td>"
            + i18n.getText("servlet.not-assigned-label")
            +"</td></tr>";

        } else {
            JSONObject resolution = new JSONObject(fields.getString("assignee"));
            result += "<tr><td>"
            + i18n.getText("servlet.assignee-label")
            +"</td><td>" + resolution.getString("name") + "</td></tr>";
        }

        // Informador
        JSONObject reporter = new JSONObject(fields.getString("reporter"));
        result += "<tr><td>"
        + i18n.getText("servlet.reporter-label")
        +"</td><td>" + reporter.getString("name") + "</td></tr>";

        // ENLACES
        // Muestra todos los enlaces con otros requisitos y los almacena en 'enlaces',
        // para usarlos si se reutiliza el requisito actual
        result += "<tr><td colspan='2' style='background-color: #deebff'><h4>"
        + i18n.getText("servlet.links-label")
        +"</h4></td></tr>";
        result += "<tr><td colspan='2'>";
        JSONArray jsonarray = new JSONArray(fields.getString("issuelinks"));
        String enlaces = "";
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject enlace = jsonarray.getJSONObject(i);
            ;
            String key = "";
            try {
                JSONObject inwardIssue = new JSONObject(enlace.getString("inwardIssue"));
                key = inwardIssue.getString("key");
            } catch (JSONException e) {
                JSONObject outwardIssue = new JSONObject(enlace.getString("outwardIssue"));
                key = outwardIssue.getString("key");
            }
            result += "<a href='/confluence/plugins/servlet/verrequisitos?key=" + key + "'>" + key + "</a>, ";
            enlaces += key + ",";
        }
        result = result.substring(0, result.length() - 2); // Eliminamos la última coma de la lista
        enlaces = enlaces.length() > 0 ? enlaces.substring(0, enlaces.length() - 1) : "";
        result += "</td></tr>";

        // REUTILIZACIÓN DEL REQUISITO
        result += "<tr><td colspan='2' style='background-color: #deebff'><h4>"
        + i18n.getText("servlet.reuse-req-label")
        +"</h4></td></tr>";

        String opcionesProyectoReutilizar = "";
        try {
            String respProyectos = executeGetCall("http://localhost:2990/jira/rest/api/2/project");
            opcionesProyectoReutilizar = getOpciones(respProyectos, 'p');

        } catch (Exception e) {
            e.printStackTrace();
        }

        result += "<tr><td colspan='2'><p>"
                + i18n.getText("servlet.select-project-text-label")
                +"</p>" +
                "<form class='aui' method='post' style='text-align: center;'>" +
                "<input type='hidden' id='key' name='key' value='" +
                issue.getString("key") + "'>" +
                "<input type='hidden' id='enlaces' name='enlaces' value='" +
                enlaces + "'>" +
                // Seleccionar el proyecto
                "<select id='proyecto' name='proyecto' class='select'>" +
                opcionesProyectoReutilizar +
                "</select>" +

                // Botones
                " <input class='aui-button' type='submit' value='"
                + i18n.getText("servlet.reuse-button-value")
                +"' style='margin-left: 10px'></input>" +
                "<br/><br/></form></td></tr>";

        result += "</table>";

        return result;
    }

}
