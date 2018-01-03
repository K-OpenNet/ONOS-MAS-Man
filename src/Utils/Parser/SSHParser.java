package Utils.Parser;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tuples.ComputingResourceTuple;

import java.util.HashMap;

public class SSHParser extends AbstractParser implements Parser {
    public SSHParser() {
        parserName = parserType.SSH;
    }

    public void parseComputingResourceMonitoringResults (String rawResults, PMBean targetPM, HashMap<String, ComputingResourceTuple> result) {

        String[] splittedResults = rawResults.split("\n");

        for (int index1 = 5; index1 < splittedResults.length; index1++) {
            String[] splittedResultsForEachLine = splittedResults[index1].split("\\s+");

            ControllerBean controller = Configuration.getInstance().getControllerBean(splittedResultsForEachLine[0]);

            if (!controller.isVmAlive()) {
                continue;
            }

            if (!result.containsKey(controller.getBeanKey())) {
                throw new ComputingResourceSanityException();
            }

            ComputingResourceTuple tmpTuple = result.get(controller.getBeanKey());

            switch(splittedResultsForEachLine[1]) {
                case "CPU/Load/User":
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2++) {
                        String tmpResult = splittedResultsForEachLine[index2].replace(",", "").replace("%", "");
                        tmpTuple.getCpuUsageUser().add(Double.valueOf(tmpResult));
                    }
                    break;
                case "CPU/Load/Kernel":
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2++) {
                        String tmpResult = splittedResultsForEachLine[index2].replace(",", "").replace("%", "");
                        tmpTuple.getCpuUsageKernel().add(Double.valueOf(tmpResult));
                    }
                    break;
                case "RAM/Usage/Used":
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2 = index2 + 2) {
                        String tmpResult = splittedResultsForEachLine[index2];
                        tmpTuple.getRamUsage().add(Integer.valueOf(tmpResult));
                    }
                    break;
                case "Net/Rate/Rx":
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2 = index2 + 2) {
                        String tmpResult = splittedResultsForEachLine[index2];
                        tmpTuple.getNetRx().add(Integer.valueOf(tmpResult));
                    }
                    break;
                case "Net/Rate/Tx":
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2 = index2 + 2) {
                        String tmpResult = splittedResultsForEachLine[index2];
                        tmpTuple.getNetTx().add(Integer.valueOf(tmpResult));
                    }
                    break;
                default:
                    throw new WrongMetricIndexException();
            }

        }

    }
}

class ComputingResourceSanityException extends RuntimeException {
    public ComputingResourceSanityException() {
        super();
    }

    public ComputingResourceSanityException(String message) {
        super(message);
    }
}

class WrongMetricIndexException extends RuntimeException {
    public WrongMetricIndexException() {
        super();
    }

    public WrongMetricIndexException(String message) {
        super(message);
    }
}