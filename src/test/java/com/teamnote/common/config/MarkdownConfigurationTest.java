package com.teamnote.common.config;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownConfigurationTest {

    private final MarkdownConfiguration configuration = new MarkdownConfiguration();
    private final Parser parser = configuration.markdownParser();
    private final HtmlRenderer renderer = configuration.markdownHtmlRenderer();
    private final Cleaner cleaner = configuration.markdownHtmlCleaner();

    @Test
    void preservesMarkdownTableAndCodeAfterCleaning() {
        String markdown = """
                | 담당 | 업무 |
                | --- | --- |
                | 팀원 A | `TaskService` 구현 |
                """;

        String rendered = renderer.render(parser.parse(markdown));
        Document cleaned = cleaner.clean(Jsoup.parseBodyFragment(rendered));

        assertThat(cleaned.select("table")).hasSize(1);
        assertThat(cleaned.select("th").eachText()).containsExactly("담당", "업무");
        assertThat(cleaned.select("code").text()).isEqualTo("TaskService");
    }

    @Test
    void removesUnsafeAttributesAndUrlsWhileKeepingHttpsLinks() {
        String html = """
                <p onclick="alert(1)">회의록</p>
                <a href="javascript:alert(1)">위험 링크</a>
                <a href="https://example.com">참고 자료</a>
                <script>alert(1)</script>
                """;

        Document cleaned = cleaner.clean(Jsoup.parseBodyFragment(html));

        assertThat(cleaned.select("script, [onclick]")).isEmpty();
        assertThat(cleaned.select("a[href]").eachAttr("href")).containsExactly("https://example.com");
        assertThat(cleaned.select("a[href]").attr("rel")).isEqualTo("nofollow noopener noreferrer");
    }

    @Test
    void escapesRawHtmlInMarkdownInput() {
        String rendered = renderer.render(parser.parse("<script>alert(1)</script>"));

        assertThat(rendered).contains("&lt;script&gt;").doesNotContain("<script>");
    }
}
