package com.holmesycl.ebook.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookUtil {

    public static void main(String[] args) {
        String s = "2018-03-27 12:11:52";
        Pattern pattern = Pattern.compile(s);
        Matcher matcher = pattern.matcher("");
        System.out.println(matcher.matches());

    }
}
