package Utils.Connection;

import Beans.Bean;
import Beans.ControllerBean;
import com.eclipsesource.json.JsonObject;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RESTConnection extends AbstractConnection implements Connection {
    public RESTConnection() {
    }

    @Override
    public String sendCommandToUser(Bean targetMachine, String cmd) {
        WebTarget target = connectREST(targetMachine, cmd.replace("<controllerIP>", targetMachine.getIpAddr())
                .replace("<controllerPort>", ((ControllerBean) targetMachine).getRestPort())
                .replace("<controllerID>", ((ControllerBean) targetMachine).getControllerId()));

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);

        //try catch: for debugging
        try {
            return builder.get(String.class);
        } catch (NotFoundException e) {
            System.out.println("********");
            System.out.println(cmd);
            System.out.println(((ControllerBean) targetMachine).getControllerId());
            e.printStackTrace();
        }

        return null;
    }

    public void putCommandToUser(Bean targetMachine, String cmd, JsonObject json) {
        WebTarget target = connectREST(targetMachine, cmd.replace("<controllerIP>", targetMachine.getIpAddr())
                .replace("<controllerPort>", ((ControllerBean) targetMachine).getRestPort())
                .replace("<controllerID>", ((ControllerBean) targetMachine).getControllerId()));

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);

        Response response = builder.put(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
    }

    @Override
    public String sendCommandToRoot(Bean targetMachine, String cmd) {
        System.out.println("Warning: sendCommandToUser and sendCommandToRoot are the same function. Please use the former one");
        return sendCommandToUser(targetMachine, cmd);
    }

    public WebTarget connectREST(Bean targetMachine, String url) {
        Client client = ClientBuilder.newClient();
        client.register(HttpAuthenticationFeature.basic(((ControllerBean) targetMachine).getControllerGuiId(), ((ControllerBean) targetMachine).getControllerGuiPw()));
        return client.target(url);
    }
}
