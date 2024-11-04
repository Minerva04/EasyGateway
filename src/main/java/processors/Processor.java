package processors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface Processor {

    void process(ChannelHandlerContext ctx, FullHttpRequest request);
    void setNext(Processor processor);
}
