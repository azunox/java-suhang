package com.teamnote.common.config;

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MarkdownConfiguration {

    @Bean
    public Parser markdownParser() {
        return Parser.builder()
                .extensions(markdownExtensions())
                .build();
    }

    @Bean
    public HtmlRenderer markdownHtmlRenderer() {
        return HtmlRenderer.builder()
                .extensions(markdownExtensions())
                .escapeHtml(true)
                .sanitizeUrls(true)
                .build();
    }

    @Bean
    public Cleaner markdownHtmlCleaner() {
        // Markdown 표·코드는 보존하면서 외부 입력의 태그·속성·링크 범위를 제한한다.
        Safelist safelist = Safelist.basic()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "hr",
                        "table", "thead", "tbody", "tr", "th", "td")
                .removeProtocols("a", "href", "ftp", "mailto")
                .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer");
        return new Cleaner(safelist);
    }

    private List<Extension> markdownExtensions() {
        return List.of(TablesExtension.create());
    }
}
