package processors.limit;

import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.Processor;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器限流 通过后台定时线程每秒重置计数器
 * 放行速率较为平均 每秒放行一定量的请求
 */
public class CountLimit implements Processor {
    private Processor nextProcessor;
    private  final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
    private  AtomicInteger currentRequests = new AtomicInteger(0);

    private int maxRequests;
    public CountLimit(int maxRequests) {
        this.maxRequests = maxRequests;
        start();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            currentRequests.set(0); // 重置计数器
        }, 1, 1, TimeUnit.SECONDS); // 每1秒重置一次
    }

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

        if (currentRequests.incrementAndGet() > maxRequests) {
            sendHttpResponse(ctx, request, HttpStatueCode.LIMIT_ERROR);
        }
        Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
