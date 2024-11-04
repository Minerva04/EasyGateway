package common;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpStatueCode {
   public static final HttpResponseStatus SUCCESS = newStatus(200, "OK");
    public static final HttpResponseStatus AUTH_ERROR = newStatus(401, "鉴权失败");


    private static HttpResponseStatus newStatus(int statusCode, String reasonPhrase) {
        return new HttpResponseStatus(statusCode, reasonPhrase);
    }
}
