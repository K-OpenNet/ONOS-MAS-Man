package Utils.Connection;

abstract class AbstractConnection implements Connection {
    protected connectionType connectionName;

    public connectionType getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(connectionType connectionName) {
        this.connectionName = connectionName;
    }
}
