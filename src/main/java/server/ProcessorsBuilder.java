package server;

import processors.auth.AuthProcessor;
import processors.EmptyProcessor;
import processors.Processor;
import processors.limit.CountLimit;
import processors.limit.LeakyBucketLimit;
import processors.limit.TokenBucketLimit;

/**
 * 构造处理器
 */
public class ProcessorsBuilder {
    private static Processor firstProcessor;

    static {
        firstProcessor = new EmptyProcessor();
        //TODO 通过配置文件的形式读取设置的处理器
        firstProcessor.setNext(new TokenBucketLimit(2,1));
    }

    public static Processor getFirstProcessor() {
        return firstProcessor;
    }
}
