package Utils.Parser;

import Beans.ControllerBean;
import Beans.PMBean;
import Database.Configure.Configuration;
import Database.Tuples.ComputingResourceTuple;

import java.util.ArrayList;
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
                    if (splittedResultsForEachLine.length == 2) {
                        tmpTuple.getCpuUsageUser().add(Double.valueOf(0.0));
                        break;
                    }
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2++) {
                        String tmpResult = splittedResultsForEachLine[index2].replace(",", "").replace("%", "");
                        tmpTuple.getCpuUsageUser().add(Double.valueOf(tmpResult));
                    }
                    break;
                case "CPU/Load/Kernel":
                    if (splittedResultsForEachLine.length == 2) {
                        tmpTuple.getCpuUsageKernel().add(Double.valueOf(0.0));
                        break;
                    }
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2++) {
                        String tmpResult = splittedResultsForEachLine[index2].replace(",", "").replace("%", "");
                        tmpTuple.getCpuUsageKernel().add(Double.valueOf(tmpResult));
                    }
                    break;
                case "RAM/Usage/Used":
                    if (splittedResultsForEachLine.length == 2) {
                        tmpTuple.getRamUsage().add(Integer.valueOf(0));
                        break;
                    }
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2 = index2 + 2) {
                        String tmpResult = splittedResultsForEachLine[index2];
                        tmpTuple.getRamUsage().add(Integer.valueOf(tmpResult));
                    }
                    break;
                case "Net/Rate/Rx":
                    if (splittedResultsForEachLine.length == 2) {
                        tmpTuple.getNetRx().add(Integer.valueOf(0));
                        break;
                    }
                    for(int index2 = 2; index2 < splittedResultsForEachLine.length; index2 = index2 + 2) {
                        String tmpResult = splittedResultsForEachLine[index2];
                        tmpTuple.getNetRx().add(Integer.valueOf(tmpResult));
                    }
                    break;
                case "Net/Rate/Tx":
                    if (splittedResultsForEachLine.length == 2) {
                        tmpTuple.getNetTx().add(Integer.valueOf(0));
                        break;
                    }
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
    public int[] parseCPUBitmap (String tmpRawResults, int[] results) {
        
        String rawResults = tmpRawResults.split("\\s+")[3];

        ArrayList<Integer> tmpResults = parseCPUBitmapFromVM(rawResults);

        for (int elemResult : tmpResults) {
            results[elemResult] = 1;
        }

        return results;
    }

    public ArrayList<Integer> parseCPUBitmapFromVM (String rawResult) {

        ArrayList<Integer> results = new ArrayList<>();

        String[] arrayResults = rawResult.split(",");

        for (String elemResult : arrayResults) {
            if (elemResult.contains("-")) {
                String[] tmpResults = elemResult.split("-");
                int start = Integer.valueOf(tmpResults[0]);
                int end = Integer.valueOf(tmpResults[1]);

                for (int index = start; index <= end; index++) {
                    results.add(index);
                }

            } else {
                results.add(Integer.valueOf(elemResult));
            }

        }

        return results;
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