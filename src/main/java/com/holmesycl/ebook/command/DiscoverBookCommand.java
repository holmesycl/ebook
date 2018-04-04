package com.holmesycl.ebook.command;

import com.holmesycl.ebook.ApplicationContext;
import com.holmesycl.ebook.bean.Book;
import com.holmesycl.ebook.bean.BookLink;
import com.holmesycl.ebook.bean.ConnectCommand;
import com.holmesycl.ebook.db.BookDbUtil;
import com.holmesycl.ebook.util.Retry;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class DiscoverBookCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DiscoverBookCommand.class);

    private ApplicationContext context;

    public DiscoverBookCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        List<BookLink> bookLinks = null;
        try {
            logger.info("查询需要更新的书籍...");
            bookLinks = BookDbUtil.qryAllBookLink();
        } catch (SQLException e) {
            logger.error("从数据库获取需要更新的书籍出错。", e);
        }

        if (bookLinks == null || bookLinks.size() == 0) {
            logger.info("无需要实时更新的图书...");
        }
        CompletionService<Book> completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(5));
        for (BookLink bookLink : bookLinks) {
            completionService.submit(() -> {
                logger.info("开始获取图书：【{}】信息，图书来源：{}", bookLink.getBookName(), bookLink.getLinkUrl());
                long start = System.currentTimeMillis();
                Document document = new Retry<>(new ConnectCommand(bookLink.getLinkUrl())).execute();
                long end = System.currentTimeMillis();
                logger.info("图书【{}】更新信息获取完毕，耗时：{}毫秒。", bookLink.getBookName(), end - start);
                Book book = new Book();
                book.setResourceName(bookLink.getBookName());
                book.setResourceUrl(bookLink.getLinkUrl());
                book.setBookId(bookLink.getBookId());
                book.init(document);
                logger.info("图书【{}】初始化完毕。详情：{}", book.getBookName(), book);
                return book;
            });
        }
        for (int i = 0, len = bookLinks.size(); i < len; i++) {
            try {
                Book book = completionService.take().get();
                Book dbBook = null;
                try {
                    dbBook = BookDbUtil.getBookById(book.getBookId());
                    if (dbBook == null) {
                        BookDbUtil.saveBook(book);
                        dbBook = book;
                    }
                } catch (SQLException e) {
                    logger.error("出错了，错误信息：{}", e);
                }
                if (book.getLastUpdateDate().after(dbBook.getLastUpdateDate())) {
                    context.offerUpdateBook(book);
                }
            } catch (InterruptedException e) {
                logger.error("出错了，错误信息：{}", e);
            } catch (ExecutionException e) {
                logger.error("出错了，错误信息：{}", e);
            }
        }
    }
}
