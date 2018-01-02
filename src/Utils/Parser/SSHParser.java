package Utils.Parser;

import Beans.ControllerBean;
import Database.Tuples.ComputingResourceTuple;

public class SSHParser extends AbstractParser implements Parser {
    public SSHParser() {
        parserName = parserType.SSH;
    }

    public ComputingResourceTuple parseComputingResourceMonitoringResults (String rawResult, ControllerBean controller) {

        ComputingResourceTuple result = new ComputingResourceTuple();

        return result;

    }
}
