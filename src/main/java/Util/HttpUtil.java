package Util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.HttpStatueCode;
import common.ResponseMsg;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpUtil {
    public FullHttpResponse createHttpResponse(FullHttpRequest request,HttpResponseStatus status) {
        // 创建响应对象
        ResponseMsg responseObject = new ResponseMsg(status.reasonPhrase(), status.code());

        // 将对象序列化为 JSON 字符串
        String responseBody = JSON.toJSONString(responseObject);

        FullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseBody, StandardCharsets.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }
    public FullHttpResponse createHttpResponse(FullHttpRequest request,String responseBody) {
        // 创建响应对象


        FullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseBody, StandardCharsets.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    public void sendResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
        ctx.writeAndFlush(response).addListener(future -> {
            if (!future.isSuccess()) {
                // 处理发送失败
                future.cause().printStackTrace();
            }
        });
    }
}
