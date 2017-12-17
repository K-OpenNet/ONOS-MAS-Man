package Utils.Connection;

import Beans.Bean;

public interface Connection {
    enum connectionType {
        SSH, REST;
    }

    String sendCommandToUser(Bean targetMachine, String cmd);
    String sendCommandToRoot(Bean targetMachine, String cmd);
    connectionType getConnectionName();
}
