package com.holmesycl.ebook.command;

import com.holmesycl.ebook.ApplicationContext;
import com.holmesycl.ebook.bean.*;
import com.holmesycl.ebook.util.Config;
import com.holmesycl.ebook.util.Retry;
import com.holmesycl.ebook.util.TemplateUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersistBookCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DiscoverBookCommand.class);
    private static String FILE_SUFFIX = ".html";

    private ApplicationContext context;

    private List<Bookshelf> bookshelves = new ArrayList<>();

    ExecutorService executorService;

    public PersistBookCommand(ApplicationContext context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(50);
    }

    @Override
    public void run() {
        logger.info("开始执行图书更新调度...");
        File baseDir = new File(Config.get("baseDir"));
        Book book;
        boolean bookshelfFileChanged = false;
        while ((book = context.pollUpdateBook()) != null) {
            if (!bookshelfFileChanged) {
                bookshelfFileChanged = true;
            }
            File bookFile = new File(baseDir, String.valueOf(book.getResourceId()));
            if (!bookFile.exists()) {
                try {
                    FileUtils.forceMkdir(bookFile);
                } catch (IOException e) {
                    logger.error("图书：【{}】目录【{}】创建失败。失败信息：{}", book.getBookName(), bookFile.getAbsolutePath(), e);
                }
            }
            // 创建图书目录
            createIndexFile(book, bookFile);
            Bookshelf bookshelf = new Bookshelf();
            bookshelf.setBookId(book.getBookId());
            bookshelf.setBookName(book.getBookName());
            bookshelf.setAuthor(book.getAuthor());
            bookshelf.setImgUrl(book.getImage());
            bookshelf.setFirstChapter(book.getFirstChapter());
            bookshelf.setLastChapter(book.getLastChapter());
            bookshelves.add(bookshelf);

            List<Chapter> chapters = book.getChapters();
            for (Chapter chapter :
                    chapters) {
                final Book fBook = book;
                executorService.submit(() -> {
                    try {
                        createChapterFile(fBook, bookFile, chapter);
                    } catch (IOException e) {
                        logger.info("图书：【{}】，章节：【{}】创建失败！错误信息：{}", fBook.getBookName(), chapter.getResourceName(), e);
                    }
                });
            }
        }
        if (bookshelfFileChanged) {
            createBookshelfFile(baseDir, bookshelves);
        }
    }


    private void createBookshelfFile(File baseDir, List<Bookshelf> bookshelves) {
        // 生成书架文件
        StringBuilder shelves = new StringBuilder();
        for (Bookshelf bookshelf : bookshelves) {
            shelves.append(bookshelf.html());
        }
        try {
            File bookshelfFile = new File(baseDir, "bookshelf.html");
            String content = String.format(TemplateUtil.bookshelf(), shelves.toString());
            FileUtils.writeStringToFile(bookshelfFile, content, "utf-8");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private File createIndexFile(Book book, File bookFile) {
        File indexFile = new File(bookFile, book.getIndex().getName() + FILE_SUFFIX);
        try {
            List<ChapterReference> chapterReferences = book.getIndex().getItems();
            StringBuilder items = new StringBuilder();
            for (ChapterReference chapterReference : chapterReferences) {
                String template = "<a href=\"./%s" + FILE_SUFFIX + "\" class=\"list-group-item\">%s</a>";
                items.append(String.format(template, chapterReference.getChapter().getResourceId(), chapterReference.getChapter().getResourceName()));
            }
            String indexHtml = String.format(TemplateUtil.index(), book.getBookName(), book.getBookName(), book.getAuthor(), book.getIntroduction(), items.toString());
            FileUtils.writeStringToFile(indexFile, indexHtml, "utf-8");
            logger.info("图书：【{}】目录生成完毕。", book.getBookName());
        } catch (IOException e) {
            logger.info("图书：【{}】目录生成失败。", book.getBookName());
        }
        return indexFile;
    }

    private File createChapterFile(Book book, File bookFile, Chapter chapter) throws IOException {
        File chapterFile = new File(bookFile, chapter.getResourceId() + FILE_SUFFIX);
        if (chapterFile.exists() && book.getLastChapter() != chapter) {
            logger.info("图书：【{}】，章节：【{}】已存在。", book.getBookName(), chapter.getResourceName());
            return chapterFile;
        }
        Document doc = new Retry<>(new ConnectCommand(chapter.getResourceUrl())).execute();
        String chapterHtml = doc.select("#content").html();
        chapterHtml = chapterHtml.substring(chapterHtml.indexOf("。") + 1);
        chapterHtml = chapterHtml.substring(0, chapterHtml.lastIndexOf("手机用户") != -1 ? chapterHtml.lastIndexOf("手机用户") : chapterHtml.length());
        chapterHtml += "<nav aria-label=\"...\">\n" +
                "  <ul class=\"pager\">\n" +
                "    <li class=\"previous\"><a href=\"./" + chapter.getPrefixChapterResource().getResourceId() + FILE_SUFFIX + "\"><span aria-hidden=\"true\">&larr;</span> 上一章</a></li>\n" +
                "<li><a href=\"" + book.getIndex().getName() + FILE_SUFFIX + "\">回目录</a></li>" +
                "<li><a href=\"../bookshelf" + FILE_SUFFIX + "\">我的书架</a></li>" +
                "    <li class=\"next\"><a href=\"./" + chapter.getNextChapterResource().getResourceId() + FILE_SUFFIX + "\">下一章 <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                "  </ul>\n" +
                "</nav>";
        chapterHtml = String.format(TemplateUtil.chapter(), book.getBookName() + " " + chapter.getResourceName(), chapter.getResourceName(), chapterHtml);
        FileUtils.writeStringToFile(chapterFile, chapterHtml, "utf-8");
        logger.info("图书：【{}】，章节：【{}】创建完毕。", book.getBookName(), chapter.getResourceName());
        return chapterFile;
    }
}
