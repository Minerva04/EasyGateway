package processors.router.loadbalance;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡策略
 */
public class RandomLoadBalance implements LoadBalance {
    private final Random random = new Random();
    @Override
    public String select(String prefix,List<String> serverList, FullHttpRequest request) {
        int index = random.nextInt(serverList.size());
        return serverList.get(index);
    }
}
