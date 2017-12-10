package Controller;

public class Controller {
    private static Controller ourInstance = new Controller();

    public static Controller getInstance() {
        return ourInstance;
    }

    private Controller() {
    }

    public static void main (String[] args) {
        System.out.println("compile test");
    }
}
