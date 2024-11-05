package processors.auth;

import Util.JwtUtil;
import Util.ObjectUtil;
import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.Processor;

import java.util.Optional;


public class JwtAuthProcessor implements Processor {
    private Processor nextProcessor;
    public JwtAuthProcessor() {

    }
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
