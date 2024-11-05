package processors;

import processors.flush.BlackFlushProcessor;
import processors.limit.CountLimit;
import processors.limit.LeakyBucketLimit;
import processors.limit.TokenBucketLimit;


import java.util.Map;

/**
 * 处理器工厂 生产需要不同参数的处理器
 */
public class ProcessorFactory {

    public static Processor createBlackFlushProcessor(Map<String, String> property) {
        String blackTime = property.getOrDefault("blackTime", "3600000");
        String maxRequest = property.getOrDefault("maxRequest", "1000");
        return new BlackFlushProcessor(Integer.parseInt(maxRequest), Long.parseLong(blackTime));
    }
    public static Processor createCountLimit(Map<String, String> property) {
        String maxRequest = property.getOrDefault("maxRequest", "1000");
        return new CountLimit(Integer.parseInt(maxRequest));
    }

    public static Processor createLeakyBucketLimit(Map<String, String> property) {
        String capacity = property.getOrDefault("capacity", "1000");
        String rate = property.getOrDefault("rate", "100");
        return new LeakyBucketLimit(Integer.parseInt(capacity), Integer.parseInt(rate));
    }

    public static Processor createTokenBucketLimit(Map<String, String> property) {
        String capacity = property.getOrDefault("capacity", "1000");
        String rate = property.getOrDefault("rate", "100");
        return new TokenBucketLimit(Integer.parseInt(capacity), Integer.parseInt(rate));
    }
    public static Processor createEmptyProperProcessor(String className) throws Exception {
        Class<?> aClass = Class.forName(className);
        Processor o = (Processor)aClass.getConstructor().newInstance();
        return o;
    }
}
