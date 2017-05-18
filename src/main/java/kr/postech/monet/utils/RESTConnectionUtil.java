package kr.postech.monet.utils;

import com.eclipsesource.json.JsonObject;
import kr.postech.monet.config.bean.VMBean;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by woojoong on 2017-05-18.
 */
public class RESTConnectionUtil {

    public String getRESTToSingleVM (VMBean targetVM, String url) {
        String resultJsonString = null;

        // Connection through REST API
        Client client = ClientBuilder.newClient();
        client.register(HttpAuthenticationFeature.basic(targetVM.getONOSID(), targetVM.getONOSPW()));
        WebTarget target = client.target(url);

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        Response response = builder.get();

        // Does REST server working?
        if (response.getStatus() != 200) {
            System.out.println("REST Connection Error - VM: " + targetVM.getVmAlias() + " (Status: " + response.getStatus() + ")");
            return null;
        }

        resultJsonString = builder.get(String.class);

        return resultJsonString;
    }

    public void putRESTToSingleVM (VMBean targetVM, String url, JsonObject parameterJson) {
        // Connection through REST API
        Client client = ClientBuilder.newClient();
        client.register(HttpAuthenticationFeature.basic(targetVM.getONOSID(), targetVM.getONOSPW()));
        WebTarget target = client.target(url);

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        Response response = builder.put(Entity.entity(parameterJson.toString(), MediaType.APPLICATION_JSON));

        // Does REST server working?
        if (response.getStatus() != 200) {
            System.out.println("REST Connection Error - VM: " + targetVM.getVmAlias() + " (Status: " + response.getStatus() + ")");
            return;
        }
    }


}
