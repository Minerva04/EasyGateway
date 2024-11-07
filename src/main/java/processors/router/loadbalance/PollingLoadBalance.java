package processors.router.loadbalance;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 */
public class PollingLoadBalance implements LoadBalance {
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public String select(String prefix,List<String> serverList, FullHttpRequest request) {
        int index = currentIndex.getAndUpdate(current -> (current + 1) % serverList.size());
        return serverList.get(index);
    }
}
