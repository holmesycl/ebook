package com.holmesycl.ebook.util;

import java.util.concurrent.TimeUnit;

public class Retry<T> {

    private Command<T> command;

    private int currentTime;

    public Retry(Command<T> command) {
        this.command = command;
    }

    public T execute() {
        try {
            return command.execute();
        } catch (Exception e) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            currentTime++;
            System.out.println("开始执行第" + currentTime + "次重试。。。");
            return execute();
        }
    }
}
