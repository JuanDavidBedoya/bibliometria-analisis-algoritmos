package com.uniquindio.bibliometria.ingestion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** PDFBox only decodes PDF text; metadata extraction is implemented separately. */
public final class PdfBoxTextReader implements PdfTextReader {
    @Override
    public PdfTextDocument read(String source, byte[] pdf) throws IOException {
        Objects.requireNonNull(pdf, "pdf");
        try (PDDocument document = Loader.loadPDF(pdf)) {
            if (!document.getCurrentAccessPermission().canExtractContent()) {
                throw new IOException("The PDF does not allow text extraction: " + source);
            }
            List<PdfPage> pages = new ArrayList<>();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PositionStripper stripper = new PositionStripper();
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);
                var box = document.getPage(i).getCropBox();
                pages.add(new PdfPage(i + 1, box.getWidth(), box.getHeight(), text,
                        stripper.lines()));
            }
            return new PdfTextDocument(source, document.getDocumentInformation().getTitle(), pages);
        }
    }

    private static final class PositionStripper extends PDFTextStripper {
        private final List<TextPosition> positions = new ArrayList<>();
        private final Set<TextPosition> wordStarts = Collections.newSetFromMap(new IdentityHashMap<>());

        private PositionStripper() throws IOException { }

        @Override
        public void beginMarkedContentSequence(COSName tag, COSDictionary properties) {
            // PDFBox removes soft hyphens from ActualText. Preserve their visible form
            // in a copied dictionary so source PDF data is never changed.
            if (properties != null) {
                String actualText = properties.getString(COSName.ACTUAL_TEXT);
                if (actualText != null && actualText.indexOf('\u00ad') >= 0) {
                    properties = new COSDictionary(properties);
                    properties.setString(COSName.ACTUAL_TEXT, actualText.replace('\u00ad', '-'));
                }
            }
            super.beginMarkedContentSequence(tag, properties);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            // PDFTextStripper emits one word at a time. Retain those boundaries as
            // reconstruction evidence without changing the original glyph-based text.
            if (!textPositions.isEmpty()) wordStarts.add(textPositions.get(0));
            positions.addAll(textPositions);
            super.writeString(text, textPositions);
        }

        private List<PdfPage.Line> lines() {
            positions.sort(Comparator.comparing(TextPosition::getYDirAdj)
                    .thenComparing(TextPosition::getXDirAdj));
            List<List<TextPosition>> rows = new ArrayList<>();
            for (TextPosition position : positions) {
                if (rows.isEmpty() || Math.abs(position.getYDirAdj()
                        - rows.get(rows.size() - 1).get(0).getYDirAdj()) > 2.5f) {
                    rows.add(new ArrayList<>());
                }
                rows.get(rows.size() - 1).add(position);
            }
            List<PdfPage.Line> result = new ArrayList<>();
            for (List<TextPosition> row : rows) {
                row.sort(Comparator.comparing(TextPosition::getXDirAdj));
                List<TextPosition> segment = new ArrayList<>();
                float endX = -1;
                for (TextPosition p : row) {
                    // A wide gutter separates columns; ordinary word spaces remain in a line.
                    if (!segment.isEmpty() && p.getXDirAdj() - endX > 18) {
                        result.add(line(segment));
                        segment.clear();
                    }
                    segment.add(p);
                    endX = Math.max(endX, p.getXDirAdj() + p.getWidthDirAdj());
                }
                if (!segment.isEmpty()) result.add(line(segment));
            }
            result.sort(Comparator.comparing(PdfPage.Line::y).thenComparing(PdfPage.Line::x));
            return result;
        }

        private PdfPage.Line line(List<TextPosition> chars) {
            StringBuilder text = new StringBuilder();
            StringBuilder reconstructed = new StringBuilder();
            float end = chars.get(0).getXDirAdj();
            float font = 0;
            for (TextPosition p : chars) {
                boolean gap = p.getXDirAdj() - end > Math.max(.3f, p.getWidthOfSpace() * .3f);
                if (text.length() > 0 && gap) {
                    text.append(' ');
                }
                if (reconstructed.length() > 0 && (gap || wordStarts.contains(p))
                        && !endsWithSpace(reconstructed) && !p.getUnicode().isBlank()) {
                    reconstructed.append(' ');
                }
                text.append(p.getUnicode());
                reconstructed.append(p.getUnicode());
                end = Math.max(end, p.getXDirAdj() + p.getWidthDirAdj());
                // Height accounts for PDFs that encode font size 1 and scale the text matrix.
                font = Math.max(font, p.getHeightDir());
            }
            var first = chars.get(0);
            return new PdfPage.Line(text.toString().strip(), first.getXDirAdj(),
                    first.getYDirAdj(), end, font, reconstructed.toString().strip());
        }

        private boolean endsWithSpace(StringBuilder text) {
            int cp = text.codePointBefore(text.length());
            return Character.isWhitespace(cp) || Character.isSpaceChar(cp);
        }
    }
}
