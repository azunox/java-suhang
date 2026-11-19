package com.teamnote.common.service;

import com.teamnote.common.config.MarkdownConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownServiceImplTest {

    private final MarkdownConfiguration configuration = new MarkdownConfiguration();
    private final MarkdownService markdownService = new MarkdownServiceImpl(
            configuration.markdownParser(), configuration.markdownHtmlRenderer(), configuration.markdownHtmlCleaner()
    );

    @Test
    void preservesTableCodeAndHttpsLinkInCleanedFragment() {
        String markdown = """
                | 항목 | 내용 |
                | --- | --- |
                | 예시 | `MarkdownService` |

                [참고](https://example.com)
                """;

        String html = markdownService.render(markdown);
        Document fragment = Jsoup.parseBodyFragment(html);

        assertThat(html).doesNotContain("<html>", "<body>");
        assertThat(fragment.select("table th").eachText()).containsExactly("항목", "내용");
        assertThat(fragment.select("table code").text()).isEqualTo("MarkdownService");
        assertThat(fragment.select("a[href]").eachAttr("href")).containsExactly("https://example.com");
        assertThat(fragment.select("a").attr("rel")).isEqualTo("nofollow noopener noreferrer");
    }

    @Test
    void escapesRawHtmlAndDropsUnsafeLinkTargets() {
        String markdown = """
                <script>alert(1)</script>

                <p onclick="alert(1)">회의록</p>

                [위험](javascript:alert%281%29) [허용 안 된 링크](ftp://example.com) [참고](https://example.com)
                """;

        String html = markdownService.render(markdown);
        Document fragment = Jsoup.parseBodyFragment(html);

        assertThat(html).contains("&lt;script&gt;");
        assertThat(fragment.select("script, [onclick]")).isEmpty();
        assertThat(fragment.select("a[href]").eachAttr("href")).containsExactly("https://example.com");
    }

    @Test
    void returnsEmptyHtmlForEmptyMarkdown() {
        assertThat(markdownService.render("")).isEmpty();
    }
}
