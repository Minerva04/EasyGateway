package handler;


import Util.HttpUtil;
import common.HttpStatueCode;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;


import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import processors.Processor;
import server.ProcessorsBuilder;

import java.io.IOException;
import java.io.InputStream;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 解析请求
        // 获取请求方法和 URI
        HttpMethod method = request.method();
        HttpUtil httpUtil = new HttpUtil();
        Processor firstProcessor = ProcessorsBuilder.getProcessor();
        firstProcessor.process(ctx, request);
        String uri = request.uri();
        HttpClient httpClient = new HttpClient();
        org.apache.commons.httpclient.HttpMethod httpMethod;

        switch (method.name()) {
            case "GET":
                httpMethod = handleGet(uri, request);
                break;
            case "POST":
                httpMethod = handlePost(uri, request);
                break;
            case "PUT":
                httpMethod = handlePut(uri, request);
                break;
            default:
                FullHttpResponse response = httpUtil.createHttpResponse(request, HttpStatueCode.REQUEST_METHOD_ERROR);
                httpUtil.sendResponse(ctx, response);
                return;
        }

        try  {
            int statusCode = httpClient.executeMethod(httpMethod);
            InputStream responseStream = httpMethod.getResponseBodyAsStream();
            ByteBuf byteBuf = ctx.alloc().buffer();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = responseStream.read(buffer)) != -1) {
                byteBuf.writeBytes(buffer, 0, bytesRead);
            }
            String s = byteBuf.toString(CharsetUtil.UTF_8);
            FullHttpResponse httpResponse = httpUtil.createHttpResponse(request, s);

            org.apache.commons.httpclient.Header[] headers = httpMethod.getResponseHeaders();
            for (org.apache.commons.httpclient.Header header : headers) {
                httpResponse.headers().set(header.getName(), header.getValue());
            }

            httpUtil.sendResponse(ctx, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
            FullHttpResponse errorResponse = httpUtil.createHttpResponse(request, HttpStatueCode.ERROR);
            httpUtil.sendResponse(ctx, errorResponse);
        } finally {
            httpMethod.releaseConnection();
        }
    }


    // 处理 GET 请求
    private org.apache.commons.httpclient.HttpMethod handleGet(String uri, FullHttpRequest request) {
        GetMethod getMethod = new GetMethod(uri);
        request.headers().forEach(header -> getMethod.setRequestHeader(header.getKey(), header.getValue()));
        return getMethod;
    }

    // 处理 POST 请求
    private org.apache.commons.httpclient.HttpMethod handlePost(String uri, FullHttpRequest request) throws Exception {
        PostMethod postMethod = new PostMethod(uri);
        request.headers().forEach(header -> postMethod.setRequestHeader(header.getKey(), header.getValue()));
        String content = request.content().toString(CharsetUtil.UTF_8);
        postMethod.setRequestEntity(new StringRequestEntity(content, "application/json", "UTF-8"));
        return postMethod;
    }

    // 处理 PUT 请求
    private org.apache.commons.httpclient.HttpMethod handlePut(String uri, FullHttpRequest request) throws Exception {
        PutMethod putMethod = new PutMethod(uri);
        putMethod.setPath(uri);
        request.headers().forEach(header -> putMethod.setRequestHeader(header.getKey(), header.getValue()));
        String content = request.content().toString(CharsetUtil.UTF_8);
        putMethod.setRequestEntity(new StringRequestEntity(content, "application/json", "UTF-8"));
        return putMethod;
    }

}




