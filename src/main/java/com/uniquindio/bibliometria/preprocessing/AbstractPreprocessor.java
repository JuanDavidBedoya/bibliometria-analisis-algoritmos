package com.uniquindio.bibliometria.preprocessing;

import com.uniquindio.bibliometria.ingestion.ExtractedArticle;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class AbstractPreprocessor {
    public ProcessedArticle process(ExtractedArticle article) {
        return process(article, PreprocessingOptions.minimalCleaning());
    }

    public ProcessedArticle process(ExtractedArticle article, PreprocessingOptions options) {
        Objects.requireNonNull(article, "article");
        Objects.requireNonNull(options, "options");
        if (!article.hasRequiredFields()) {
            throw new IllegalArgumentException("Review missing article fields before preprocessing");
        }
        String text = options.reconstructWordSpacing()
                ? mergeSpacingEvidence(article.originalAbstract(), article.spacingReconstructedAbstract())
                : article.originalAbstract();
        if (options.normalizeUnicode()) text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        // Apply before whitespace normalization: the line break is required evidence.
        if (options.joinLineHyphenation()) {
            text = text.replaceAll("(?<=[\\p{L}\\p{M}])[-\\u00ad\\u2010][\\t\\p{Zs}]*\\R[\\t\\p{Zs}]*(?=\\p{Ll})", "");
        }
        if (options.normalizeWhitespace()) {
            // Normal line endings are separators, never empty strings. Do this only
            // after resolving discretionary line-end hyphens above.
            text = text.replaceAll("\\R", " ")
                    .replaceAll("[\\s\\p{Z}]+", " ").strip();
        }
        if (options.lowercase()) text = text.toLowerCase(Locale.ROOT);
        return new ProcessedArticle(article, text, tokenize(text), options);
    }

    /** Keep boundaries from either reading, without changing letters or interpreting punctuation. */
    private String mergeSpacingEvidence(String original, String reconstructed) {
        StringBuilder result = new StringBuilder();
        int sourceIndex = 0, reconstructedIndex = 0;
        while (sourceIndex < original.length() || reconstructedIndex < reconstructed.length()) {
            int sourceGap = sourceIndex, reconstructedGap = reconstructedIndex;
            while (sourceIndex < original.length() && isSpace(original.codePointAt(sourceIndex))) {
                sourceIndex += Character.charCount(original.codePointAt(sourceIndex));
            }
            while (reconstructedIndex < reconstructed.length()
                    && isSpace(reconstructed.codePointAt(reconstructedIndex))) {
                reconstructedIndex += Character.charCount(reconstructed.codePointAt(reconstructedIndex));
            }
            // Source line breaks must survive until the hyphenation step runs.
            if (sourceIndex > sourceGap) result.append(original, sourceGap, sourceIndex);
            else if (reconstructedIndex > reconstructedGap) {
                result.append(reconstructed, reconstructedGap, reconstructedIndex);
            }
            boolean sourceEnd = sourceIndex == original.length();
            boolean reconstructedEnd = reconstructedIndex == reconstructed.length();
            if (sourceEnd || reconstructedEnd) {
                return sourceEnd && reconstructedEnd ? result.toString() : original;
            }
            int cp = original.codePointAt(sourceIndex);
            if (cp != reconstructed.codePointAt(reconstructedIndex)) {
                // A candidate changing non-space content is not spacing evidence.
                return original;
            }
            result.appendCodePoint(cp);
            sourceIndex += Character.charCount(cp);
            reconstructedIndex += Character.charCount(cp);
        }
        return result.toString();
    }

    private boolean isSpace(int cp) {
        return Character.isWhitespace(cp) || Character.isSpaceChar(cp) || cp == 0x85;
    }

    /** Explicit scan: letters, combining marks and numbers; internal hyphens/apostrophes remain. */
    public List<String> tokenize(String text) {
        Objects.requireNonNull(text, "text");
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < text.length();) {
            int cp = text.codePointAt(i);
            int nextIndex = i + Character.charCount(cp);
            int type = Character.getType(cp);
            boolean word = Character.isLetterOrDigit(cp) || (token.length() > 0 &&
                    (type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK));
            boolean connector = (cp == '-' || cp == '\u2011' || cp == '\'' || cp == '\u2019')
                    && token.length() > 0 && nextIndex < text.length()
                    && Character.isLetterOrDigit(text.codePointAt(nextIndex));
            if (word || connector) token.appendCodePoint(cp);
            else if (token.length() > 0) {
                result.add(token.toString());
                token.setLength(0);
            }
            i = nextIndex;
        }
        if (token.length() > 0) result.add(token.toString());
        return List.copyOf(result);
    }
}
