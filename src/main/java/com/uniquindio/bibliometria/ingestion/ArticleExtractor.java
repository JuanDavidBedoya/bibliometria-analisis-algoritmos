package com.uniquindio.bibliometria.ingestion;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** General layout rules, with explicit review notes instead of invented missing values. */
public final class ArticleExtractor {
    private static final Pattern ABSTRACT = Pattern.compile(
            "(?i)^\\s*a\\s*b\\s*s\\s*t\\s*r\\s*a\\s*c\\s*t\\s*(?::\\s*(.*))?$");
    private static final Pattern BOUNDARY = Pattern.compile(
            "(?i)^(?:k\\s*e\\s*y\\s*w\\s*o\\s*r\\s*d\\s*s?\\b.*|(?:\\d+[.\\s|]+)?introduction\\s*|paper\\s*type\\b.*)$");
    private static final Pattern AFFILIATION = Pattern.compile(
            "(?i).*(department|university|institute|faculty|college|school of|correspondence|accepted:|published online:|copyright|author\\(s\\)).*");

    public ExtractedArticle extract(PdfTextDocument document) {
        List<String> notes = new ArrayList<>();
        notes.add("Manual verification of title, authors and abstract is pending.");
        if (document.pages().isEmpty()) return missing(document.source(), notes);
        PdfPage page = document.pages().get(0);
        List<PdfPage.Line> lines = page.lines();
        PdfPage.Line heading = lines.stream().filter(l -> ABSTRACT.matcher(l.text()).matches())
                .findFirst().orElse(null);
        if (heading == null) {
            notes.add("No separate abstract heading found on the first page; review the layout.");
        }
        float abstractY = heading == null ? page.height() : heading.y();
        List<PdfPage.Line> titleLines = titleLines(document.metadataTitle(), lines, abstractY);
        String title = join(titleLines, " ");
        if (title.isBlank()) notes.add("Title not identified.");
        if (!document.metadataTitle().isBlank() && !key(title).equals(key(document.metadataTitle()))) {
            notes.add("Visible title differs from PDF metadata; use the visible title for review.");
        }
        List<String> authors = authors(lines, titleLines, abstractY);
        if (authors.isEmpty()) notes.add("Authors not identified.");
        String abstractText = heading == null ? "" : abstractText(lines, heading, notes);
        // Select exactly the same abstract region; only its word-spacing evidence differs.
        String reconstructed = heading == null ? "" : abstractText(lines, heading, new ArrayList<>(), true);
        if (abstractText.isBlank()) notes.add("Abstract body not identified.");
        if (title.indexOf('\ufffd') >= 0 || abstractText.indexOf('\ufffd') >= 0
                || authors.stream().anyMatch(a -> a.indexOf('\ufffd') >= 0)) {
            notes.add("PDF text contains unmapped characters; compare against the visible PDF.");
        }
        return new ExtractedArticle(document.source(), title, authors, abstractText, notes, reconstructed);
    }

    private ExtractedArticle missing(String source, List<String> notes) {
        return new ExtractedArticle(source, "", List.of(), "", notes);
    }

    private List<PdfPage.Line> titleLines(String metadata, List<PdfPage.Line> lines, float beforeY) {
        String metaKey = key(metadata);
        List<PdfPage.Line> candidates = lines.stream().filter(l -> l.y() < beforeY
                && key(l.text()).length() > 15).toList();
        PdfPage.Line start = candidates.stream().filter(l -> {
            String k = key(l.text());
            return !metaKey.isEmpty() && metaKey.startsWith(k.substring(0, Math.min(30, k.length())));
        }).max(Comparator.comparing(PdfPage.Line::fontSize)).orElse(null);
        if (start == null) {
            start = candidates.stream().filter(l -> !l.text().contains("http")
                    && !l.text().toLowerCase(Locale.ROOT).contains("journal homepage"))
                    .max(Comparator.comparing(PdfPage.Line::fontSize)).orElse(null);
        }
        if (start == null) return List.of();
        List<PdfPage.Line> result = new ArrayList<>();
        for (PdfPage.Line line : lines) {
            if (line.y() < start.y() - 1) continue;
            if (line.y() >= beforeY) break;
            if (Math.abs(line.fontSize() - start.fontSize()) < .8f
                    && (result.isEmpty() || line.y() - result.get(result.size()-1).y() <= 35)) {
                result.add(line);
            } else if (!result.isEmpty() && line.y() - result.get(result.size()-1).y() > 10) {
                break;
            }
        }
        return result;
    }

    private List<String> authors(List<PdfPage.Line> lines, List<PdfPage.Line> title, float abstractY) {
        if (title.isEmpty()) return List.of();
        float bottom = title.get(title.size()-1).y();
        List<PdfPage.Line> candidates = lines.stream().filter(l -> l.y() > bottom + 3
                && l.y() < Math.min(abstractY, bottom + 65)
                && key(l.text()).length() > 8 && !AFFILIATION.matcher(l.text()).matches()).toList();
        PdfPage.Line first = candidates.stream().max(Comparator.comparing(PdfPage.Line::fontSize))
                .orElse(null);
        if (first == null) return List.of();
        List<PdfPage.Line> block = new ArrayList<>();
        for (PdfPage.Line line : candidates) {
            if (line.y() >= first.y() - 2 && line.y() <= first.y() + 22
                    && Math.abs(line.fontSize() - first.fontSize()) < .8f) block.add(line);
        }
        String text = join(block, " ").replaceAll("[\\d*]+", "")
                .replaceAll("(?<=\\s)[a-f](?=\\s*[,;]|$)", "")
                .replaceAll("[\\s\\p{Z}]+", " ");
        List<String> result = new ArrayList<>();
        for (String author : text.split("\\s*(?:[,;|\\u00b7]|\\band\\b)\\s*")) {
            author = author.strip();
            if (author.codePoints().filter(Character::isLetter).count() >= 4) result.add(author);
        }
        return result;
    }

    private String abstractText(List<PdfPage.Line> lines, PdfPage.Line heading, List<String> notes) {
        return abstractText(lines, heading, notes, false);
    }

    private String abstractText(List<PdfPage.Line> lines, PdfPage.Line heading, List<String> notes,
                                boolean reconstructSpacing) {
        List<String> body = new ArrayList<>();
        var match = ABSTRACT.matcher(heading.text());
        match.matches();
        if (match.group(1) != null && !match.group(1).isBlank()) {
            var reconstructedHeading = ABSTRACT.matcher(heading.reconstructedText());
            String firstLine = reconstructSpacing && reconstructedHeading.matches()
                    ? reconstructedHeading.group(1) : match.group(1);
            body.add(firstLine == null ? match.group(1) : firstLine);
        }
        float lastY = heading.y();
        float leading = 0;
        boolean bounded = false;
        for (PdfPage.Line line : lines) {
            if (line.y() <= heading.y() + 2) continue;
            boolean sameColumn = Math.abs(line.x() - heading.x()) <= 15;
            if (BOUNDARY.matcher(line.text()).matches()
                    && (sameColumn || (!body.isEmpty() && line.y() - lastY > 15))) {
                bounded = true;
                break;
            }
            if (!sameColumn) continue;
            if (!body.isEmpty() && line.y() - lastY > Math.max(22, leading * 1.9f)) {
                notes.add("Abstract end inferred from a vertical layout gap; verify its boundary.");
                bounded = true;
                break;
            }
            if (!body.isEmpty()) leading = line.y() - lastY;
            body.add(reconstructSpacing ? line.reconstructedText() : line.text());
            lastY = line.y();
        }
        if (!bounded) notes.add("No explicit abstract end found on this page; review completeness.");
        // Line breaks and glyph content are retained; cleaning happens only in preprocessing.
        return String.join("\n", body);
    }

    private String join(List<PdfPage.Line> lines, String separator) {
        return String.join(separator, lines.stream().map(PdfPage.Line::text).toList());
    }

    private String key(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }
}
