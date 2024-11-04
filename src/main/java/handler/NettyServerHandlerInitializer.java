package handler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;


public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {
    //超时时间
    private Integer READTIME_OUT=60*3;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                //空闲检测
                .addLast(new ReadTimeoutHandler(READTIME_OUT, TimeUnit.SECONDS))
                .addLast(new HttpServerCodec())
                .addLast( new HttpObjectAggregator(1024 * 1024))
                .addLast(new HttpRequestHandler());

    }
}
