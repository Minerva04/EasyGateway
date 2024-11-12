package server;

import Util.JwtUtil;

public class MainStarter {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(JwtUtil.createToken("secret1","admin"));
        ConfigReader configReader = new ConfigReader();
        configReader.start();
        NettyStarter serverStarter = new NettyStarter();
        serverStarter.start(1001);
    }
}
