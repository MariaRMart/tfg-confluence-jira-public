package com.um.mariaros.impl;

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

import org.springframework.context.ApplicationListener;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ReturningResponseHandler;


public class ServletHelper {
    
    private final ApplicationLink applicationLink;
    private final DocumentBuilderFactory xmlBuilderFactory;

    public ServletHelper(ApplicationLink applicationLink) {
        this.applicationLink = applicationLink;
        this.xmlBuilderFactory = DocumentBuilderFactory.newInstance();
    }

    private String executeGetCall(String url) {
        try
        {
            return applicationLink.createImpersonatingAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.GET, url)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }       

    }

    public String getOpciones(String data, char p_u) {
        String result = "<option value=''>Seleccione una opción</option>";
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

    // Cambia el string devuelto por Jira con la fecha y solo deja día, mes y año
    private String trimDate(String date){
        String date_array[] = date.split(" ");
        String resultado = "";
        for(int i = 0; i < date_array.length && i < 4; i++) {
            resultado += date_array[i] + " ";
        }
        
        return resultado;
    }

    public String getFormularioNuevoRequisito() {
        String opcionesProyecto = "";
        String opcionesUsuario = "";
        try {
            String respProyecto = executeGetCall("http://localhost:2990/jira/rest/api/2/project");
            String respUsuario = executeGetCall("http://localhost:2990/jira/rest/api/2/user/search?username=.");
            opcionesProyecto = getOpciones(respProyecto, 'p');
            opcionesUsuario = getOpciones(respUsuario, 'u');
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String formulario = "<div class='aui-page-panel-inner'>"
                + "<section class='aui-page-panel-content' style='text-align: center;'>"

                + "<form class='aui' method='post' style='display: inline-block;'>"
                // Proyecto
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='pid'>Proyecto<span class='aui-icon icon-required'>Obligatorio</span></label>"
                + "<select class='select' id='pid' name='pid'>"
                + opcionesProyecto
                + "</select>"
                + "</div>"

                // Tipo de requisito
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='issuetype'>Tipo de requisito<span class='aui-icon icon-required'>Obligatorio</span></label>"
                + "<select class='select' id='issuetype' name='issuetype'>"
                + "<option value='Requisito del sistema'><img alt='' src='images/icono-requisito.svg'/>Requisito del sistema</option>"
                + "<option value='Historia de usuario'>Historia de usuario</option>"
                + "</select>"
                + "</div>"

                // Informador
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='reporter'>Informador</label>"
                + "<select class='select' id='reporter' name='reporter'>"
                + opcionesUsuario
                + "</select>"
                + "</div>"

                // Resumen
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='summary'>Resumen<span class='aui-icon icon-required'>Obligatorio</span></label>"
                + "<input class='text long-field' id='summary' name='summary' type='text' value=''>"
                + "</div>"

                // Descripción
                + "<fieldset style='text-align: left;'>"
                + "<legend><span>Descripción</span></legend>"
                + "<div class='field-group'>"
                + "<label for='description'>Descripción</label>"
                + "<textarea class='textarea long-field' name='description' id='description'></textarea>"
                + "</div>"
                + "</fieldset>"

                // Responsable
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='assignee'>Responsable</label>"
                + "<select class='select' id='assignee' name='assignee'>"
                + opcionesUsuario
                + "</select>"
                + "</div>"

                // Prioridad
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='priority'>Prioridad</label>"
                + "<select class='select' id='priority' name='priority'>"
                + "<option value='1'>Muy alta</option>"
                + "<option value='2'>Alta</option>"
                + "<option value='3' selected>Media</option>"
                + "<option value='4'>Baja</option>"
                + "</select>"
                + "</div>"

                // Estimación original
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='timetracking_originalestimate'>Estimación original</label>"
                + "<input class='text short-field' id='timetracking_originalestimate' name='timetracking_originalestimate' type='text' value=''>"
                + "<span class='aui-form example'> (Por ejemplo, 3w 4d 12h)</span>"
                + "<div class='description'>La estimación original de cuánto trabajo implicaría la resolución de esta incidencia.</div>"
                + "</div>"

                // Estimación restante
                + "<div class='field-group' style='text-align: left;'>"
                + "<label for='timetracking_remainingestimate'>Estimación restante</label>"
                + "<input class='text short-field' id='timetracking_remainingestimate' name='timetracking_remainingestimate' type='text' value=''>"
                + "<span class='aui-form example'> (Por ejemplo, 3w 4d 12h)</span>"
                + "<div class='description'>Una estimación del trabajo que aún queda por realizar hasta que esta incidencia sea resuelta.</div>"
                + "</div>"

                + "<hr/>"
                // Botones de enviar y cancelar
                + "<div class='buttons'>"
                + "<input accesskey='e' class='aui-button' id='issue-create-submit' name='Create' title='Presione Alt+e para enviar este formulario' type='submit' value='Crear'>"
                + "<a accesskey='`' class='aui-button aui-button-link cancel' href='/confluence/plugins/servlet/nuevorequisito' id='issue-create-cancel' title='Presione Alt+` para cancelar'>Cancelar</a>"
                + "</div>"

                + "</form>"
                + "</section>"
                + "</div>";

        return formulario;
    }

        // Tabla con todas las incidencias
        public String parseXMLTable(String xmlData) {
            // Icono que se muestra en la tabla, en la columna "Ver en Jira"
            String svgLink = "https://bitbucket.org/atlassian/confluence-icons/raw/957f0b7c3d284cba8f7ef6ef1b0958e3691986bb/src/main/resources/assets/icons/sidebar-open-link.svg";
            String result = "";
            try {
                result = "<hr><table class='aui'><thead><tr>"
                        + "<th>T</th>"
                        + "<th>Clave</th>"
                        + "<th>Resumen</th>"
                        + "<th>Responsable</th>"
                        + "<th>Informador</th>"
                        + "<th>Pr</th>"
                        + "<th>Estado</th>"
                        + "<th>Resolución</th>"
                        + "<th>Creada</th>"
                        + "<th>Actualizada</th>"
                        + "<th>Proyecto</th>"
                        + "<th>Ver en Jira</th>"
                        + "</tr></thead><tbody>";
    
                // TODO: cambiar XML por JSON 
                DocumentBuilder builder = xmlBuilderFactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xmlData));
                Document doc = builder.parse(is);
                NodeList issuesList = doc.getElementsByTagName("item");
    
                // Recorremos todas las incidencias y las añadimos en la tabla 
                for (int i = 0; i < issuesList.getLength(); i++) {
                    result += "<tr>";
                    Element issue = (Element) issuesList.item(i);
                    result += "<td><img src='"
                            + issue.getElementsByTagName("type").item(0).getAttributes().getNamedItem("iconUrl")
                                    .getNodeValue()
                            + "' alt='" + issue.getElementsByTagName("type").item(0).getTextContent()
                            + "' title='" + issue.getElementsByTagName("type").item(0).getTextContent()
                            + "'/></td>";
    
                    String key = issue.getElementsByTagName("key").item(0).getTextContent();
                    result += "<td>"
                            + "<a href='/confluence/plugins/servlet/verrequisitos?key=" + key + "'>"
                            + key + "</a></td>";
    
                    NodeList parent = issue.getElementsByTagName("parent");
                    if (parent.getLength() != 0) {
                        result += "<td>" + parent.item(0).getTextContent() + "/ ";
                    } else {
                        result += "<td>";
                    }
                    result += issue.getElementsByTagName("summary").item(0).getTextContent() + "</td>";
                    result += "<td>" + issue.getElementsByTagName("assignee").item(0).getTextContent() + "</td>";
                    result += "<td>" + issue.getElementsByTagName("reporter").item(0).getTextContent() + "</td>";
                    result += "<td><img src='"
                            + issue.getElementsByTagName("priority").item(0).getAttributes().getNamedItem("iconUrl")
                                    .getNodeValue()
                            + "' alt='" + issue.getElementsByTagName("priority").item(0).getTextContent()
                            + "' title='" + issue.getElementsByTagName("priority").item(0).getTextContent()
                            + "'witdh='20px' height='20px'/></td>";
                    result += "<td>" + issue.getElementsByTagName("status").item(0).getTextContent() + "</td>";
                    result += "<td>" + issue.getElementsByTagName("resolution").item(0).getTextContent() + "</td>";
                    result += "<td>" + trimDate(issue.getElementsByTagName("created").item(0).getTextContent()) + "</td>";
                    result += "<td>" + trimDate(issue.getElementsByTagName("updated").item(0).getTextContent()) + "</td>";
                    result += "<td>" + issue.getElementsByTagName("project").item(0).getTextContent() + "</td>";
                    result += "<td><a href='" + issue.getElementsByTagName("link").item(0).getTextContent() + "'><img src='" + svgLink + "'/></a></td>";
                    result += "</tr>";
                }
                result += "</tbody></table>";
    
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    
    // Muestra una tabla para una incidencia concreta
    public String parseXMLIssue(String xmlData) {
        String result = "";
        try {
            DocumentBuilder builder = xmlBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlData));
            Document doc = builder.parse(is);
            NodeList issuesList = doc.getElementsByTagName("item");
            Element issue = (Element) issuesList.item(0);
            // TABLA
            result += "<hr><table class='aui' style='max-width: 700px; margin-left: auto; margin-right: auto;'>";
            result += "<tr><td style='background-color: #0065ff;' colspan='2'><h3 style='color: white; font-weight: bold;'>"
                    + issue.getElementsByTagName("title").item(0).getTextContent() + "</h3></td></tr>";

            // PROYECTO
            result += "<tr><td>Proyecto</td><td>"
                    + issue.getElementsByTagName("project").item(0).getTextContent() + "</td></tr>";

            // HEADER: DETALLES
            result += "<tr><td colspan='2' style='background-color: #deebff'><h4>Detalles</h4></td></tr>";

            // TIPO
            result += "<tr><td>Tipo</td><td><img src='"
                    + issue.getElementsByTagName("type").item(0).getAttributes().getNamedItem("iconUrl")
                            .getNodeValue()
                    + "' alt='" + issue.getElementsByTagName("type").item(0).getTextContent()
                    + "' title='" + issue.getElementsByTagName("type").item(0).getTextContent()
                    + "'/> " + issue.getElementsByTagName("type").item(0).getTextContent() + "</td></tr>";

            // RESUMEN
            result += "<tr><td>Resumen</td><td>" + issue.getElementsByTagName("summary").item(0).getTextContent()
                    + "</td></tr>";

            // PRIORIDAD
            result += "<tr><td>Prioridad</td><td>"
                    + "<img width='16px' src='"
                    + issue.getElementsByTagName("priority").item(0).getAttributes().getNamedItem("iconUrl")
                            .getNodeValue()
                    + "'/>"
                    + issue.getElementsByTagName("priority").item(0).getTextContent() + "</td></tr>";

            // ESTADO
            result += "<tr><td>Estado</td><td>" + issue.getElementsByTagName("status").item(0).getTextContent()
                    + "</td></tr>";

            // RESOLUCIÓN
            result += "<tr><td>Resolución</td><td>" + issue.getElementsByTagName("resolution").item(0).getTextContent()
                    + "</td></tr>";

            // HEADER: DESCRIPCIÓN
            result += "<tr><td colspan='2' style='background-color: #deebff'><h4>Descripción</h4></td></tr>";
            // DESCRIPCIÓN
            result += "<tr><td colspan='2'>" + issue.getElementsByTagName("description").item(0).getTextContent()
                    + "</td></tr>";

            // HEADER: PERSONAS
            result += "<tr><td colspan='2' style='background-color: #deebff'><h4>Personas</h4></td></tr>";
            // Responsable
            result += "<tr><td>Responsable</td><td>" + issue.getElementsByTagName("assignee").item(0).getTextContent()
            + "</td></tr>";
            // Informador
            result += "<tr><td>Informador</td><td>" + issue.getElementsByTagName("reporter").item(0).getTextContent()
                    + "</td></tr>";


            // ENLACES
            // Muestra todos los enlaces con otros requisitos y los almacena en 'enlaces', para
            // usarlos si se reutiliza el requisito actual
            result += "<tr><td colspan='2' style='background-color: #deebff'><h4>Enlaces</h4></td></tr>";
            result += "<tr><td colspan='2'>";
            NodeList listaEnlaces = issue.getElementsByTagName("issuekey");
            String enlaces = "";
            for (int i = 0; i < listaEnlaces.getLength(); i++) {
                String key = listaEnlaces.item(i).getTextContent();
                result += "<a href='/confluence/plugins/servlet/verrequisitos?key=" + key + "'>" + key + "</a>, ";
                enlaces += key + ",";
            }
            result = result.substring(0, result.length() - 2); // Eliminamos la última coma de la lista
            enlaces = enlaces.length() > 0 ? enlaces.substring(0, enlaces.length() - 1) : "";
            result += "</td></tr>";

            // REUTILIZACIÓN DEL REQUISITO
            result += "<tr><td colspan='2' style='background-color: #deebff'><h4>Reutilizar requisito</h4></td></tr>";

            String opcionesProyectoReutilizar = "";
            try {
                String respProyectos = executeGetCall("http://localhost:2990/jira/rest/api/2/project");
                opcionesProyectoReutilizar = getOpciones(respProyectos, 'p');

            } catch (Exception e) {
                e.printStackTrace();
            }

            result += "<tr><td colspan='2'><p>Seleccione un proyecto para el que reutilizar este requisito: </p>" +
                    "<form class='aui' method='post' style='text-align: center;'>" +
                        "<input type='hidden' id='key' name='key' value='" +
                            issue.getElementsByTagName("key").item(0).getTextContent() + "'>" +
                        "<input type='hidden' id='enlaces' name='enlaces' value='" +
                            enlaces + "'>" +
                        // Seleccionar el proyecto
                        "<select id='proyecto' name='proyecto' class='select'>" +
                            opcionesProyectoReutilizar +
                        "</select>" +

                        // Botones
                        " <input class='aui-button' type='submit' value='Reutilizar' style='margin-left: 10px'></input>" +
                    "<br/><br/></form></td></tr>";

            result += "</table>";

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
