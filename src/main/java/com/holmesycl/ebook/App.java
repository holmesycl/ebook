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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {

    static String FILE_SUFFIX = ".html";

    static Map<Integer, AtomicInteger> CURRENT_NUM = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<BookLink> bookLinks = new ArrayList<>();
        try {
            bookLinks = BookDbUtil.qryAllBookLink();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(Config.get("baseDir"));
        File baseDir = new File(Config.get("baseDir"));
        for (BookLink bookLink :
                bookLinks) {
            // 获取目录
            try {
                Book book = new Book();
                int bookId = bookLink.getBookId();
                book.setBookId(bookLink.getBookId());
                Document document = new Retry<>(new ConnectCommand(bookLink.getLinkUrl())).execute();
                boolean updateBook = initBook(document, book);
                if (!updateBook) {
                    System.out.println("图书：【" + book.getBookName() + "】不需要更新，最后更新时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(book.getLastUpdateDate()));
                    continue;
                }
                CURRENT_NUM.put(bookId, new AtomicInteger(0));
                File bookFile = new File(baseDir, String.valueOf(book.getBookId()));
                FileUtils.deleteDirectory(bookFile);
                System.out.println("删除文件" + bookFile.getAbsolutePath());
                FileUtils.forceMkdir(bookFile);
                System.out.println("创建文件" + bookFile.getAbsolutePath());

                List<ChapterLink> chapterLinks = new ArrayList<ChapterLink>();
                Elements elements = document.select("#list a");
                Index index = new Index();
                book.setIndex(index);

                for (int i = 0, len = elements.size(); i < len; i++) {
                    Element current = elements.get(i);
                    Element prefix = elements.get(i == 0 ? i : i - 1);
                    Element next = elements.get(i == len - 1 ? i : i + 1);
                    ChapterLink chapterLink = new ChapterLink();
                    chapterLink.setChapterLink(current.attr("abs:href"));
                    chapterLink.setPrefixChapterLink(new ChapterLink(prefix.attr("abs:href")));
                    chapterLink.setNextChapterLink(new ChapterLink(next.attr("abs:href")));
                    String chapterTitle = current.text();
                    chapterLink.setChapterTitle(chapterTitle);
                    chapterLinks.add(chapterLink);
                    executorService.submit(() -> {
                        try {
                            Document doc = new Retry<>(new ConnectCommand(chapterLink.getChapterLink())).execute();
                            String chapterHtml = doc.select("#content").html();
                            chapterHtml = chapterHtml.substring(chapterHtml.indexOf("。") + 1);
                            chapterHtml = chapterHtml.substring(0, chapterHtml.lastIndexOf("手机用户"));
                            chapterHtml = chapterHtml.replaceAll("<br>", "\n");
                            BookChapter chapter = BookDbUtil.findBookChapterByBookIdAndPageNumber(book.getBookId(), chapterLink.getPageNumber());
                            if (chapter == null) {
                                chapter = new BookChapter();
                                chapter.setBookId(book.getBookId());
                                chapter.setPageNumber(chapterLink.getPageNumber());
                                chapter.setPreviousChapter(chapterLink.getPrefixChapterLink().getPageNumber());
                                chapter.setNextChapter(chapterLink.getNextChapterLink().getPageNumber());
                                chapter.setTitle(chapterTitle);
                                chapter.setContent(chapterHtml);
                                chapter.setCreateDate(new Date());
                                BookDbUtil.saveBookChapter(chapter);
                            }

                            chapterHtml += "<nav aria-label=\"...\">\n" +
                                    "  <ul class=\"pager\">\n" +
                                    "    <li class=\"previous\"><a href=\"./" + chapterLink.getPrefixChapterLink().getPageNumber() + FILE_SUFFIX + "\"><span aria-hidden=\"true\">&larr;</span> 上一章</a></li>\n" +
                                    "<li><a href=\"" + index.getPageNumber() + FILE_SUFFIX + "\">回目录</a></li>" +
                                    "    <li class=\"next\"><a href=\"./" + chapterLink.getNextChapterLink().getPageNumber() + FILE_SUFFIX + "\">下一章 <span aria-hidden=\"true\">&rarr;</span></a></li>\n" +
                                    "  </ul>\n" +
                                    "</nav>";
                            chapterHtml = String.format(TemplateUtil.chapter(), book.getBookName(), chapterLink.getChapterTitle(), chapterHtml);
                            File chapterFile = new File(bookFile, chapterLink.getPageNumber() + FILE_SUFFIX);
                            FileUtils.writeStringToFile(chapterFile, chapterHtml, "utf-8");
                            int cur = CURRENT_NUM.get(book.getBookId()).incrementAndGet();
                            System.out.println("章节文件：" + chapterFile.getName() + "创建完成，当前进度：" + cur + " / " + len);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    ChapterItem chapterItem = new ChapterItem();
                    chapterItem.setPageNumber(chapterLink.getPageNumber());
                    chapterItem.setChapterTitle(chapterTitle);
                    index.addChapterItem(chapterItem);
                }
                executorService.submit(() -> {
                    File indexFile = new File(bookFile, index.getPageNumber() + FILE_SUFFIX);
                    try {
                        List<ChapterItem> chapterItems = index.getItems();
                        String chapterItemLinkList = "";
                        for (ChapterItem chapterItem : chapterItems) {
                            String chapterItemTemplate = "<a href=\"./%s" + FILE_SUFFIX + "\" class=\"list-group-item\">%s</a>";
                            String chapterItemLink = String.format(chapterItemTemplate, chapterItem.getPageNumber(), chapterItem.getChapterTitle());
                            chapterItemLinkList += chapterItemLink;

                            BookIndex bookIndex = BookDbUtil.findBookIndexByBookIdAndPageNumber(bookId, chapterItem.getPageNumber());
                            if (bookIndex == null) {
                                bookIndex = new BookIndex();
                                bookIndex.setBookId(bookId);
                                bookIndex.setPageNumber(chapterItem.getPageNumber());
                                bookIndex.setTitle(chapterItem.getChapterTitle());
                                bookIndex.setCreateDate(new Date());
                                BookDbUtil.saveBookIndex(bookIndex);
                            }
                        }
                        String indexHtml = String.format(TemplateUtil.index(), book.getBookName(), book.getBookName(), book.getAuthor(), book.getIntroduction(), chapterItemLinkList);
                        FileUtils.writeStringToFile(indexFile, indexHtml, "utf-8");
                        System.out.println("目录文件：" + indexFile.getName() + "创建完成。");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
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
                return false;
            }
        }
        return true;
    }

}
