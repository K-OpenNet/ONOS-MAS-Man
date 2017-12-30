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
