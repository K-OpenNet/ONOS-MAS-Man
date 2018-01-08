package Monitor;

abstract class AbstractMonitor implements Monitor {
    protected monitorType monitorName;

    public monitorType getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(monitorType monitorName) {
        this.monitorName = monitorName;
    }
}

class SSHRetryExceedException extends RuntimeException {
    public SSHRetryExceedException() {
        super();
    }

    public SSHRetryExceedException(String message) {
        super(message);
    }
}
