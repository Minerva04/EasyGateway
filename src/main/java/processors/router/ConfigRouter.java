package processors.router;

import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import processors.Processor;
import server.ConfigReader;

import java.util.*;

public class ConfigRouter implements Processor {
    private Processor nextProcessor;
    private Map<String, List<String>>routerMap;
    public ConfigRouter() {
        routerMap = ConfigReader.getRouterMap();
    }
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

        String path = request.uri();

        Set<String> prefix = routerMap.keySet();
        for (String s : prefix) {
            if (path.startsWith(s)) {
                path = path.substring(s.length());
                //TODO 根据负载均衡策略选择不同的主机
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