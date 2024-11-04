package processors;

import Util.HttpUtil;
import Util.JwtUtil;
import Util.ObjectUtil;
import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Optional;


public class AuthProcessor implements Processor {
    private Processor nextProcessor;
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {
        String authorization = request.headers().get("Authorization");
        if(ObjectUtil.isEmpty(authorization)|| !JwtUtil.validateToken(authorization)){
            HttpUtil httpUtil = new HttpUtil();
            FullHttpResponse response = httpUtil.createHttpResponse(request, HttpStatueCode.AUTH_ERROR);
            httpUtil.sendResponse(ctx, response);
            return;
        }
       Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
