package common;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpStatueCode {
   public static final HttpResponseStatus SUCCESS = newStatus(200, "OK");
    public static final HttpResponseStatus ERROR = newStatus(500, "服务器异常");
    public static final HttpResponseStatus AUTH_ERROR = newStatus(401, "鉴权失败");

    public static final HttpResponseStatus LIMIT_ERROR = newStatus(402, "当前服务器访问人数过多");


    private static HttpResponseStatus newStatus(int statusCode, String reasonPhrase) {
        return new HttpResponseStatus(statusCode, reasonPhrase);
    }
}
