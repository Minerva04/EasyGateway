package server;

import handler.NettyServerHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyStarter {
    private EventLoopGroup workerGroup=new NioEventLoopGroup();
    private EventLoopGroup bossGroup=new NioEventLoopGroup();
    private Channel channel;
    private static final Logger logger = LoggerFactory.getLogger(NettyStarter.class);

    public void start(int port) throws InterruptedException {

        ServerBootstrap serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .option(ChannelOption.SO_BACKLOG, 1024) //  服务端 accept 队列的大小
                .childOption(ChannelOption.SO_KEEPALIVE, true) //  TCP Keepalive 机制，实现 TCP 层级的心跳保活功能
                .childOption(ChannelOption.TCP_NODELAY, true)//  允许较小的数据包的发送，降低延迟
                .childHandler(new NettyServerHandlerInitializer());

        ChannelFuture future = serverBootstrap.bind().sync();
        if(future.isSuccess()){
            channel= future.channel();
            logger.info("server start success 端口:"+port);
            channel.closeFuture().sync();
        }

    }
}
