package Utils.Connection;

import Beans.Bean;

public interface Connection {
    enum connectionType {
        SSH, REST;
    }

    String sendCommand(Bean targetMachine);
}
