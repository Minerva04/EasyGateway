package processors.limit;

import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.Processor;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 漏桶算法限流器
 * 算法思想 请求来到时判断漏桶是否有容量 有就存入漏桶中同时通过countDownlanch阻塞该线程继续向下执行 没有就拒绝请求
 * 后台线程每秒取出一定量的请求进行放行（通过countdownlangch放行执行请求的线程）
 *
 */
public class LeakyBucketLimit implements Processor {

    private Processor nextProcessor;
    private final int capacity; // 桶的容量
    private final int leakRate; // 漏水速率（每秒漏出的请求数）
    private final AtomicInteger currentWater = new AtomicInteger(0); // 当前桶中的水量（请求数）
    private final Queue<Task> requestQueue = new LinkedList<>(); // 存储请求的队列
    private  final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LeakyBucketLimit(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        start();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            int allowedRequests = Math.min(leakRate, currentWater.get());
            currentWater.addAndGet(-allowedRequests);
            for (int i = 0; i < allowedRequests; i++) {
                Task task = requestQueue.poll();
                task.latch.countDown();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }


    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

        if (currentWater.incrementAndGet() > capacity) {
            sendHttpResponse(ctx, request, HttpStatueCode.LIMIT_ERROR);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        requestQueue.add(new Task(latch));
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));
    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }

    /**
     * 用于控制线程阻塞
     */
    private class Task {
        CountDownLatch latch;

        public Task(CountDownLatch latch) {
            {
                this.latch = latch;
            }
        }
    }
}
