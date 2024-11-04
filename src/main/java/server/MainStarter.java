package server;

import Util.JwtUtil;

public class MainStarter {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(JwtUtil.createToken("admin"));
        Starter serverStarter = new Starter();
        serverStarter.start(8080);
    }
}
