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
 * 令牌桶算法限流器
 * 算法思想 后台线程每秒以一定速率补充令牌 获取到令牌的线程才能放行
 * 桶中可以存储一定量的令牌 允许一定的突发流量 但在令牌耗尽时也是以一定速率放行
 */
public class TokenBucketLimit implements Processor {
    private Processor nextProcessor;
    private final int capacity; // 桶的最大容量
    private final int refillRate; // 令牌生成速率（每秒生成的令牌数量）
    private AtomicInteger currentTokens = new AtomicInteger(0); // 当前桶中的令牌
    private  final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //TODO 通过配置文件读取
    public TokenBucketLimit(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        start();
    }

    private void start() {
        scheduler.scheduleAtFixedRate(() -> {
            int filledTokens = Math.min(capacity, currentTokens.get()+refillRate);
            currentTokens.set(filledTokens);
        }, 1, 10, TimeUnit.SECONDS);
    }
    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {
        int current = currentTokens.get();
        if (currentTokens.get() < 0) {
            sendHttpResponse(ctx, request, HttpStatueCode.LIMIT_ERROR);
            return;
        }
        //尝试三次获取令牌
        for (int i = 0; i < 3; i++) {
            if (currentTokens.compareAndSet(current, current - 1)) {
                // 成功获取令牌，继续处理请求
                Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        sendHttpResponse(ctx, request, HttpStatueCode.LIMIT_ERROR);
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }
}
