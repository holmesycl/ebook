package com.holmesycl.ebook;

import com.holmesycl.ebook.bean.*;
import com.holmesycl.ebook.util.Retry;
import com.holmesycl.ebook.util.TemplateUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {

    static String FILE_SUFFIX = ".html";

    static AtomicInteger CURRENT_NUM = new AtomicInteger(0);

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("参数不对。正确格式：地址 线程池数量。");
        }
        String bookUrl = args[0];
        int nThreads = Integer.parseInt(args[1]);
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        BookLink bookLink = new BookLink(bookUrl);
        // 获取目录
        try {
            Book book = new Book();
            book.setBookId(bookLink.getId());

            File bookFile = new File(String.valueOf(book.getBookId()));
            FileUtils.deleteDirectory(bookFile);
            System.out.println("删除文件" + bookFile.getName());
            FileUtils.forceMkdir(bookFile);
            System.out.println("创建文件" + bookFile.getName());
            Document document = new Retry<Document>(new ConnectCommand(bookLink.getUrl())).execute();
            initBook(document, book);
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
                        Document doc = new Retry<Document>(new ConnectCommand(chapterLink.getChapterLink())).execute();
                        String chapterHtml = doc.select("#content").html();
                        chapterHtml = chapterHtml.substring(chapterHtml.indexOf("。") + 1);
                        chapterHtml = chapterHtml.substring(0, chapterHtml.lastIndexOf("手机用户"));
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
                        int cur = CURRENT_NUM.incrementAndGet();
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
                    }
                    String indexHtml = String.format(TemplateUtil.index(), book.getBookName(), book.getBookName(), book.getAuthor(), book.getIntroduction(), chapterItemLinkList);
                    FileUtils.writeStringToFile(indexFile, indexHtml, "utf-8");
                    System.out.println("目录文件：" + indexFile.getName() + "创建完成。");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initBook(Document document, Book book) throws Exception {
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
    }

}
