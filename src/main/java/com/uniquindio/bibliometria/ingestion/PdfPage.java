package com.uniquindio.bibliometria.ingestion;

import java.util.List;

/** Text and geometry from one PDF page, before preprocessing. Coordinates are points. */
public record PdfPage(int number, float width, float height, String text, List<Line> lines) {
    public PdfPage {
        lines = List.copyOf(lines);
    }

    public record Line(String text, float x, float y, float endX, float fontSize,
                       String reconstructedText) {
        public Line(String text, float x, float y, float endX, float fontSize) {
            this(text, x, y, endX, fontSize, text);
        }
    }
}
