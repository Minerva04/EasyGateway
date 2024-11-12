package handler;


import Util.HttpUtil;
import common.HttpStatueCode;

import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;


import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import processors.Processor;
import server.ProcessorsBuilder;

import java.io.IOException;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    private static final CloseableHttpClient httpClient;

    static {
        connectionManager.setMaxTotal(10000);  // 设置最大连接数
        connectionManager.setDefaultMaxPerRoute(20);  // 设置每个路由的最大连接数
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        Processor processor = ProcessorsBuilder.getProcessor();
        processor.process(ctx, request);
        HttpUriRequest httpMethod;
        HttpMethod method = request.method();
        HttpUtil httpUtil = new HttpUtil();
        switch (method.name()) {
            case "GET":
                httpMethod = handleGet(request.uri(), request);
                break;
            case "POST":
                httpMethod = handlePost(request.uri(), request);
                break;
            case "PUT":
                httpMethod = handlePut(request.uri(), request);
                break;
            default:
                FullHttpResponse response = httpUtil.createHttpResponse(request, HttpStatueCode.REQUEST_METHOD_ERROR);
                httpUtil.sendResponse(ctx, response);
                return;
        }

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpMethod)) {
            String responseString = EntityUtils.toString(httpResponse.getEntity(), CharsetUtil.UTF_8);
            FullHttpResponse nettyResponse = httpUtil.createHttpResponse(request, responseString);

            for (org.apache.http.Header header : httpResponse.getAllHeaders()) {
                nettyResponse.headers().set(header.getName(), header.getValue());
            }

            httpUtil.sendResponse(ctx, nettyResponse);
        } catch (IOException e) {
            e.printStackTrace();
            FullHttpResponse errorResponse = httpUtil.createHttpResponse(request, HttpStatueCode.ERROR);
            httpUtil.sendResponse(ctx, errorResponse);
        }
    }


    private HttpUriRequest handleGet(String uri, FullHttpRequest request) {
        HttpGet getMethod = new HttpGet(uri);
        request.headers().forEach(header -> getMethod.setHeader(header.getKey(), header.getValue()));
        return getMethod;
    }

    private HttpUriRequest handlePost(String uri, FullHttpRequest request) throws Exception {
        HttpPost postMethod = new HttpPost(uri);
        request.headers().forEach(header -> postMethod.setHeader(header.getKey(), header.getValue()));
        String content = request.content().toString(CharsetUtil.UTF_8);
        postMethod.setEntity(new StringEntity(content, "UTF-8"));
        return postMethod;
    }

    private HttpUriRequest handlePut(String uri, FullHttpRequest request) throws Exception {
        HttpPut putMethod = new HttpPut(uri);
        request.headers().forEach(header -> putMethod.setHeader(header.getKey(), header.getValue()));
        String content = request.content().toString(CharsetUtil.UTF_8);
        putMethod.setEntity(new StringEntity(content, "UTF-8"));
        return putMethod;
    }

}




