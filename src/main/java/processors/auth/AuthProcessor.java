package processors.auth;

import Util.HttpUtil;
import Util.JwtUtil;
import Util.ObjectUtil;
import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import processors.Processor;

import java.util.Optional;


public class AuthProcessor implements Processor {
    private Processor nextProcessor;
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {
        String authorization = request.headers().get("Authorization");
        /**
         * validateToken抛出异常或者验证失败则返回
         */
        try {
            if(ObjectUtil.isEmpty(authorization)|| !JwtUtil.validateToken(authorization)){
                throw new Exception();
            }
        }catch (Exception e){
          sendHttpResponse(ctx, request, HttpStatueCode.AUTH_ERROR);
        }
       Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
