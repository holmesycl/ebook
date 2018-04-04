package com.holmesycl.ebook.bean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Book extends Resource {

    private int bookId;

    private String bookName;

    private String image;

    private String author;

    private String introduction;

    private Date lastUpdateDate;

    private Index index;

    private List<Chapter> chapters;

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", image='" + image + '\'' +
                ", author='" + author + '\'' +
                ", introduction='" + introduction + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", index=" + index +
                ", chapters=" + chapters +
                '}';
    }

    public void init(Document document) throws Exception {
        Element mainInfo = document.selectFirst("#maininfo");
        Element info = mainInfo.selectFirst("#info");
        String author = info.selectFirst("p").text();
        author = author.substring(author.lastIndexOf("：") + 1);

        // 最后更新：2018-03-27 12:11:52
        String lastUpdateDate = info.select("p").get(2).text();
        lastUpdateDate = lastUpdateDate.substring(5);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.setLastUpdateDate(sdf.parse(lastUpdateDate));

        this.setAuthor(author);
        Element intro = mainInfo.selectFirst("#intro");
        String introduction = intro.selectFirst("p").text();
        this.setIntroduction(introduction);

        String imageScript = document.selectFirst("#fmimg script").attr("abs:src");
        Document imageDocument = Jsoup.connect(imageScript).get();
        String src = imageDocument.body().html();
        String imageHtml = src.substring(src.indexOf("<"), src.lastIndexOf(">") + 1);
        String image = Jsoup.parse(imageHtml).selectFirst("img").attr("src");
        this.setImage(image);

        Elements elements = document.select("#list a");
        Index index = new Index();
        for (int i = 0, len = elements.size(); i < len; i++) {
            Element current = elements.get(i);
            Element prefix = elements.get(i == 0 ? i : i - 1);
            Element next = elements.get(i == len - 1 ? i : i + 1);
            ChapterResource chapterResource = new ChapterResource(current.text(), current.attr("abs:href"));
            chapterResource.setPrefixChapterResource(new ChapterResource(prefix.text(), prefix.attr("abs:href")));
            chapterResource.setNextChapterResource(new ChapterResource(next.text(), next.attr("abs:href")));

            ChapterReference chapterReference = new ChapterReference();
            chapterReference.setChapterTitle(chapterResource.getResourceName());
            chapterReference.setChapter(chapterReference);
            index.addChapterItem(chapterItem);
        }
        this.setIndex(index);
    }
}
