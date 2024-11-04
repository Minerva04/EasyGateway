package server;

import processors.AuthProcessor;
import processors.EmptyProcessor;
import processors.Processor;

/**
 * 构造处理器
 */
public class ProcessorsBuilder {
    private static Processor firstProcessor;

    static {
        firstProcessor = new EmptyProcessor();
        //TODO 通过配置文件的形式读取设置的处理器
        firstProcessor.setNext(new AuthProcessor());
    }

    public static Processor getFirstProcessor() {
        return firstProcessor;
    }
}
