package common;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpStatueCode {
   public static final HttpResponseStatus SUCCESS = newStatus(200, "OK");
    public static final HttpResponseStatus ERROR = newStatus(500, "服务器异常");
    public static final HttpResponseStatus AUTH_ERROR = newStatus(401, "鉴权失败");

    public static final HttpResponseStatus LIMIT_ERROR = newStatus(402, "当前服务器访问人数过多");
    public static final HttpResponseStatus FLUSH_ERROR = newStatus(403, "访问频率过高 请稍后在试");

    public static final HttpResponseStatus ROUTER_ERROR = newStatus(404, "访问资源不存在");
    public static final HttpResponseStatus REQUEST_METHOD_ERROR = newStatus(405, "请求方法不支持");


    private static HttpResponseStatus newStatus(int statusCode, String reasonPhrase) {
        return new HttpResponseStatus(statusCode, reasonPhrase);
    }
}
