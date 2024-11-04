package processors;

import Util.HttpUtil;
import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public interface Processor {

    void process(ChannelHandlerContext ctx, FullHttpRequest request);
    void setNext(Processor processor);
    default void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        HttpUtil httpUtil = new HttpUtil();
        FullHttpResponse response = httpUtil.createHttpResponse(request, status);
        httpUtil.sendResponse(ctx, response);
    }
}
