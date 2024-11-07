package processors.router.loadbalance;

import Util.ObjectUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.router.ConfigRouter;
import server.ConfigReader;

import java.util.*;

/**
 * 一致性hash负载均衡策略  hash环通过sortedMap实现，sortedMap可以保证key的顺序性
 */
public class HashLoadBalance implements LoadBalance {


    private final Integer VIRTUAL_NODE=30;
    private final Integer GRAY_VIRTUAL_NODE=5;

    private Map<String,SortedMap<Integer, String>> circleMap = new HashMap<>();
    public HashLoadBalance() {
        /**
         * 构建不同前缀对应不同的哈希环
         */
        Map<String, List<String>> routerMap = ConfigReader.getRouterMap("routerMap");
        Map<String, List<String>> grayRouterMap = ConfigReader.getRouterMap("grayRouterMap");
        for (Map.Entry<String, List<String>> entry : routerMap.entrySet()) {
            String prefix = entry.getKey();
            List<String> serverList = entry.getValue();
            List<String> grayRouterList = grayRouterMap.get(prefix);
            circleMap.put(prefix,getCircle(serverList,grayRouterList));
        }
    }
    @Override
    public String select(String prefix,List<String> serverList, FullHttpRequest request) {
        if (ObjectUtil.isEmpty(serverList)) {
            return null;
        }

        //TODO 测试阶段先注释 由系统生成随机ip
        //String ip = request.headers().get("X-Forwarded-For");
        Random random = new Random();
        String ip = random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
        if(ObjectUtil.isEmpty(ip)){
            throw new RuntimeException("ip is null");
        }
        int requestHash = getHash(ip);

        // 获取哈希环中大于等于请求哈希值的map
        SortedMap<Integer, String> tailMap = circleMap.get(prefix).tailMap(requestHash);

        // 如果没有找到合适的节点（tailMap为空），则返回哈希环中的第一个节点
        int nodeHash = tailMap.isEmpty() ? circleMap.get(prefix).firstKey() : tailMap.firstKey();

        // 返回选中的节点
        return circleMap.get(prefix).get(nodeHash);
    }
    private SortedMap<Integer, String>getCircle(List<String> serverList,List<String> grayList){
        SortedMap<Integer, String> circle = new TreeMap<>();
        // 为每个服务器节点添加多个虚拟节点到哈希环
        for (String server : serverList) {
            for (int i = 0; i < VIRTUAL_NODE; i++) {
                String virtualNode = server + "-" + i;
                int hash = getHash(virtualNode);
                circle.put(hash, server);
            }
        }
        if(!ObjectUtil.isEmpty(grayList)){
            for (String server : grayList) {
                for (int i = 0; i < GRAY_VIRTUAL_NODE; i++) {
                    String virtualNode = server + "-" + i;
                    int hash = getHash(virtualNode);
                    circle.put(hash, server);
                }
            }
        }

        return circle;
    }

    private int getHash(String virtualNode) {
        //保证hashcode为正数
        return virtualNode.hashCode() & 0x7fffffff;
    }
}
