package Database.Tables;

import java.util.ArrayList;

public class Database {
    private static Database ourInstance = new Database();
    private static ArrayList<State> database = new ArrayList<>();

    public static Database getInstance() {
        return ourInstance;
    }

    private Database() {
    }

    public State getAllTuples(int timeIndex) {
        return database.get(timeIndex);
    }

    // DB print functions
    // Overall
    // CPU sequentially (per sec, per timeslot)
    // Mem sequentially (per sec, per timeslot)
    // Net-RX sequentially (per sec, per timeslot)
    // Net-TX sequentially (per sec, per timeslot)
    // numSwitches sequentially (per sec, per timeslot)
    // numCPU sequentially (per sec, per timeslot)
    // numOFMsgs (per timeslot)
    // Bandwidth of OF msgs (per timeslot)


    public static Database getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(Database ourInstance) {
        Database.ourInstance = ourInstance;
    }

    public static ArrayList<State> getDatabase() {
        return database;
    }

    public static void setDatabase(ArrayList<State> database) {
        Database.database = database;
    }
}
