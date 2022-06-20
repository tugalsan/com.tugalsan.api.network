package com.tugalsan.api.network.client;

import java.util.stream.*;

public class TGS_NetworkHTMLUtils {

    public static String escapeHtmlGWT(CharSequence html) {
        if (html == null) {
            return null;
        }
        return html.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String makeHTMLSpace(int charSize) {//USE HTML NOT LABEL
        var sb = new StringBuilder();
        IntStream.range(0, charSize).forEachOrdered(i -> sb.append(HTML_SPACE()));
        return sb.toString();
    }

    public static String HTML_SPACE() {
        return "&nbsp;";
    }
}
