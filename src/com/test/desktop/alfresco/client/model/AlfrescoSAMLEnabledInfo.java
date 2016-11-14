package com.test.desktop.alfresco.client.model;

public class AlfrescoSAMLEnabledInfo
{
    public boolean isSamlEnabled;
    public boolean isSamlEnforced;
    public String idpDescription;
    public String tenantDomain;

    @Override
    public String toString()
    {
        return "{ " + "isSamlEnabled: " + isSamlEnabled + "; " + "isSamlEnforced: " + isSamlEnforced + "; " + "idpDescription: " + idpDescription
                + "; " + "tenantDomain: " + tenantDomain + " }";
    }

    public void initFromJsonString(String jsonString)
    {
        // json format is :
        // {
        // "isSamlEnabled": true,
        // "isSamlEnforced": true,
        // "idpDescription": "PingFederateRepository",
        // "tenantDomain": "-default-"
        // }
        // there are better ways of doing this :)
        {
            int startIndex = jsonString.indexOf("isSamlEnabled") + "isSamlEnabled".length() + 2;
            String value = jsonString.substring(startIndex, jsonString.indexOf(',', startIndex));
            if (value.contains("true"))
            {
                this.isSamlEnabled = true;
            }
        }
        {
            int startIndex = jsonString.indexOf("isSamlEnforced") + "isSamlEnforced".length() + 2;
            String value = jsonString.substring(startIndex, jsonString.indexOf(',', startIndex));
            if (value.contains("true"))
            {
                this.isSamlEnforced = true;
            }

        }
        {
            int startIndex = jsonString.indexOf("idpDescription") + "idpDescription".length() + 2;
            String value = jsonString.substring(startIndex, jsonString.indexOf(',', startIndex));
            if (value.length() > 0)
            {
                this.idpDescription = value.replaceAll("\"", "").trim();
            }
        }
        {
            int startIndex = jsonString.indexOf("tenantDomain") + "tenantDomain".length() + 2;
            String value = jsonString.substring(startIndex, jsonString.indexOf('}', startIndex));
            if (value.length() > 0)
            {
                this.tenantDomain = value.replaceAll("\"", "").trim();
            }
        }
    }
}
