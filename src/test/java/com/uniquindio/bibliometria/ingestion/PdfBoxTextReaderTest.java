package com.uniquindio.bibliometria.ingestion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class PdfBoxTextReaderTest {
    @Test void readsMultiplePagesAndCoordinatesWithoutModifyingBytes() throws Exception {
        byte[] pdf;
        try (var document = new PDDocument()) {
            document.getDocumentInformation().setTitle("Sample PDF");
            for (String text : new String[]{"First page", "Second page"}) {
                var page = new PDPage();
                document.addPage(page);
                try (var content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            var bytes = new ByteArrayOutputStream();
            document.save(bytes);
            pdf = bytes.toByteArray();
        }
        byte[] original = pdf.clone();
        var result = new PdfBoxTextReader().read("sample.pdf", pdf);
        assertEquals("Sample PDF", result.metadataTitle());
        assertEquals(2, result.pages().size());
        assertTrue(result.pages().get(0).text().contains("First page"));
        assertTrue(result.pages().get(1).text().contains("Second page"));
        assertEquals(50, result.pages().get(0).lines().get(0).x(), 1);
        assertEquals("First page", result.pages().get(0).lines().get(0).reconstructedText());
        assertEquals("Second page", result.pages().get(1).lines().get(0).reconstructedText());
        assertArrayEquals(original, pdf);
    }
    @Test void invalidPdfFailsExplicitly() {
        assertThrows(IOException.class, () -> new PdfBoxTextReader().read("broken.pdf", new byte[]{1, 2, 3}));
    }
    @Test void encryptedPdfRequiringPasswordFailsExplicitly() throws Exception {
        byte[] pdf;
        try (var document = new PDDocument()) {
            document.addPage(new PDPage());
            document.protect(new StandardProtectionPolicy("owner", "reader", new AccessPermission()));
            var bytes = new ByteArrayOutputStream();
            document.save(bytes);
            pdf = bytes.toByteArray();
        }
        assertThrows(IOException.class, () -> new PdfBoxTextReader().read("protected.pdf", pdf));
    }
    @Test void imageOnlyPageReportsNoTextRatherThanInventingAnAbstract() throws Exception {
        byte[] pdf;
        try (var document = new PDDocument()) {
            document.addPage(new PDPage());
            var bytes = new ByteArrayOutputStream();
            document.save(bytes);
            pdf = bytes.toByteArray();
        }
        var result = new PdfBoxTextReader().read("blank.pdf", pdf);
        assertTrue(result.pages().get(0).text().isBlank());
        assertFalse(new ArticleExtractor().extract(result).hasRequiredFields());
    }
}
