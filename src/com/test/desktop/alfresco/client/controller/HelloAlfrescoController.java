package com.test.desktop.alfresco.client.controller;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.test.desktop.alfresco.client.model.AlfrescoSAMLEnabledInfo;

public class HelloAlfrescoController
{
    public static final String ALFRESCO_SP_ID = "rest-api";
    public static final String ALFRESCO_SERVER = "http://localhost:8080/alfresco";
    public static final String FILE_ID = "1d06aa13-1149-4869-b466-ea30b5f37f26";
    public static final String ALFRESCO_ENABLED = ALFRESCO_SERVER + "/service/saml/-default-/" + ALFRESCO_SP_ID + "/enabled";
    public static final String ALFRESCO_AUTHENTICATE = ALFRESCO_SERVER + "/service/saml/-default-/" + ALFRESCO_SP_ID + "/authenticate";
    public static final String ALFRESCO_AUTHENTICATE_RESPONSE = ALFRESCO_SERVER + "/service/saml/-default-/" + ALFRESCO_SP_ID
            + "/authenticate-response";
    public static final String ALFRESCO_AUTHENTICATE_RESPONSE_S = "https://localhost:8443/alfresco/service/saml/-default-/" + ALFRESCO_SP_ID
            + "/authenticate-response";

    private AlfrescoSAMLEnabledInfo info;
    private CookieManager cookieManager;
    private HttpCookie impCookie;
    private String userID;
    private String ticket;

    public void init()
    {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        printCookies();
    }

    public String checkAlfrescoSAMLSettings()
    {
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(HelloAlfrescoController.ALFRESCO_ENABLED);
        Builder request = resource.request();
        request.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
        {
            String readEntity = response.readEntity(String.class);
            info = new AlfrescoSAMLEnabledInfo();
            System.out.println(readEntity);

            info.initFromJsonString(readEntity);

            System.out.println(info.toString());
            return info.toString();
        }
        else
        {
            String errorMessage = "ERROR! " + response.getStatus() + response.getEntity();
            System.out.println(errorMessage);
            return errorMessage;
        }
    }

    public void parseTicket(String pageText)
    {
        
    //    {
    //        "entry": {
    //                "id": "TICKET_1a2a825683a36731fbb497569ecb2fd6fc1313e5",
    //            "userId": "userA"
    //        }
    //}
        // parse the userID and Ticket
        userID = pageText.substring(pageText.indexOf("userId") + "userId".length() + 2).replaceAll("}", "").replaceAll("\"", "").trim();
        int start = pageText.indexOf("\"id\":") + "\"id\":".length() + 2;
        ticket = pageText.substring(start, pageText.indexOf(',', start)).replaceAll("\"", "").trim();
    }

    /**
     * WARRING: has side effects Keep in mind that the configured "rest-api" (ALFRESCO_SP_ID) does not set a session by
     * default
     */
    public void printCookies()
    {
        CookieStore cookieJar = cookieManager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();

        for (HttpCookie ck : cookies)
        {
            if (ck.getName().equals("JSESSIONID"))
            {
                String jsessionIDMessage = "JSESSIONID" + " " + ck.getName() + " " + ck.getValue() + " " + ck.getPath() + " " + ck.getDomain();
                System.out.println(jsessionIDMessage);
                if (ck.getDomain().contains("localhost") && ck.getPath().contains("alfresco"))
                {
                    // this may not be found if ALFRESCO_SP_ID is not configured to establish a session
                    impCookie = ck;
                }
            }
            else
            {
                ck.setMaxAge(0);
                System.out.println("CookieHandler retrieved cookie: " + ck);
            }
        }
        System.out.println("done printing all cookies.");
    }

    public String removeAllCookies()
    {
        CookieStore cookieJar = cookieManager.getCookieStore();
        cookieJar.removeAll();
        printCookies();
        return "ALL coookies are cleared!";
    }

    public String addGoodCookie()
    {
        CookieStore cookieJar = cookieManager.getCookieStore();
        cookieJar.removeAll();
        printCookies();
        if (impCookie == null)
        {
            return getGenericSessionErrorMessage();
        }
        HttpCookie cookie = new HttpCookie("JSESSIONID", impCookie.getValue());
        cookie.setDomain(impCookie.getDomain());
        cookie.setPath(impCookie.getPath());
        try
        {
            cookieJar.add(new URI("http://localhost:8080"), cookie);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        return "added good cookie: JSESSIONID " + cookie.getValue() + "\n";
    }

    public String getGenericSessionErrorMessage()
    {
        return "Could not add a cookie.\n" + "The SP used: " + ALFRESCO_SP_ID + " might be configured to not establish a session.\n"
                + "See saml.sp.outcome.establishSession property in JMX\n";
    }

    public String listAllSites()
    {
        String URLtoCheck = ALFRESCO_SERVER + "/s/api/sites";
        // uncomment the following line if you want to use the ticket
         URLtoCheck += "?alf_ticket=" + getTicket();
        return executeQueryJSON(URLtoCheck);

    }

    public String getAFile()
    {
        String URLtoGet = ALFRESCO_SERVER + "/s/api/node/workspace/SpacesStore/" + FILE_ID + "/content";

        return executeQueryPlainText(URLtoGet);
    }

    public String getAFileWithTicket()
    {

        String URLtoGet = ALFRESCO_SERVER + "/s/api/node/workspace/SpacesStore/" + FILE_ID + "/content";
        URLtoGet += "?alf_ticket=" + getTicket();

        return executeQueryPlainText(URLtoGet);
    }

    public String getAFileWithCMISWithTicket()
    {
        printCookies();
        // for rest v0 API
        // String URLtoGet = ALFRESCO_SERVER + "/api/-default-/public/cmis/versions/1.1/atom/content?id=" + FILE_ID;

        String URLtoGet = ALFRESCO_SERVER + "/api/-default-/public/cmis/versions/1.1/browser/root?objectId=" + FILE_ID
                + "%3B1.0&cmisselector=content";
        URLtoGet += "&alf_ticket=" + getTicket();

        return executeQueryPlainText(URLtoGet);
    }

    public String getAFileWithCMIS()
    {
        printCookies();

        String URLtoGet = ALFRESCO_SERVER + "/api/-default-/public/cmis/versions/1.1/browser/root?objectId=" + FILE_ID
                + "%3B1.0&cmisselector=content";

        return executeQueryPlainText(URLtoGet);
    }

    public AlfrescoSAMLEnabledInfo getInfo()
    {
        return info;
    }

    public HttpCookie getImpCookie()
    {
        return impCookie;
    }

    public String getUserID()
    {
        return userID;
    }

    public String getTicket()
    {
        return ticket;
    }

    protected String extractStringFromResponse(Response response)
    {
        System.out.println("Success! " + response.getStatus());
        System.out.println(response.getEntity());
        String readEntity = response.readEntity(String.class);
        System.out.println(readEntity);
        return readEntity;
    }

    protected String buildErrorResponseMessage(String URLtoGet, Response response)
    {
        System.out.println("ERROR! " + response.getStatus());
        System.out.println(response.getEntity());
        return URLtoGet + ":\n" + "ERROR! " + response.getStatus() + " " + response.getEntity() + "\n";
    }

    protected String executeQueryPlainText(String URLtoGet)
    {
        return executeQueryGeneric(URLtoGet, javax.ws.rs.core.MediaType.TEXT_PLAIN);
    }

    protected String executeQueryJSON(String URLtoGet)
    {
        return executeQueryGeneric(URLtoGet, javax.ws.rs.core.MediaType.APPLICATION_JSON);
    }

    protected String executeQueryGeneric(String URLtoCheck, String mediaType)
    {
        System.out.println("Executing call: " + URLtoCheck + " of type: " + mediaType);
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(URLtoCheck);
        Builder request = resource.request();
        request.accept(mediaType);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
        {
            String readEntity = extractStringFromResponse(response);
            return URLtoCheck + ":\n" + readEntity + "\n";
        }
        return buildErrorResponseMessage(URLtoCheck, response);
    }

}
