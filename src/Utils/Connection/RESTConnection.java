package Utils.Connection;

import Beans.Bean;

public class RESTConnection extends AbstractConnection implements Connection {
    public RESTConnection() {
    }

    @Override
    public String sendCommandToUser(Bean targetMachine, String cmd) {
        return null;
    }

    @Override
    public String sendCommandToRoot(Bean targetMachine, String cmd) {
        return sendCommandToUser(targetMachine, cmd);
    }
}
