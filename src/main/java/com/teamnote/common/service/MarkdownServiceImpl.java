package com.teamnote.common.service;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.springframework.stereotype.Service;

@Service
public class MarkdownServiceImpl implements MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Cleaner cleaner;

    public MarkdownServiceImpl(Parser parser, HtmlRenderer renderer, Cleaner cleaner) {
        this.parser = parser;
        this.renderer = renderer;
        this.cleaner = cleaner;
    }

    @Override
    public String render(String markdown) {
        String html = renderer.render(parser.parse(markdown));
        return cleaner.clean(Jsoup.parseBodyFragment(html)).body().html();
    }
}
