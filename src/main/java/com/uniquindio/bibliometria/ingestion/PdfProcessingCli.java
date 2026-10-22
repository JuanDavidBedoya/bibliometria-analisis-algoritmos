package com.uniquindio.bibliometria.ingestion;

import com.uniquindio.bibliometria.algorithms.similarity.classic.JaccardSimilarity;
import com.uniquindio.bibliometria.preprocessing.AbstractPreprocessor;
import com.uniquindio.bibliometria.preprocessing.PreprocessingOptions;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

/** Console entry point: reads an external ZIP/directory without copying PDFs or saving results. */
public final class PdfProcessingCli {
    private PdfProcessingCli() { }

    public static void main(String[] args) {
        try {
            run(args, System.out);
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("Error: " + error.getMessage());
            System.exit(1);
        }
    }

    public static void run(String[] args, PrintStream output) throws IOException {
        if (args.length < 2 || !(args[0].equals("inspect") || args[0].equals("compare"))) {
            throw new IllegalArgumentException("Usage: inspect <ZIP/directory> [article number] [options] | "
                    + "compare <ZIP/directory> <left number> <right number> "
                    + "[--preserve-text] [--whitespace] [--lowercase] [--unicode] [--join-hyphenation] [--reconstruct-spacing] [--empty=REJECT|ZERO|ONE]");
        }
        boolean compare = args[0].equals("compare");
        if (compare && args.length < 4) throw new IllegalArgumentException("Select two articles");
        var defaults = PreprocessingOptions.minimalCleaning();
        boolean whitespace = defaults.normalizeWhitespace(), lowercase = defaults.lowercase();
        boolean unicode = defaults.normalizeUnicode(), hyphens = defaults.joinLineHyphenation();
        boolean spacing = defaults.reconstructWordSpacing();
        var emptyPolicy = JaccardSimilarity.EmptyUnionPolicy.REJECT;
        boolean inspectArticle = !compare && args.length > 2 && !args[2].startsWith("--");
        for (int i = compare ? 4 : inspectArticle ? 3 : 2; i < args.length; i++) {
            switch (args[i]) {
                case "--preserve-text" -> {
                    whitespace = false;
                    lowercase = false;
                    unicode = false;
                    hyphens = false;
                    spacing = false;
                }
                case "--whitespace" -> whitespace = true;
                case "--lowercase" -> lowercase = true;
                case "--unicode" -> unicode = true;
                case "--join-hyphenation" -> hyphens = true;
                case "--reconstruct-spacing" -> spacing = true;
                default -> {
                    if (!args[i].startsWith("--empty=")) {
                        throw new IllegalArgumentException("Unknown option: " + args[i]);
                    }
                    emptyPolicy = JaccardSimilarity.EmptyUnionPolicy.valueOf(
                            args[i].substring(8).toUpperCase(Locale.ROOT));
                }
            }
        }
        List<Integer> requested = compare
                ? List.of(articleNumber(args[2]), articleNumber(args[3]))
                : inspectArticle ? List.of(articleNumber(args[2])) : List.of();
        var loaded = loadSelected(Path.of(args[1]), requested, new DocumentPipeline(
                new PdfBoxTextReader(), new ArticleExtractor(), new AbstractPreprocessor()));
        List<ExtractedArticle> articles = loaded.articles();
        var options = new PreprocessingOptions(whitespace, lowercase, unicode, hyphens, spacing);
        if (!compare) {
            output.println("PDFs in corpus: " + loaded.totalCount() + "; processed: "
                    + loaded.processedCount() + "; extraction pending manual review");
            if (inspectArticle) printArticle(articles.get(0), options, output);
            else for (int i = 0; i < articles.size(); i++) {
                var a = articles.get(i);
                output.printf("%02d | %s | authors=%d | abstract=%d chars | %s%n", i + 1,
                        a.title().isBlank() ? a.source() : a.title(), a.authors().size(),
                        a.originalAbstract().length(), a.hasRequiredFields() ? "REVIEW" : "MISSING FIELDS");
            }
            return;
        }
        var preprocessor = new AbstractPreprocessor();
        var left = preprocessor.process(articles.get(0), options);
        var right = preprocessor.process(articles.get(1), options);
        var result = new JaccardSimilarity().compare(left.tokens(), right.tokens(), emptyPolicy);
        output.println("Jaccard comparison.");
        output.println("A: " + left.article().source());
        output.println("B: " + right.article().source());
        output.println("Options: " + options + "; empty union policy=" + emptyPolicy);
        output.println("Processed abstract A: " + left.processedAbstract());
        output.println("Processed abstract B: " + right.processedAbstract());
        output.println("A = " + result.leftSet());
        output.println("B = " + result.rightSet());
        output.println("Intersection = " + result.intersection());
        output.println("Union = " + result.union());
        output.printf(Locale.ROOT, "J(A,B) = %d / %d = %.12f%n",
                result.intersection().size(), result.union().size(), result.similarity());
    }

    public static List<ExtractedArticle> loadCorpus(Path path) throws IOException {
        var pipeline = new DocumentPipeline(new PdfBoxTextReader(), new ArticleExtractor(),
                new AbstractPreprocessor());
        return loadSelected(path, List.of(), pipeline).articles();
    }

    record LoadedCorpus(int totalCount, int processedCount, List<ExtractedArticle> articles) {
        LoadedCorpus { articles = List.copyOf(articles); }
    }

    /** Enumerates names first; only requested PDF payloads are read and decoded. */
    static LoadedCorpus loadSelected(Path path, List<Integer> requested, DocumentPipeline pipeline)
            throws IOException {
        List<ExtractedArticle> result = new ArrayList<>();
        var cache = new HashMap<Integer, ExtractedArticle>();
        int total;
        if (Files.isDirectory(path)) {
            try (var files = Files.list(path)) {
                var entries = files.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString(), SOURCE_ORDER)).toList();
                total = entries.size();
                for (int index : selectedNumbers(total, requested, path)) {
                    if (!cache.containsKey(index)) {
                        Path pdf = entries.get(index - 1);
                        cache.put(index, extractSafely(pipeline, pdf.toString(), Files.readAllBytes(pdf)));
                    }
                    result.add(cache.get(index));
                }
            }
        } else {
            try (var zip = new ZipFile(path.toFile())) {
                var entries = zip.stream().filter(e -> !e.isDirectory()
                        && e.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                        .sorted(Comparator.comparing(e -> e.getName(), SOURCE_ORDER)).toList();
                total = entries.size();
                for (int index : selectedNumbers(total, requested, path)) {
                    if (!cache.containsKey(index)) {
                        var entry = entries.get(index - 1);
                        try (var stream = zip.getInputStream(entry)) {
                            cache.put(index, extractSafely(pipeline, entry.getName(), stream.readAllBytes()));
                        }
                    }
                    result.add(cache.get(index));
                }
            }
        }
        return new LoadedCorpus(total, cache.size(), result);
    }

    private static List<Integer> selectedNumbers(int total, List<Integer> requested, Path path)
            throws IOException {
        if (total == 0) throw new IOException("No PDFs found in " + path);
        if (requested.isEmpty()) return java.util.stream.IntStream.rangeClosed(1, total).boxed().toList();
        for (int index : requested) {
            if (index < 1 || index > total) throw new IllegalArgumentException("Article out of range: " + index);
        }
        return requested;
    }

    private static ExtractedArticle extractSafely(DocumentPipeline pipeline, String source, byte[] pdf) {
        try {
            return pipeline.extract(source, pdf);
        } catch (IOException error) {
            return new ExtractedArticle(source, "", List.of(), "",
                    List.of("PDF read failed: " + error.getMessage()));
        }
    }

    private static int articleNumber(String number) {
        int index;
        try { index = Integer.parseInt(number); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("Invalid article number: " + number); }
        if (index < 1) throw new IllegalArgumentException("Article out of range: " + index);
        return index;
    }

    private static void printArticle(ExtractedArticle article, PreprocessingOptions options, PrintStream output) {
        output.println("Source: " + article.source());
        output.println("Title: " + article.title());
        output.println("Authors: " + article.authors());
        output.println("Original abstract:\n" + article.originalAbstract());
        if (article.hasRequiredFields()) {
            var processed = new AbstractPreprocessor().process(article, options);
            output.println("Preprocessing options: " + options);
            output.println("Processed abstract:\n" + processed.processedAbstract());
            output.println("Token count: " + processed.tokens().size());
            output.println("Tokens: " + formatTokens(processed.tokens()));
        } else output.println("Processed abstract unavailable: review missing fields.");
        output.println("Review notes: " + article.reviewNotes());
    }

    static String formatTokens(List<String> tokens) {
        return tokens.stream().map(token -> "\"" + token.replace("\\", "\\\\")
                        .replace("\"", "\\\"") + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static final Comparator<String> SOURCE_ORDER = Comparator
            .comparingInt(PdfProcessingCli::sourceNumber).thenComparing(Comparator.naturalOrder());

    private static int sourceNumber(String source) {
        String name = source.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        int end = 0;
        while (end < name.length() && Character.isDigit(name.charAt(end))) end++;
        try { return end == 0 ? Integer.MAX_VALUE : Integer.parseInt(name.substring(0, end)); }
        catch (NumberFormatException error) { return Integer.MAX_VALUE; }
    }
}
