package handler;


import Util.HttpUtil;
import common.HttpStatueCode;

import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;


import io.netty.handler.codec.http.*;
import processors.Processor;
import server.ProcessorsBuilder;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 解析请求
        // 获取请求方法和 URI
        HttpMethod method = request.method();
        System.out.println(request.uri());
        Processor firstProcessor = ProcessorsBuilder.getProcessor();
        firstProcessor.process(ctx, request);
        HttpUtil httpUtil = new HttpUtil();
        FullHttpResponse httpResponse = httpUtil.createHttpResponse(request, HttpStatueCode.SUCCESS);
        httpUtil.sendResponse(ctx, httpResponse);

        //TODO 这里可以添加鉴权、限流等逻辑
        // TODO 转发请求到下游服务


        // TODO 构建响应
        /*FullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(downstreamResponse, CharsetUtil.UTF_8)
        );*/

        // 发送响应

    }
}
