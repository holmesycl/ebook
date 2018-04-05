package com.holmesycl.ebook;

import com.holmesycl.ebook.command.DiscoverBookCommand;
import com.holmesycl.ebook.command.PersistBookCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerApplication {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerApplication.class);

    /**
     * 启动入口
     *
     * @param args
     */
    public static void main(String[] args) {
        logger.info("开始启动电子书爬虫...");
        ApplicationContext context = new ApplicationContext();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleWithFixedDelay(new DiscoverBookCommand(context), 1, 10 * 60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(new PersistBookCommand(context), 5, 5, TimeUnit.SECONDS);
        logger.info("电子书爬虫启动完毕...");
    }

}
