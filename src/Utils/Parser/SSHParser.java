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

        for (int index = 6; index < splittedResults.length; index++) {
            String[] splittedResultsForEachLine = splittedResults[index].split("\\s+");

            if (!result.containsKey(splittedResultsForEachLine[0])) {
                throw new ComputingResourceSanityException();
            }

            // ToDo: implement parsing functon

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