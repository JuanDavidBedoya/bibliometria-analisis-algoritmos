package com.uniquindio.bibliometria.preprocessing;

/** Adjustable policy; minimal cleaning is the agreed default, other transformations are opt-in. */
public record PreprocessingOptions(boolean normalizeWhitespace, boolean lowercase,
                                   boolean normalizeUnicode, boolean joinLineHyphenation,
                                   boolean reconstructWordSpacing) {
    /** Compatibility constructor for callers selecting the original four operations explicitly. */
    public PreprocessingOptions(boolean normalizeWhitespace, boolean lowercase,
                                 boolean normalizeUnicode, boolean joinLineHyphenation) {
        this(normalizeWhitespace, lowercase, normalizeUnicode, joinLineHyphenation, false);
    }

    public static PreprocessingOptions minimalCleaning() {
        return new PreprocessingOptions(true, true, false, true, true);
    }

    public static PreprocessingOptions preserveText() {
        return new PreprocessingOptions(false, false, false, false, false);
    }
}
