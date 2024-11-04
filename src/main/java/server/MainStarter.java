package server;

public class MainStarter {
    public static void main(String[] args) throws InterruptedException {
        Starter serverStarter = new Starter();
        serverStarter.start(8080);
    }
}
