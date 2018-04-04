package com.holmesycl.ebook;

import com.holmesycl.ebook.bean.*;
import com.holmesycl.ebook.db.BookDbUtil;
import com.holmesycl.ebook.util.Config;
import com.holmesycl.ebook.util.Retry;
import com.holmesycl.ebook.util.TemplateUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static String FILE_SUFFIX = ".html";

    static Map<Integer, AtomicInteger> CURRENT_NUM = new ConcurrentHashMap<>();

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(20);

    public static void main(String[] args) {

        CompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(20));

        List<BookLink> bookLinks = new ArrayList<>();
        try {
            logger.info("查询需要更新的书籍。");
            bookLinks = BookDbUtil.qryAllBookLink();
        } catch (SQLException e) {
            logger.error("从数据库获取需要更新的书籍出错。", e);
        }

        File baseDir = new File(Config.get("baseDir"));
        logger.info("图书主目录：" + baseDir.getAbsolutePath());

        List<Bookshelf> bookshelves = new ArrayList<>();
        for (BookLink bookLink : bookLinks) {
            // 获取目录
            try {
                logger.info("开始更新图书：【{}】，图书来源：{}", bookLink.getBookName(), bookLink.getLinkUrl());
                logger.info("开始从图书原链接获取图书更新信息...");
                long start = System.currentTimeMillis();
                Document document = new Retry<>(new ConnectCommand(bookLink.getLinkUrl())).execute();
                long end = System.currentTimeMillis();
                logger.info("图书更新信息获取完毕，耗时：{}毫秒。", end - start);

                Book book = new Book();
                int bookId = bookLink.getBookId();
                book.setBookId(bookLink.getBookId());
                boolean updateBook = initBook(document, book);
                if (!updateBook) {
                    logger.info("图书：【" + book.getBookName() + "】不需要更新，最后更新时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(book.getLastUpdateDate()));
                    continue;
                }

                Bookshelf bookshelf = new Bookshelf();
                bookshelf.setBookId(bookId);
                bookshelf.setBookName(book.getBookName());
                bookshelf.setAuthor(book.getAuthor());
                bookshelf.setImgUrl(book.getImage());
                bookshelves.add(bookshelf);

                CURRENT_NUM.put(bookId, new AtomicInteger(0));
                File bookFile = new File(baseDir, String.valueOf(book.getBookId()));
                if (!bookFile.exists()) {
                    FileUtils.forceMkdir(bookFile);
                }

                List<ChapterResource> chapterLinks = new ArrayList<ChapterResource>();
                Elements elements = document.select("#list a");
                Index index = new Index();
                book.setIndex(index);

                for (int i = 0, len = elements.size(); i < len; i++) {
                    Element current = elements.get(i);
                    Element prefix = elements.get(i == 0 ? i : i - 1);
                    Element next = elements.get(i == len - 1 ? i : i + 1);
                    ChapterResource chapterLink = new ChapterResource();
                    chapterLink.setChapterLink(current.attr("abs:href"));
                    chapterLink.setPrefixChapterLink(new ChapterResource(prefix.attr("abs:href")));
                    chapterLink.setNextChapterLink(new ChapterResource(next.attr("abs:href")));
                    String chapterTitle = current.text();

                    if (current == next) {
                        bookshelf.setLastChapterTitle(chapterTitle);
                    }
                    if (current == prefix) {
                        bookshelf.setFirstChapter(chapterLink.getPageNumber() + FILE_SUFFIX);
                    }

                    chapterLink.setChapterTitle(chapterTitle);
                    chapterLinks.add(chapterLink);
                    completionService.submit(() -> {
                        try {
                            Document doc = new Retry<>(new ConnectCommand(chapterLink.getChapterLink())).execute();
                            String chapterHtml = doc.select("#content").html();
                            chapterHtml = chapterHtml.substring(chapterHtml.indexOf("。") + 1);
                            chapterHtml = chapterHtml.substring(0, chapterHtml.lastIndexOf("手机用户") != -1 ? chapterHtml.lastIndexOf("手机用户") : chapterHtml.length());
                            //chapterHtml = chapterHtml.replaceAll("<br>", "\n");
                            return createChapterFile(book, bookFile, index, chapterLink, chapterHtml);
//                            BookChapter chapter = BookDbUtil.findBookChapterByBookIdAndPageNumber(book.getBookId(), chapterLink.getPageNumber());
//                            if (chapter == null) {
//                                chapter = new BookChapter();
//                                chapter.setBookId(book.getBookId());
//                                chapter.setPageNumber(chapterLink.getPageNumber());
//                                chapter.setPreviousChapter(chapterLink.getPrefixChapterLink().getPageNumber());
//                                chapter.setNextChapter(chapterLink.getNextChapterLink().getPageNumber());
//                                chapter.setTitle(chapterTitle);
//                                chapter.setContent(chapterHtml.replaceAll("<br>", "\n"));
//                                chapter.setCreateDate(new Date());
//                                BookDbUtil.saveBookChapter(chapter);
//                            }
                        } catch (Exception e) {
                            logger.error("章节【{}】创建失败。失败信息{}", chapterLink.getChapterTitle(), e);
                            return null;
                        }
                    });
                    ChapterReference chapterItem = new ChapterReference();
                    chapterItem.setPageNumber(chapterLink.getPageNumber());
                    chapterItem.setChapterTitle(chapterTitle);
                    index.addChapterItem(chapterItem);
                }
                // 保存索引文件
                createIndexFile(book, bookFile, index);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        //executorService.shutdown();
        createBookshelfFile(baseDir, bookshelves);
    }

    private static void createBookshelfFile(File baseDir, List<Bookshelf> bookshelves) {
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

    private static File createChapterFile(Book book, File bookFile, Index index, ChapterResource chapterLink, String chapterHtml) throws SQLException, IOException {
        File chapterFile = new File(bookFile, chapterLink.getPageNumber() + FILE_SUFFIX);
        if (chapterFile.exists()) {
            return null;
        }
        chapterHtml += "<nav aria-label=\"...\">\n" +
                "  <ul class=\"pager\">\n" +
                "    <li class=\"previous\"><a href=\"./" + chapterLink.getPrefixChapterLink().getPageNumber() + FILE_SUFFIX + "\"><span aria-hidden=\"true\">&larr;</span> 上一章</a></li>\n" +
                "<li><a href=\"" + index.getPageNumber() + FILE_SUFFIX + "\">回目录</a></li>" +
                "    <li class=\"next\"><a href=\"./" + chapterLink.getNextChapterLink().getPageNumber() + FILE_SUFFIX + "\">下一章 <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                "  </ul>\n" +
                "</nav>";
        chapterHtml = String.format(TemplateUtil.chapter(), book.getBookName(), chapterLink.getChapterTitle(), chapterHtml);
        FileUtils.writeStringToFile(chapterFile, chapterHtml, "utf-8");
        logger.info("图书：【{}】，章节：【{}】创建完毕。", book.getBookName(), chapterLink.getChapterTitle());
        return chapterFile;
    }

    private static File createIndexFile(Book book, File bookFile, Index index) {
        File indexFile = new File(bookFile, index.getPageNumber() + FILE_SUFFIX);
        try {
            List<ChapterReference> chapterItems = index.getItems();
            String chapterItemLinkList = "";
            for (ChapterReference chapterItem : chapterItems) {
                String chapterItemTemplate = "<a href=\"./%s" + FILE_SUFFIX + "\" class=\"list-group-item\">%s</a>";
                String chapterItemLink = String.format(chapterItemTemplate, chapterItem.getPageNumber(), chapterItem.getChapterTitle());
                chapterItemLinkList += chapterItemLink;
//                BookIndex bookIndex = BookDbUtil.findBookIndexByBookIdAndPageNumber(book.getBookId(), chapterItem.getPageNumber());
//                if (bookIndex == null) {
//                    bookIndex = new BookIndex();
//                    bookIndex.setBookId(book.getBookId());
//                    bookIndex.setPageNumber(chapterItem.getPageNumber());
//                    bookIndex.setTitle(chapterItem.getChapterTitle());
//                    bookIndex.setCreateDate(new Date());
//                    BookDbUtil.saveBookIndex(bookIndex);
//                }
            }
            String indexHtml = String.format(TemplateUtil.index(), book.getBookName(), book.getBookName(), book.getAuthor(), book.getIntroduction(), chapterItemLinkList);
            FileUtils.writeStringToFile(indexFile, indexHtml, "utf-8");
            logger.info("图书：【{}】目录生成完毕。", book.getBookName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexFile;
    }

    private static boolean initBook(Document document, Book book) throws Exception {
        Element mainInfo = document.selectFirst("#maininfo");
        Element info = mainInfo.selectFirst("#info");
        String bookName = info.select("h1").text();
        book.setBookName(bookName);

        String author = info.selectFirst("p").text();
        author = author.substring(author.lastIndexOf("：") + 1);

        // 最后更新：2018-03-27 12:11:52
        String lastUpdateDate = info.select("p").get(2).text();
        lastUpdateDate = lastUpdateDate.substring(5);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        book.setLastUpdateDate(sdf.parse(lastUpdateDate));

        book.setAuthor(author);
        Element intro = mainInfo.selectFirst("#intro");
        String introduction = intro.selectFirst("p").text();
        book.setIntroduction(introduction);

        String imageScript = document.selectFirst("#fmimg script").attr("abs:src");
        Document imageDocument = Jsoup.connect(imageScript).get();
        String src = imageDocument.body().html();
        String imageHtml = src.substring(src.indexOf("<"), src.lastIndexOf(">") + 1);
        String image = Jsoup.parse(imageHtml).selectFirst("img").attr("src");
        book.setImage(image);

        Book dbBook = BookDbUtil.getBookById(book.getBookId());
        if (dbBook == null) {
            BookDbUtil.saveBook(book);
            return true;
        } else {
            String dbLastUpdateDate = sdf.format(dbBook.getLastUpdateDate());
            if (lastUpdateDate.compareTo(dbLastUpdateDate) > 0) {
                BookDbUtil.updateBook(book);
                return true;
            }
        }
        return true;
    }

}
