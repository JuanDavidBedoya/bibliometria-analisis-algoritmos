package com.uniquindio.bibliometria.ingesta;

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

class LectorPdfBoxTest {
    @Test void leePaginasYPosicionesSinModificarElPdf() throws Exception {
        byte[] pdf;
        try (var documento = new PDDocument()) {
            documento.getDocumentInformation().setTitle("Sample PDF");
            for (String texto : new String[]{"First page", "Second page"}) {
                var pagina = new PDPage();
                documento.addPage(pagina);
                try (var content = new PDPageContentStream(documento, pagina)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(texto);
                    content.endText();
                }
            }
            var bytes = new ByteArrayOutputStream();
            documento.save(bytes);
            pdf = bytes.toByteArray();
        }
        byte[] original = pdf.clone();
        var resultado = new LectorPdfBox().leer("sample.pdf", pdf);
        assertEquals("Sample PDF", resultado.tituloMetadatos());
        assertEquals(2, resultado.paginas().size());
        assertTrue(resultado.paginas().get(0).texto().contains("First page"));
        assertTrue(resultado.paginas().get(1).texto().contains("Second page"));
        assertEquals(50, resultado.paginas().get(0).lineas().get(0).x(), 1);
        assertEquals("First page", resultado.paginas().get(0).lineas().get(0).textoReconstruido());
        assertEquals("Second page", resultado.paginas().get(1).lineas().get(0).textoReconstruido());
        assertArrayEquals(original, pdf);
    }
    @Test void rechazaUnPdfInvalido() {
        assertThrows(IOException.class, () -> new LectorPdfBox().leer("broken.pdf", new byte[]{1, 2, 3}));
    }
    @Test void rechazaUnPdfQueRequiereClave() throws Exception {
        byte[] pdf;
        try (var documento = new PDDocument()) {
            documento.addPage(new PDPage());
            documento.protect(new StandardProtectionPolicy("owner", "reader", new AccessPermission()));
            var bytes = new ByteArrayOutputStream();
            documento.save(bytes);
            pdf = bytes.toByteArray();
        }
        assertThrows(IOException.class, () -> new LectorPdfBox().leer("protected.pdf", pdf));
    }
    @Test void noInventaTextoEnUnaPaginaEscaneada() throws Exception {
        byte[] pdf;
        try (var documento = new PDDocument()) {
            documento.addPage(new PDPage());
            var bytes = new ByteArrayOutputStream();
            documento.save(bytes);
            pdf = bytes.toByteArray();
        }
        var resultado = new LectorPdfBox().leer("blank.pdf", pdf);
        assertTrue(resultado.paginas().get(0).texto().isBlank());
        assertFalse(new ExtractorArticulos().extraer(resultado).tieneCamposCompletos());
    }
}
