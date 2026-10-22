package com.uniquindio.bibliometria.ingestion;

import com.uniquindio.bibliometria.preprocessing.AbstractPreprocessor;
import com.uniquindio.bibliometria.preprocessing.PreprocessingOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** External corpus required: -Dbibliometria.corpus=<ZIP/directory>. No PDF copies in the repo. */
class CorpusExtractionTest {
    private static List<ExtractedArticle> articles;
    private static final int[] AUTHOR_COUNTS = {4,5,4,5,4,3,7,3,2,2,5,9,2,3,5,2,3,4,1,4};
    private static final List<List<String>> AUTHORS = List.of(
            List.of("Matthias Carl Laupichler", "Alexandra Aster", "Jana Schirch", "Tobias Raupach"),
            List.of("Thomas K.F. Chiu", "Qi Xia", "Xinyan Zhou", "Ching Sing Chai", "Miaoting Cheng"),
            List.of("Arne Bewersdorff", "Xiaoming Zhai", "Jessica Roberts", "Claudia Nerdel"),
            List.of("Srecko Joksimovic", "Dirk Ifenthaler", "Rebecca Marrone", "Maarten De Laat", "George Siemens"),
            List.of("Xieling Chen", "Haoran Xie", "Di Zou", "Gwo-Jen Hwang"),
            List.of("Xieling Chen", "Haoran Xie", "Gwo-Jen Hwang"),
            List.of("Riordan Alfredo", "Vanessa Echeverria", "Yueqiao Jin", "Lixiang Yan", "Zachari Swiecki", "Dragan Gašević", "Roberto Martinez-Maldonado"),
            List.of("Seng Chee Tan", "Alwyn Vwen Yen Lee", "Min Lee"),
            List.of("Ke Zhang", "Ayse Begum Aslan"), List.of("Chih-Pu Dai", "Fengfeng Ke"),
            List.of("Qi Xia", "Xiaojing Weng", "Fan Ouyang", "Tzung Jin Lin", "Thomas K.F. Chiu"),
            List.of("Melissa Bond", "Hassan Khosravi", "Maarten De Laat", "Nina Bergdahl", "Violeta Negrea", "Emily Oxley", "Phuong Pham", "Sin Wang Chong", "George Siemens"),
            List.of("Jyoti Mudkanna Gavhane", "Reena Pagare"),
            List.of("Tamar Mikeladze", "Paulien C. Meijer", "Roald P. Verhoeff"),
            List.of("Bayode Ogunleye", "Kudirat Ibilola Zakariyyah", "Oluwaseun Ajao", "Olakunle Olayinka", "Hemlata Sharma"),
            List.of("Che Yee Lye", "Lyndon Lim"), List.of("Fan Ouyang", "Tuan Anh Dinh", "Weiqi Xu"),
            List.of("Zied Bahroun", "Chiraz Anane", "Vian Ahmed", "Andrew Zacca"), List.of("Chung Kwan Lo"),
            List.of("Olaf Zawacki-Richter", "Victoria I. Marín", "Melissa Bond", "Franziska Gouverneur")
    );
    private static final String[] TITLES = {
            "Artificial intelligence literacy in higher and adult education: A scoping literature review",
            "Systematic literature review on opportunities, challenges, and future research recommendations of artificial intelligence in education",
            "Myths, mis- and preconceptions of artificial intelligence: A review of the literature",
            "Opportunities of artificial intelligence for supporting complex problem-solving: Findings from a scoping review",
            "Application and theory gaps during the rise of Artificial Intelligence in Education",
            "A multi-perspective study on Artificial Intelligence in Education: grants, conferences, journals, software tools, institutions, and researchers",
            "Human-centred learning analytics and AI in education: A systematic literature review",
            "A systematic review of artificial intelligence techniques for collaborative learning over the past two decades",
            "AI technologies for education: Recent research & future directions",
            "Educational applications of artificial intelligence in simulation-based learning: A systematic mapping review",
            "A scoping review on how generative artificial intelligence transforms assessment in higher education",
            "A meta systematic review of artificial intelligence in higher education: a call for increased ethics, collaboration, and rigour",
            "Artificial intelligence for education and its emphasis on assessment and adversity quotient: a review",
            "A comprehensive exploration of artificial intelligence competence frameworks for educators: A critical review",
            "A Systematic Review of Generative AI for Teaching and Learning Practice",
            "Generative Artificial Intelligence in Tertiary Education: Assessment Redesign Principles and Considerations",
            "A Systematic Review of AI‑Driven Educational Assessment in STEM Education",
            "Transforming Education: A Comprehensive Review of Generative Artificial Intelligence in Educational Settings through Bibliometric and Content Analysis",
            "What Is the Impact of ChatGPT on Education? A Rapid Review of the Literature",
            "Systematic review of research on artificial intelligence applications in higher education – where are the educators?"
    };
    // Reference boundaries read from the corpus, independent of the extraction implementation.
    private static final String[] STARTS = {
            "Since artificial intelligence", "Applications of artificial intelligence",
            "Artificial Intelligence (AI) is prevalent", "The research objective of this paper",
            "Considering the increasing importance", "With the rapid development",
            "The rapid expansion of Learning Analytics", "This systematic review focuses",
            "From unique educational perspectives", "The field of education has experienced",
            "Generative artificial intelligence provides", "Although the field of Artificial Intelligence",
            "Purpose", "Recent literature underscores", "The use of generative artificial intelligence",
            "The emergence of generative artificial intelligence", "Artificial intelligence (AI), as an emerging technology",
            "In the ever-evolving era", "An artificial intelligence-based chatbot", "According to various international reports"
    };
    private static final String[] ENDS = {
            "results.", "directions of AIEd research.", "education.",
            "Implications for future research and practice are being discussed.", "nologies with educational theories.",
            "Computers & Education: Artificial Intelligence.", "future human-centred LA/AIED systems.",
            "suggest future areas of study.", "opment efforts.", "simulation-based learning.",
            "provide more inter-disciplinary programs and teaching.", "and secondary research.",
            "educational assessment and the evaluation of AQ.", "teaching practices.",
            "guidelines, frameworks, and policies for GenAI usage.", "in assessments.",
            "educational assessment in STEM education.", "the intersection of GAI and education.",
            "ChatGPT on the educational environment.", "in higher education."
    };

    @BeforeAll static void load() throws Exception {
        String corpus = System.getProperty("bibliometria.corpus");
        assumeTrue(corpus != null && !corpus.isBlank(), "External PDFs were not provided");
        articles = PdfProcessingCli.loadCorpus(Path.of(corpus));
        assertEquals(20, articles.size(), "The controlled corpus must contain 20 documents");
    }

    @TestFactory Stream<DynamicTest> extractionBoundariesForEveryDocument() {
        return IntStream.range(0, 20).mapToObj(i -> DynamicTest.dynamicTest("Article " + (i + 1), () -> {
            var article = articles.get(i);
            assertTrue(article.hasRequiredFields(), article.reviewNotes().toString());
            assertEquals(AUTHOR_COUNTS[i], article.authors().size(), article.authors().toString());
            assertEquals(normalized(TITLES[i]), normalized(article.title()));
            assertEquals(AUTHORS.get(i).stream().map(CorpusExtractionTest::normalized).toList(),
                    article.authors().stream().map(CorpusExtractionTest::normalized).toList());
            String text = normalized(article.originalAbstract());
            assertTrue(text.startsWith(STARTS[i]), text.substring(0, Math.min(150, text.length())));
            assertTrue(text.endsWith(ENDS[i]), text.substring(Math.max(0, text.length() - 180)));
            assertFalse(text.matches("(?is).*\\bkeywords\\s*:.*"));
            assertFalse(text.matches("(?is).*\\b1\\. Introduction\\b.*"));
            assertTrue(article.reviewNotes().stream().anyMatch(n -> n.contains("Manual verification")));
        }));
    }

    @Test void titleFallbackAndComplexAuthorBlocks() {
        assertEquals("Artificial intelligence for education and its emphasis on assessment and adversity quotient: a review",
                normalized(articles.get(12).title()));
        assertEquals(List.of("Tamar Mikeladze", "Paulien C. Meijer", "Roald P. Verhoeff"), articles.get(13).authors());
        assertEquals("Chung Kwan Lo", articles.get(18).authors().get(0));
    }

    @Test void visibleLineEndHyphensAreKeptInOriginalExtraction() {
        assertTrue(articles.get(1).originalAbstract().contains("practi-\ntioners"));
        assertTrue(articles.get(8).originalAbstract().contains("devel-\nopment"));
        var processed = new AbstractPreprocessor().process(articles.get(1),
                new PreprocessingOptions(true, false, false, true));
        assertTrue(processed.tokens().contains("practitioners"));
        assertTrue(articles.get(1).originalAbstract().contains("practi-\ntioners"));
    }

    @Test void smallWordGapsArePreserved() {
        assertTrue(normalized(articles.get(0).originalAbstract()).contains("recommendations for research and practice"));
        assertFalse(articles.get(0).originalAbstract().contains("researchand"));
        String abstractText = normalized(articles.get(12).originalAbstract());
        assertTrue(abstractText.contains("Education is a vital tool to"));
        assertTrue(abstractText.contains("skills for success"));
    }

    @Test void articleTwoIsReconstructedWithoutChangingOriginal() {
        var article = articles.get(1);
        String original = article.originalAbstract();
        var result = new AbstractPreprocessor().process(article);
        assertTrue(original.contains("practi-\ntioners"));
        assertTrue(original.contains("re-\nlationships"));
        assertEquals(original, result.article().originalAbstract());
        assertTrue(result.tokens().contains("practitioners"));
        assertTrue(result.tokens().contains("relationships"));
        assertTrue(result.processedAbstract().contains("also provides"));
        assertFalse(result.processedAbstract().contains("alsoprovides"));
        assertTrue(result.processedAbstract().contains("analysis approaches. the results"));
        int sentenceEnd = result.processedAbstract().indexOf("analysis approaches.")
                + "analysis approaches.".length();
        assertEquals(' ', result.processedAbstract().charAt(sentenceEnd),
                "A normal sentence boundary must contain a literal U+0020 space");
        assertFalse(result.processedAbstract().contains("approaches.the"));
        int how = result.tokens().indexOf("how");
        assertTrue(how >= 0 && how + 1 < result.tokens().size());
        assertEquals("ai", result.tokens().get(how + 1));
        assertFalse(result.tokens().contains("how,ai"));
        assertFalse(result.tokens().contains("howai"));
        assertTrue(PdfProcessingCli.formatTokens(result.tokens()).contains("\"how\", \"ai\""));
        assertFalse(result.processedAbstract().matches("(?s).*[\\s\\p{Z}]{2,}.*"));
        assertFalse(result.processedAbstract().contains("\n"));
        assertEquals(5, article.authors().size());
    }

    @Test void articleTwoConsoleOutputPreservesSentenceSpaceAndOriginalText() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            PdfProcessingCli.run(new String[]{"inspect", System.getProperty("bibliometria.corpus"), "2"}, output);
        }
        String printed = bytes.toString(StandardCharsets.UTF_8);
        String marker = "Processed abstract:\n";
        int start = printed.indexOf(marker);
        assertTrue(start >= 0);
        String processed = printed.substring(start + marker.length()).lines().findFirst().orElseThrow();
        assertTrue(processed.contains("analysis approaches. the results"), processed);
        assertFalse(processed.contains("approaches.the results"));
        int afterPeriod = processed.indexOf("approaches.") + "approaches.".length();
        assertEquals(0x20, processed.codePointAt(afterPeriod),
                "The actual printed text must include an ASCII space after the period");
        assertTrue(printed.contains("Original abstract:\n" + articles.get(1).originalAbstract()
                + System.lineSeparator()), "Console must print the unchanged original abstract");
    }

    private static String normalized(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).replaceAll("[\\s\\p{Z}]+", " ").strip();
    }
}
