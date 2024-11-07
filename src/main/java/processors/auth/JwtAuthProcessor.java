package processors.auth;

import Util.JwtUtil;
import Util.ObjectUtil;
import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.Processor;
import server.ConfigReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class JwtAuthProcessor implements Processor {
    private Processor nextProcessor;

    private Map<String, String> secretKeyMap;

    public JwtAuthProcessor() {
        secretKeyMap = ConfigReader.getSecretMap();
    }

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {
        String authorization = request.headers().get("Authorization");
        String uri = request.uri();
        Set<String> prefixSet = secretKeyMap.keySet();
        for (String prefix : prefixSet) {
            if (uri.startsWith(prefix)) {
                try {
                    if(ObjectUtil.isEmpty(authorization)|| !JwtUtil.validateToken(secretKeyMap.get(prefix),authorization)){
                        throw new Exception();
                    }
                }catch (Exception e){
                    sendHttpResponse(ctx, request, HttpStatueCode.AUTH_ERROR);
                    return;
                }
                break;
            }
        }
        /**
         * validateToken抛出异常或者验证失败则返回
         */
        Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
