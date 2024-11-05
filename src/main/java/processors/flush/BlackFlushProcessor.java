package processors.flush;

import common.HttpStatueCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import processors.Processor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 防刷处理器
 * 通过黑名单判断是否通过 每次请求时将当前请求时间放入用户队列中 同时清理队列中1秒中之前的请求时间
 * 队列中剩下的就是一秒钟之内的请求时间 判断队列大小是否大于阈值
 */
public class BlackFlushProcessor implements Processor {
    private Processor nextProcessor;
    private Map<String, Deque<Long>> requestTimesMap = new ConcurrentHashMap<>(); // 存储每个IP的请求时间戳

    private Map<String, Long> blackList = new ConcurrentHashMap<>(); // 存储黑名单IP及过期时间


    //TODO 通过配置文件的形式读取最大请求次数 拉黑时间
    private int MAX_REQUESTS = 3;
    //拉黑10分钟
    private long EXPIRE = 1000*60*10;

    public BlackFlushProcessor() {

    }
    public BlackFlushProcessor(int MAX_REQUESTS,long EXPIRE) {
        this.MAX_REQUESTS = MAX_REQUESTS;
        this.EXPIRE = EXPIRE;

    }


    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request) {

        // 获取IP
        String ip = request.headers().get("X-Forwarded-For");
        if (ip == null) {
            ip = ctx.channel().remoteAddress().toString();
        }

        long currentTime = System.currentTimeMillis();

        // 检查黑名单
        if (blackList.containsKey(ip)) {
            if (currentTime < blackList.get(ip)) {
                sendHttpResponse(ctx, request, HttpStatueCode.FLUSH_ERROR);
                return;
            } else {
                blackList.remove(ip);
            }
        }

        // 记录请求时间
        requestTimesMap.putIfAbsent(ip, new LinkedList<>());
        Deque<Long> requestTimes = requestTimesMap.get(ip);

        // 清理过期请求时间
        while (!requestTimes.isEmpty() && currentTime - requestTimes.peekFirst() > 10000) {
            requestTimes.pollFirst();
        }

        // 添加当前请求时间
        requestTimes.addLast(currentTime);

        // 检查请求次数
        if (requestTimes.size() > MAX_REQUESTS) {
            blackList.put(ip, currentTime + EXPIRE);
            sendHttpResponse(ctx, request, HttpStatueCode.FLUSH_ERROR);
            return;
        }

        Optional.ofNullable(nextProcessor).ifPresent(p -> p.process(ctx, request));

    }

    @Override
    public void setNext(Processor processor) {
        this.nextProcessor = processor;
    }



}
