package processors.router;

import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import processors.Processor;
import processors.router.loadbalance.LoadBalance;
import server.ConfigReader;

import java.util.*;

public class ConfigRouter implements Processor {
    private Processor nextProcessor;
    private LoadBalance loadBalance;
    private Map<String, List<String>>routerMap;
    public ConfigRouter(LoadBalance loadBalance) {
        routerMap = ConfigReader.getRouterMap("routerMap");
        this.loadBalance = loadBalance;
    }
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

        String path = request.uri();

        Set<String> prefix = routerMap.keySet();
        for (String s : prefix) {
            if (path.startsWith(s)) {
                path = path.substring(s.length());
                String serverPath = loadBalance.select(s, routerMap.get(s), request);
                path="http://"+serverPath+path;
                request.setUri(path);
                Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
                return;
            }
        }
        sendHttpResponse(ctx, request, HttpStatueCode.ROUTER_ERROR);

    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
