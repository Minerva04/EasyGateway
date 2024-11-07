package processors.router.loadbalance;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

public interface LoadBalance {
    String select(String prefix,List<String> serverList, FullHttpRequest request);
}
