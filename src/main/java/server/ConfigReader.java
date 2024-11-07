package server;

import java.io.*;
import java.util.*;

public class ConfigReader {

    //TODO 动态读取
    //private static String FILE_PATH =new File("config.txt").getAbsolutePath();
    public static String FILE_PATH = "D:\\project\\IdeaProjects\\EasyGateway\\src\\main\\resources\\config.txt";
    /**
     * 采用LinkedHashMap保证插入顺序 这样之后如果要修改处理器的执行顺序只需要修该配置文件的顺序就可以
     */
    private static Map<String, Map<String, String>> configMap = new LinkedHashMap<>();

    private void loadConfig(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentSection = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.contains(":")) {
                    String[] section = line.split(":");
                    currentSection = section[0].trim();
                    line = section[1].trim();
                    configMap.put(currentSection, new HashMap<>());
                }
                if (currentSection != null) {
                    String[] pairs = line.split("&");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            configMap.get(currentSection).compute(key, (k, v) -> (v == null ? value : v + "&" + value));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        loadConfig(FILE_PATH);
        ProcessorsBuilder.initProcessor(configMap);
    }

    public static Map<String, Map<String, String>> getConfigMap() {
        return configMap;
    }
    public static Map<String, List<String>>getRouterMap(String name){
        Map<String, String> routerMap = configMap.get(name);
        Map<String,List<String>>res=new HashMap<>();
        for (Map.Entry<String, String> entry : routerMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().replace("-", ":");
            String[] split = value.split("&");
            List<String> strings = Arrays.asList(split);
            res.put(key, strings);
        }
        return res;
    }
    public static Map<String, String>getSecretMap(){
       return configMap.get("secretMap");
    }
}
