package processors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 空处理器 用于作为处理器责任链模式的头节点
 */
public class EmptyProcessor implements Processor {
    private Processor nextProcessor;
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
