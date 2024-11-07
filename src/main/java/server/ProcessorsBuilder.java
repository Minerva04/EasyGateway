package server;



import processors.EmptyProcessor;
import processors.Processor;
import processors.ProcessorFactory;
import processors.auth.JwtAuthProcessor;
import processors.flush.BlackFlushProcessor;
import processors.limit.CountLimit;
import processors.limit.LeakyBucketLimit;
import processors.limit.TokenBucketLimit;
import processors.router.ConfigRouter;


import java.util.HashMap;
import java.util.Map;

import java.util.function.Function;

/**
 * 构造处理器
 */
public class ProcessorsBuilder {
    private static Processor firstProcessor;

    private static Map<String, Map<String, String>> processorMap = new HashMap<>();
    private static Map<String,String> defaultProcessor = new HashMap<>();
    private static final Map<String, Function<Map<String, String>, Processor>> processorCreateMap = new HashMap<>();

    /**
     * 策略者模式加工厂模式 每个处理器有多个不同的实现 将不同的处理器的生产方法注册到map中后续生产只需要拿到需要生产的处理器的类名 直接调用生产方法就可以生产
     * 生产方法定义在处理器工厂中
     */
    static {
        processorCreateMap.put(BlackFlushProcessor.class.getName(), ProcessorFactory::createBlackFlushProcessor);
        processorCreateMap.put(CountLimit.class.getName(), ProcessorFactory::createCountLimit);
        processorCreateMap.put(LeakyBucketLimit.class.getName(), ProcessorFactory::createLeakyBucketLimit);
        processorCreateMap.put(TokenBucketLimit.class.getName(), ProcessorFactory::createTokenBucketLimit);
        processorCreateMap.put(ConfigRouter.class.getName(), ProcessorFactory::createConfigRouter);
    }



    //加载将每种处理器支持的不同种类处理器
    static {
        Map<String,String>authMap=new HashMap<>();
        authMap.put("jwt", JwtAuthProcessor.class.getName());
        defaultProcessor.put("auth", JwtAuthProcessor.class.getName());

        Map<String,String>flushMap=new HashMap<>();
        flushMap.put("black", BlackFlushProcessor.class.getName());
        defaultProcessor.put("flush", BlackFlushProcessor.class.getName());

        Map<String,String>limitMap=new HashMap<>();
        limitMap.put("count", CountLimit.class.getName());
        limitMap.put("leaky", LeakyBucketLimit.class.getName());
        limitMap.put("token", TokenBucketLimit.class.getName());
        defaultProcessor.put("limit", CountLimit.class.getName());

        Map<String,String>routerMap=new HashMap<>();
        routerMap.put("config", ConfigRouter.class.getName());
        defaultProcessor.put("router", ConfigRouter.class.getName());


        processorMap.put("auth",authMap);
        processorMap.put("flush",flushMap);
        processorMap.put("limit",limitMap);
        processorMap.put("router",routerMap);
        firstProcessor = new EmptyProcessor();
    }

    public static void initProcessor(Map<String,Map<String,String>> configMap) {
        Processor processor = getProcessor();
        for (Map.Entry<String, Map<String, String>> entry : configMap.entrySet()) {
            String key = entry.getKey();
            if(processorMap.containsKey(key)){
                Processor next = createProcessor(key, entry.getValue());
                processor.setNext(next);
                processor = next;
            }
        }
    }

    private static Processor createProcessor(String type, Map<String, String> property)  {
        Map<String, String> typeMap = processorMap.get(type);
        if (typeMap == null) {
            throw new RuntimeException("processor type " + type + " not found");
        }
        String enable = property.getOrDefault("enable", "true");
        if (!"true".equals(enable)) {
            return new EmptyProcessor();
        }
        String className = typeMap.getOrDefault(property.get("policy"), defaultProcessor.get(type));
        if(processorCreateMap.containsKey(className)){
            return processorCreateMap.get(className).apply(property);
        }else{
            try {
               return ProcessorFactory.createEmptyProperProcessor(className);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }



    public static Processor getProcessor() {
        return firstProcessor;
    }

}
