package com.uniquindio.bibliometria.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class PdfProcessingCliTest {
    @TempDir Path directory;

    @Test void quotedDisplayDistinguishesTwoTokensFromOneTokenContainingComma() {
        assertEquals("[\"how\", \"ai\"]", PdfProcessingCli.formatTokens(List.of("how", "ai")));
        assertEquals("[\"how,ai\"]", PdfProcessingCli.formatTokens(List.of("how,ai")));
    }

    @Test void emptyCorpusIsReported() throws Exception {
        var empty = directory.resolve("empty.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(empty))) { }
        assertThrows(IOException.class, () -> PdfProcessingCli.loadCorpus(empty));
    }
    @Test void failedPdfIsVisibleAndZipEntriesAreNeverExtractedToDisk() throws Exception {
        var corpus = directory.resolve("corpus.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(corpus))) {
            zip.putNextEntry(new ZipEntry("../broken.pdf"));
            zip.write(new byte[]{1,2,3});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("note.txt"));
            zip.write(new byte[]{4});
            zip.closeEntry();
        }
        var articles = PdfProcessingCli.loadCorpus(corpus);
        assertEquals(1, articles.size());
        assertFalse(articles.get(0).hasRequiredFields());
        assertTrue(articles.get(0).reviewNotes().get(0).contains("read failed"));
        assertFalse(Files.exists(directory.resolve("broken.pdf")));
        try (var paths = Files.list(directory)) { assertEquals(1, paths.count()); }
    }
    @Test void invalidCommandsFailBeforeLoadingCorpus() {
        var output = new PrintStream(new ByteArrayOutputStream());
        assertThrows(IllegalArgumentException.class, () -> PdfProcessingCli.run(new String[0], output));
        assertThrows(IllegalArgumentException.class, () -> PdfProcessingCli.run(
                new String[]{"compare", "missing.zip", "1", "2", "--unknown"}, output));
        assertThrows(IllegalArgumentException.class, () -> PdfProcessingCli.run(
                new String[]{"inspect", "missing.zip", "0"}, output));
        assertThrows(IllegalArgumentException.class, () -> PdfProcessingCli.run(
                new String[]{"inspect", "missing.zip", "abc"}, output));
    }

    private DocumentPipeline recordingPipeline(List<String> reads) {
        return new DocumentPipeline((source, bytes) -> {
            reads.add(source);
            assertArrayEquals(new byte[]{1,2,3}, bytes);
            return new PdfTextDocument(source, "", List.of());
        }, new ArticleExtractor(), new com.uniquindio.bibliometria.preprocessing.AbstractPreprocessor());
    }

    private Path selectionZip() throws IOException {
        Path corpus = directory.resolve("selected.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(corpus))) {
            for (String name : List.of("10. third.pdf", "2. second.PDF", "1. first.pdf", "note.txt")) {
                zip.putNextEntry(new ZipEntry(name));
                zip.write(new byte[]{1,2,3});
                zip.closeEntry();
            }
        }
        return corpus;
    }

    @Test void inspectingOneArticleDecodesOnlyThatPdfFromZip() throws Exception {
        var reads = new ArrayList<String>();
        var result = PdfProcessingCli.loadSelected(selectionZip(), List.of(2), recordingPipeline(reads));
        assertEquals(3, result.totalCount());
        assertEquals(1, result.processedCount());
        assertEquals(List.of("2. second.PDF"), reads);
        assertEquals("2. second.PDF", result.articles().get(0).source());
    }

    @Test void selectedPairPreservesOrderAndRepeatedArticleIsDecodedOnce() throws Exception {
        var reads = new ArrayList<String>();
        var result = PdfProcessingCli.loadSelected(selectionZip(), List.of(3,1,3), recordingPipeline(reads));
        assertEquals(List.of("10. third.pdf", "1. first.pdf"), reads);
        assertEquals(2, result.processedCount());
        assertEquals(List.of("10. third.pdf", "1. first.pdf", "10. third.pdf"),
                result.articles().stream().map(ExtractedArticle::source).toList());
        assertSame(result.articles().get(0), result.articles().get(2));
    }

    @Test void outOfRangeSelectionFailsBeforeReadingPdfPayloads() throws Exception {
        var reads = new ArrayList<String>();
        Path corpus = selectionZip();
        assertThrows(IllegalArgumentException.class, () -> PdfProcessingCli.loadSelected(
                corpus, List.of(1,4), recordingPipeline(reads)));
        assertTrue(reads.isEmpty());
    }

    @Test void directorySelectionAlsoReadsOnlyRequestedPdf() throws Exception {
        for (String name : List.of("10. third.pdf", "2. second.pdf", "1. first.pdf")) {
            Files.write(directory.resolve(name), new byte[]{1,2,3});
        }
        var reads = new ArrayList<String>();
        var result = PdfProcessingCli.loadSelected(directory, List.of(1), recordingPipeline(reads));
        assertEquals(3, result.totalCount());
        assertEquals(1, reads.size());
        assertTrue(reads.get(0).endsWith("1. first.pdf"));
    }
}
