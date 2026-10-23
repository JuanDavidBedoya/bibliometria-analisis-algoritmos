package com.uniquindio.bibliometria.ingesta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.uniquindio.bibliometria.preprocesamiento.OpcionesPreprocesamiento;
import com.uniquindio.bibliometria.preprocesamiento.ProcesadorResumen;

/** Usa -Dbibliometria.corpus=<ZIP/carpeta> para indicar los documentos de prueba. */
class ExtraccionCorpusTest {
    private static List<ArticuloExtraido> articulos;
    private static final int[] CANTIDADES_AUTORES = {4,5,4,5,4,3,7,3,2,2,5,9,2,3,5,2,3,4,1,4};
    private static final List<List<String>> AUTORES = List.of(
            List.of("Matthias Carl Laupichler", "Alexandra Aster", "Jana Schirch", "Tobias Raupach"),
            List.of("Thomas K.F. Chiu", "Qi Xia", "Xinyan Zhou", "Ching Sing Chai", "Miaoting Cheng"),
            List.of("Arne Bewersdorff", "Xiaoming Zhai", "Jessica Roberts", "Claudia Nerdel"),
            List.of("Srecko Joksimovic", "Dirk Ifenthaler", "Rebecca Marrone", "Maarten De Laat", "George Siemens"),
            List.of("Xieling Chen", "Haoran Xie", "Di Zou", "Gwo-Jen Hwang"),
            List.of("Xieling Chen", "Haoran Xie", "Gwo-Jen Hwang"),
            List.of("Riordan Alfredo", "Vanessa Echeverria", "Yueqiao Jin", "Lixiang Yan", "Zachari Swiecki", "Dragan Gašević", "Roberto Martinez-Maldonado"),
            List.of("Seng Chee Tan", "Alwyn Vwen Yen Lee", "Min Lee"),
            List.of("Ke Zhang", "Ayse Begum Aslan"), List.of("Chih-Pu Dai", "Fengfeng Ke"),
            List.of("Qi Xia", "Xiaojing Weng", "Fan Ouyang", "Tzung Jin Lin", "Thomas K.F. Chiu"),
            List.of("Melissa Bond", "Hassan Khosravi", "Maarten De Laat", "Nina Bergdahl", "Violeta Negrea", "Emily Oxley", "Phuong Pham", "Sin Wang Chong", "George Siemens"),
            List.of("Jyoti Mudkanna Gavhane", "Reena Pagare"),
            List.of("Tamar Mikeladze", "Paulien C. Meijer", "Roald P. Verhoeff"),
            List.of("Bayode Ogunleye", "Kudirat Ibilola Zakariyyah", "Oluwaseun Ajao", "Olakunle Olayinka", "Hemlata Sharma"),
            List.of("Che Yee Lye", "Lyndon Lim"), List.of("Fan Ouyang", "Tuan Anh Dinh", "Weiqi Xu"),
            List.of("Zied Bahroun", "Chiraz Anane", "Vian Ahmed", "Andrew Zacca"), List.of("Chung Kwan Lo"),
            List.of("Olaf Zawacki-Richter", "Victoria I. Marín", "Melissa Bond", "Franziska Gouverneur")
    );
    private static final String[] TITULOS = {
            "Artificial intelligence literacy in higher and adult education: A scoping literature review",
            "Systematic literature review on opportunities, challenges, and future research recommendations of artificial intelligence in education",
            "Myths, mis- and preconceptions of artificial intelligence: A review of the literature",
            "Opportunities of artificial intelligence for supporting complex problem-solving: Findings from a scoping review",
            "Application and theory gaps during the rise of Artificial Intelligence in Education",
            "A multi-perspective study on Artificial Intelligence in Education: grants, conferences, journals, software tools, institutions, and researchers",
            "Human-centred learning analytics and AI in education: A systematic literature review",
            "A systematic review of artificial intelligence techniques for collaborative learning over the past two decades",
            "AI technologies for education: Recent research & future directions",
            "Educational applications of artificial intelligence in simulation-based learning: A systematic mapping review",
            "A scoping review on how generative artificial intelligence transforms assessment in higher education",
            "A meta systematic review of artificial intelligence in higher education: a call for increased ethics, collaboration, and rigour",
            "Artificial intelligence for education and its emphasis on assessment and adversity quotient: a review",
            "A comprehensive exploration of artificial intelligence competence frameworks for educators: A critical review",
            "A Systematic Review of Generative AI for Teaching and Learning Practice",
            "Generative Artificial Intelligence in Tertiary Education: Assessment Redesign Principles and Considerations",
            "A Systematic Review of AI‑Driven Educational Assessment in STEM Education",
            "Transforming Education: A Comprehensive Review of Generative Artificial Intelligence in Educational Settings through Bibliometric and Content Analysis",
            "What Is the Impact of ChatGPT on Education? A Rapid Review of the Literature",
            "Systematic review of research on artificial intelligence applications in higher education – where are the educators?"
    };
    // Inicios y finales revisados en los PDF, independientes del codigo de extraccion.
    private static final String[] INICIOS = {
            "Since artificial intelligence", "Applications of artificial intelligence",
            "Artificial Intelligence (AI) is prevalent", "The research objective of this paper",
            "Considering the increasing importance", "With the rapid development",
            "The rapid expansion of Learning Analytics", "This systematic review focuses",
            "From unique educational perspectives", "The field of education has experienced",
            "Generative artificial intelligence provides", "Although the field of Artificial Intelligence",
            "Purpose", "Recent literature underscores", "The use of generative artificial intelligence",
            "The emergence of generative artificial intelligence", "Artificial intelligence (AI), as an emerging technology",
            "In the ever-evolving era", "An artificial intelligence-based chatbot", "According to various international reports"
    };
    private static final String[] FINALES = {
            "results.", "directions of AIEd research.", "education.",
            "Implications for future research and practice are being discussed.", "nologies with educational theories.",
            "Computers & Education: Artificial Intelligence.", "future human-centred LA/AIED systems.",
            "suggest future areas of study.", "opment efforts.", "simulation-based learning.",
            "provide more inter-disciplinary programs and teaching.", "and secondary research.",
            "educational assessment and the evaluation of AQ.", "teaching practices.",
            "guidelines, frameworks, and policies for GenAI usage.", "in assessments.",
            "educational assessment in STEM education.", "the intersection of GAI and education.",
            "ChatGPT on the educational environment.", "in higher education."
    };

    public static List<ArticuloExtraido> getArticulos() {
        return articulos;
    }

    @TestFactory Stream<DynamicTest> compruebaLosCamposDeLosVeinteDocumentos() {
        return IntStream.range(0, 20).mapToObj(i -> DynamicTest.dynamicTest("Article " + (i + 1), () -> {
            var articulo = articulos.get(i);
            assertTrue(articulo.tieneCamposCompletos(), articulo.observaciones().toString());
            assertEquals(CANTIDADES_AUTORES[i], articulo.autores().size(), articulo.autores().toString());
            assertEquals(normalizado(TITULOS[i]), normalizado(articulo.titulo()));
            assertEquals(AUTORES.get(i).stream().map(ExtraccionCorpusTest::normalizado).toList(),
                    articulo.autores().stream().map(ExtraccionCorpusTest::normalizado).toList());
            String texto = normalizado(articulo.resumenOriginal());
            assertTrue(texto.startsWith(INICIOS[i]), texto.substring(0, Math.min(150, texto.length())));
            assertTrue(texto.endsWith(FINALES[i]), texto.substring(Math.max(0, texto.length() - 180)));
            assertFalse(texto.matches("(?is).*\\bkeywords\\s*:.*"));
            assertFalse(texto.matches("(?is).*\\b1\\. Introduction\\b.*"));
            assertTrue(articulo.observaciones().stream().anyMatch(n -> n.contains("revision manual")));
        }));
    }

    @Test void compruebaTitulosYAutoresEnDistintosFormatos() {
        assertEquals("Artificial intelligence for education and its emphasis on assessment and adversity quotient: a review",
                normalizado(articulos.get(12).titulo()));
        assertEquals(List.of("Tamar Mikeladze", "Paulien C. Meijer", "Roald P. Verhoeff"), articulos.get(13).autores());
        assertEquals("Chung Kwan Lo", articulos.get(18).autores().get(0));
    }

    @Test void conservaLosGuionesEnElResumenOriginal() {
        assertTrue(articulos.get(1).resumenOriginal().contains("practi-\ntioners"));
        assertTrue(articulos.get(8).resumenOriginal().contains("devel-\nopment"));
        var procesado = new ProcesadorResumen().procesar(articulos.get(1),
                new OpcionesPreprocesamiento(true, false, false, true));
        assertTrue(procesado.palabras().contains("practitioners"));
        assertTrue(articulos.get(1).resumenOriginal().contains("practi-\ntioners"));
    }

    @Test void conservaEspaciosPequenosEntrePalabras() {
        assertTrue(normalizado(articulos.get(0).resumenOriginal()).contains("recommendations for research and practice"));
        assertFalse(articulos.get(0).resumenOriginal().contains("researchand"));
        String textoResumen = normalizado(articulos.get(12).resumenOriginal());
        assertTrue(textoResumen.contains("Education is a vital tool to"));
        assertTrue(textoResumen.contains("skills for success"));
    }

    @Test void reconstruyeElArticuloDosSinModificarElOriginal() {
        var articulo = articulos.get(1);
        String original = articulo.resumenOriginal();
        var resultado = new ProcesadorResumen().procesar(articulo);
        assertTrue(original.contains("practi-\ntioners"));
        assertTrue(original.contains("re-\nlationships"));
        assertEquals(original, resultado.articulo().resumenOriginal());
        assertTrue(resultado.palabras().contains("practitioners"));
        assertTrue(resultado.palabras().contains("relationships"));
        assertTrue(resultado.resumenProcesado().contains("also provides"));
        assertFalse(resultado.resumenProcesado().contains("alsoprovides"));
        assertTrue(resultado.resumenProcesado().contains("analysis approaches. the results"));
        int finalFrase = resultado.resumenProcesado().indexOf("analysis approaches.")
                + "analysis approaches.".length();
        assertEquals(' ', resultado.resumenProcesado().charAt(finalFrase),
                "A normal sentence boundary must contain a literal U+0020 space");
        assertFalse(resultado.resumenProcesado().contains("approaches.the"));
        int how = resultado.palabras().indexOf("how");
        assertTrue(how >= 0 && how + 1 < resultado.palabras().size());
        assertEquals("ai", resultado.palabras().get(how + 1));
        assertFalse(resultado.palabras().contains("how,ai"));
        assertFalse(resultado.palabras().contains("howai"));
        assertTrue(ConsolaPdf.mostrarPalabras(resultado.palabras()).contains("\"how\", \"ai\""));
        assertFalse(resultado.resumenProcesado().matches("(?s).*[\\s\\p{Z}]{2,}.*"));
        assertFalse(resultado.resumenProcesado().contains("\n"));
        assertEquals(5, articulo.autores().size());
    }

    @Test void laConsolaConservaLosEspaciosYElOriginal() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var salida = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            ConsolaPdf.ejecutar(new String[]{"inspeccionar", System.getProperty("bibliometria.corpus"), "2"}, salida);
        }
        String impreso = bytes.toString(StandardCharsets.UTF_8);
        String marcador = "Resumen procesado:\n";
        int inicio = impreso.indexOf(marcador);
        assertTrue(inicio >= 0);
        String procesado = impreso.substring(inicio + marcador.length()).lines().findFirst().orElseThrow();
        assertTrue(procesado.contains("analysis approaches. the results"), procesado);
        assertFalse(procesado.contains("approaches.the results"));
        int despuesDelPunto = procesado.indexOf("approaches.") + "approaches.".length();
        assertEquals(0x20, procesado.codePointAt(despuesDelPunto),
                "The actual printed text must include an ASCII space after the period");
        assertTrue(impreso.contains("Resumen original:\n" + articulos.get(1).resumenOriginal()
                + System.lineSeparator()), "Console must print the unchanged original abstract");
    }

    @Test void exportaLosVeinteArticulosYPermiteLeerlosDesdeJson(
            @org.junit.jupiter.api.io.TempDir Path carpeta) throws Exception {
        Path destino = carpeta.resolve("articulos.json");
        var salida = new ByteArrayOutputStream();
        try (var consola = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            ConsolaPdf.ejecutar(new String[]{"exportar", System.getProperty("bibliometria.corpus"),
                    destino.toString()}, consola);
        }
        var guardados = new ArchivoArticulosJson().leer(destino);
        assertEquals(20, guardados.size());
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("Articulos guardados: 20"));
        for (int i = 0; i < articulos.size(); i++) {
            assertEquals(articulos.get(i).titulo(), guardados.get(i).titulo());
            assertEquals(articulos.get(i).autores(), guardados.get(i).autores());
            assertEquals(articulos.get(i).resumenOriginal(), guardados.get(i).resumenOriginal());
            var procesado = new ProcesadorResumen().procesar(articulos.get(i));
            assertEquals(procesado.resumenProcesado(), guardados.get(i).resumenProcesado());
            assertEquals(procesado.palabras(), guardados.get(i).palabras());
        }
    }

    private static String normalizado(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFKC).replaceAll("[\\s\\p{Z}]+", " ").strip();
    }
}
