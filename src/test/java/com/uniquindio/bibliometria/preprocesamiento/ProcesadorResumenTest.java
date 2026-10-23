package com.uniquindio.bibliometria.preprocesamiento;

import com.uniquindio.bibliometria.ingesta.ArticuloExtraido;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcesadorResumenTest {
    private final ProcesadorResumen procesador = new ProcesadorResumen();
    private ArticuloExtraido articulo(String texto) {
        return new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"), texto, List.of());
    }

    @Test void conservaElEspacioEntreFrasesAlUnirLineas() {
        String original = "first sentence.\nSecond sentence";
        var articulo = articulo(original);
        var opciones = new OpcionesPreprocesamiento(true, true, false, false);
        var resultado = procesador.procesar(articulo, opciones);
        assertEquals("first sentence. second sentence", resultado.resumenProcesado());
        assertEquals(original, resultado.articulo().resumenOriginal());
        assertEquals("first sentence. second sentence", procesador.procesar(articulo).resumenProcesado());
    }

    @Test void laReconstruccionConservaLosEspaciosDelOriginal() {
        for (String newline : List.of("\n", "\r\n", "\r", "\u0085", "\u2028", "\u2029")) {
            String original = "first sentence." + newline + "Second sentence";
            var articulo = new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"), original,
                    List.of(), "first sentence.Second sentence");
            var resultado = procesador.procesar(articulo);
            assertEquals("first sentence. second sentence", resultado.resumenProcesado());
            assertEquals(original, resultado.articulo().resumenOriginal());
        }
    }
    @Test void laLimpiezaConservaPalabrasNumerosSiglasYOriginal() {
        var articulo = articulo("THE\u00a0 AI 2024\n ﬁeld edu-\ncation.");
        var resultado = procesador.procesar(articulo);
        assertEquals("the ai 2024 ﬁeld education.", resultado.resumenProcesado());
        assertEquals(List.of("the", "ai", "2024", "ﬁeld", "education"), resultado.palabras());
        assertEquals("THE\u00a0 AI 2024\n ﬁeld edu-\ncation.", resultado.articulo().resumenOriginal());
        assertFalse(resultado.opciones().normalizarUnicode());
        assertTrue(resultado.opciones().unirGuiones());
        assertTrue(resultado.opciones().reconstruirEspacios());
    }
    @Test void elModoConservarNoModificaElTexto() {
        var articulo = articulo("AI\n  learning 2024.");
        var procesado = procesador.procesar(articulo, OpcionesPreprocesamiento.conservarTexto());
        assertEquals(articulo.resumenOriginal(), procesado.resumenProcesado());
        assertEquals(List.of("AI", "learning", "2024"), procesado.palabras());
    }
    @Test void laNormalizacionConservaNumerosYSiglas() {
        var articulo = articulo("AI\u00a0  2024\nlearning");
        var procesado = procesador.procesar(articulo, new OpcionesPreprocesamiento(true, true, false, false));
        assertEquals("ai 2024 learning", procesado.resumenProcesado());
        assertEquals("AI\u00a0  2024\nlearning", procesado.articulo().resumenOriginal());
    }
    @Test void normalizaLigadurasSoloCuandoSeSolicita() {
        assertEquals("ﬁeld", procesador.procesar(articulo("ﬁeld"), OpcionesPreprocesamiento.conservarTexto()).resumenProcesado());
        assertEquals("field", procesador.procesar(articulo("ﬁeld"), new OpcionesPreprocesamiento(false, false, true, false)).resumenProcesado());
    }
    @Test void laUnionDeGuionesEsConfigurable() {
        var articulo = articulo("edu-\ncation AI-driven");
        assertEquals("edu-\ncation AI-driven", procesador.procesar(articulo, OpcionesPreprocesamiento.conservarTexto()).resumenProcesado());
        assertEquals("education AI-driven", procesador.procesar(articulo, new OpcionesPreprocesamiento(false, false, false, true)).resumenProcesado());
    }

    @Test void uneGuionesAntesDeNormalizarEspacios() {
        var original = "practi-\r\n tioners  and re-\nlationships\u00a0\tAI-driven 2024";
        var articulo = articulo(original);
        var resultado = procesador.procesar(articulo);
        assertEquals("practitioners and relationships ai-driven 2024", resultado.resumenProcesado());
        assertEquals(original, resultado.articulo().resumenOriginal());
        assertEquals(List.of("practitioners", "and", "relationships", "ai-driven", "2024"), resultado.palabras());
    }

    @Test void noInventaSeparacionesSinEvidenciaDelPdf() {
        var articulo = articulo("alsoprovides microscope AI-driven");
        assertEquals("alsoprovides microscope ai-driven", procesador.procesar(articulo).resumenProcesado());
    }

    @Test void laReconstruccionSoloModificaElTextoProcesado() {
        var original = "The study alsoprovides\nnew evidence.";
        var articulo = new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "The study also provides\nnew evidence.");
        var resultado = procesador.procesar(articulo);
        assertEquals("the study also provides new evidence.", resultado.resumenProcesado());
        assertEquals(original, articulo.resumenOriginal());
        assertEquals(original, procesador.procesar(articulo, OpcionesPreprocesamiento.conservarTexto()).resumenProcesado());
    }

    @Test void conservaEspaciosDespuesDeLaPuntuacion() {
        String original = "Analysis approaches. The results! Another finding? More; still: useful.";
        var articulo = new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "Analysis approaches.The results!Another finding?More;still:useful.");
        var resultado = procesador.procesar(articulo);
        assertEquals("analysis approaches. the results! another finding? more; still: useful.",
                resultado.resumenProcesado());
        assertEquals(original, resultado.articulo().resumenOriginal());
        assertEquals(original, procesador.procesar(articulo, OpcionesPreprocesamiento.conservarTexto()).resumenProcesado());
    }

    @Test void conservaDecimalesAbreviaturasYEnlaces() {
        String original = "Modelslearn. The value is 3.14, e.g. https://example.org/v1.2.";
        var articulo = new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "Models learn.The value is 3.14, e.g. https://example.org/v1.2.");
        assertEquals("models learn. the value is 3.14, e.g. https://example.org/v1.2.",
                procesador.procesar(articulo).resumenProcesado());
        assertEquals(original, articulo.resumenOriginal());
    }

    @Test void descartaReconstruccionesQueCambianLasLetras() {
        var articulo = new ArticuloExtraido("sample.pdf", "Sample", List.of("Author"),
                "Correct results.", List.of(), "Invented results.");
        assertEquals("correct results.", procesador.procesar(articulo).resumenProcesado());
    }

    @Test void separaPalabrasConEspaciosOComas() {
        for (String input : List.of("how ai", "how,ai", "how, ai", "how\u00a0ai")) {
            assertEquals(List.of("how", "ai"), procesador.separarPalabras(input));
        }
        assertEquals(List.of("how", "ai"), procesador.procesar(articulo("HOW AI")).palabras());
    }

    @Test void conservaGuionesInternosYLimitesDeParrafo() {
        var resultado = procesador.procesar(articulo("AI-driven 2024-2025 pre-\nAI 2024-\n2025 end-\n\nstart"));
        assertEquals("ai-driven 2024-2025 pre- ai 2024- 2025 end- start", resultado.resumenProcesado());
    }
    @Test void separaPalabrasUnicodeConNumerosYConectores() {
        // Un apostrofe sin una letra a continuacion se considera puntuacion.
        assertEquals(List.of("café", "cafe\u0301", "2024", "AI-driven", "don't", "students", "learning"),
                procesador.separarPalabras("café cafe\u0301, 2024; AI-driven don't students’ learning."));
    }
    @Test void elTextoVacioNoTienePalabras() {
        assertEquals(List.of(), procesador.separarPalabras(""));
        assertEquals(List.of(), procesador.separarPalabras("... \n"));
    }
}
