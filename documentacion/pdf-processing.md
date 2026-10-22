# Procesamiento documental de Lisa

## Ejecución sencilla

El módulo lee los PDFs desde un ZIP o una carpeta externa. No los copia al proyecto ni guarda archivos de resultados. El código y las pruebas están en los paquetes de ingestion, preprocessing y similarity/classic.

Desde la raíz del repositorio:

```powershell
# Ver un resumen de los 20 artículos
.\scripts\pdf-processing.ps1 -Mode Inspect -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip'

# Ver título, autores, abstract original, procesado, tokens y observaciones del artículo 7
.\scripts\pdf-processing.ps1 -Mode Inspect -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip' -Article 7

# Ejecutar pruebas unitarias y pruebas con los 20 documentos
.\scripts\pdf-processing.ps1 -Mode Test -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip'

# Caso común del equipo: artículos 15 y 19 con la limpieza acordada
.\scripts\pdf-processing.ps1 -Mode Compare -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip' -Left 15 -Right 19
```

Los números se refieren al orden del corpus: primero se ordena por el prefijo numérico del nombre y después por nombre. Sin prefijo, el orden es alfabético después de los archivos numerados.

`Inspect -Article N` enumera los nombres para localizar el documento y procesa únicamente ese PDF; el encabezado distingue el total de documentos del corpus y los procesados. `Compare` procesa solo los dos seleccionados (una vez si son el mismo). `Inspect` sin número y las pruebas de integración procesan los 20. Las advertencias que aparezcan al seleccionar un artículo corresponden a esa lectura, no a otros documentos del ZIP.

Para probar sin modificar datos, ejecuta los comandos anteriores tal como están, sin redirigir su salida a archivos. Verifica primero un artículo contra su PDF, después el resumen completo, luego las pruebas y finalmente una comparación. `REVIEW` indica campos presentes y revisión manual pendiente; `MISSING FIELDS` requiere examinar las observaciones. El ejecutor solo escribe archivos de compilación en TEMP y dependencias en la caché de Maven; no modifica el ZIP, los PDFs ni crea salidas dentro del proyecto.

Requisitos del ejecutor: Java/JDK con soporte para records y switch moderno (probado con JDK 21), Maven en PATH y acceso al repositorio Maven para la primera descarga. El script resuelve PDFBox 3.0.8 y JUnit Platform Console 1.11.4 mediante un descriptor en TEMP y compila únicamente este módulo. Borra su directorio temporal al terminar. No inicializa ni sustituye el Maven/Gradle del equipo; Maven conserva las dependencias descargadas en su caché normal. Las pruebas sin `-Corpus` omiten el contenedor de integración, por lo que esa ejecución no prueba los 20 PDFs.

PDFBox se usa para interpretar el formato PDF, no para implementar Jaccard. Referencias: [dependencia oficial](https://pdfbox.apache.org/3.0/getting-started.html) y [API de carga en PDFBox 3](https://pdfbox.apache.org/3.0/migration.html).

El ejecutor establece UTF-8 tanto para PowerShell como para la salida normal y de errores de Java. En JDK 21, `file.encoding` no basta para configurar `System.out` y `System.err`; se fijan también `stdout.encoding` y `stderr.encoding`, según la [documentación de Java](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html). Las advertencias de PDFBox sobre glifos sin correspondencia Unicode siguen visibles: no son un error de ejecución por sí mismas, pero deben considerarse al cotejar los campos extraídos.

Para una consola más limpia, añade `-HideFontWarnings` a cualquiera de los comandos. Esta opción filtra exclusivamente los avisos `No Unicode mapping for ...` de las fuentes de PDFBox; conserva errores y otras advertencias. Crea la configuración de registro únicamente en TEMP para esa ejecución y la elimina al terminar. Ocultar el aviso no recupera caracteres ni cambia los resultados; omite la opción cuando quieras ver los avisos durante la revisión del corpus. El código de PDFBox emite estos avisos cuando no consigue convertir un código de fuente a Unicode: [PDSimpleFont](https://github.com/apache/pdfbox/blob/3.0.8/pdfbox/src/main/java/org/apache/pdfbox/pdmodel/font/PDSimpleFont.java).

```powershell
.\scripts\pdf-processing.ps1 -Mode Test -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip' -HideFontWarnings
```

## Flujo e interfaz temporal

1. `PdfTextReader.read(source, bytes)` devuelve `PdfTextDocument`: texto y geometría por página y título interno.
2. `ArticleExtractor.extract(document)` devuelve `ExtractedArticle`: origen, título visible, autores, abstract original, observaciones y una copia con evidencia de separación de palabras. El original nunca se sustituye por esa copia.
3. `DocumentPipeline.preprocess(article, options)` devuelve `ProcessedArticle`: extracción conservada, abstract procesado, tokens y opciones aplicadas.
4. `JaccardSimilarity.compare(tokensA, tokensB, emptyUnionPolicy)` devuelve conjuntos A/B, intersección, unión y coeficiente.

Son objetos inmutables sin anotaciones de entidad ni decisiones de persistencia. `DocumentPipeline` permite inyectar otro lector y consumir una extracción corregida manualmente. Una extracción incompleta se puede inspeccionar, pero el preprocesador rechaza usarla como si estuviera completa. Tener todos los campos no equivale a estar validado.

Ejemplo para consumirlo desde otro módulo Java:

```java
var pipeline = new DocumentPipeline(
    new PdfBoxTextReader(), new ArticleExtractor(), new AbstractPreprocessor());
var article = pipeline.extract(fileName, pdfBytes);
var processed = pipeline.preprocess(article);
var result = new JaccardSimilarity().compare(
    processed.tokens(), otherProcessed.tokens(), JaccardSimilarity.EmptyUnionPolicy.REJECT);
```

El proyecto ya cuenta con la configuración Maven de David, que incluye PDFBox y las dependencias de pruebas de Spring Boot. Las 80 pruebas del módulo pasaron con esa configuración y los 20 PDFs. El script sigue disponible como ejecutor independiente y utiliza las versiones indicadas en sus requisitos. No se han añadido controladores Spring, endpoints ni entidades: sus contratos deben coordinarse con David y Steban.

## Reglas de extracción y límites

El lector conserva texto por página y agrupa glifos por posición vertical, separando segmentos cuando existe una distancia horizontal grande. Estima espacios entre glifos cuando el PDF no contiene espacios explícitos. La altura de los glifos permite distinguir tamaños aunque el PDF declare tamaño de fuente 1 con una matriz escalada. Los umbrales geométricos son heurísticas verificadas en este corpus, no garantías para PDFs arbitrarios.

El extractor busca variantes de Abstract, incluso con letras espaciadas y con texto en la misma línea. Usa la posición horizontal del encabezado para distinguir el cuerpo de otras columnas. Se detiene en keywords, introducción, tipo de artículo o un salto vertical suficientemente grande. Si no encuentra un límite explícito, deja una observación. Conserva las líneas y contenido del resumen antes de limpiar. La implementación actual identifica el abstract de la primera página: si un documento nuevo lo presenta en otra página o lo continúa, requiere ampliar las reglas y validar esa variante.

Para el título, contrasta el comienzo del metadato con líneas visibles y su tamaño; si el metadato no corresponde, busca un bloque visible destacado. Los autores se identifican bajo el título, diferenciando tamaños y descartando bloques de afiliación/editoriales. Se eliminan marcas de afiliación y correspondencia de la lista de autores, no del abstract. No se usa una lista de excepciones por archivo.

Los PDFs etiquetados pueden representar un guion visible como soft hyphen en ActualText. PDFBox normalmente lo elimina. El lector convierte ese soft hyphen a su representación visible en una copia del diccionario para conservar el corte de línea; no modifica los bytes del PDF. La unión de palabras se controla después mediante una opción independiente.

PDFs inválidos, cifrados con contraseña o sin permiso de extracción producen error de lectura. La consola conserva ese error por documento y puede continuar con los demás. Las páginas sin texto no se convierten en texto inventado: no se ha implementado OCR.

## Preprocesamiento y tokenización

La configuración predeterminada acordada por Lisa reconstruye espacios usando la evidencia del PDF, une palabras partidas por guion y salto de línea, normaliza espacios y convierte a minúsculas con Locale.ROOT, conservando palabras, números y acrónimos. El abstract original permanece separado y sin modificaciones. `PreprocessingOptions.minimalCleaning()` expresa esta política y los métodos de preprocesamiento sin argumento de opciones la aplican. Se puede proporcionar otra configuración sin cambiar el lector, el tokenizador ni Jaccard. `preserveText()` conserva el texto extraído en la salida procesada; en consola se selecciona con `-PreserveText`.

| Opción | Operación | Motivo y límite |
|---|---|---|
| NormalizeWhitespace | Unifica espacios y saltos de línea | Activada por defecto; reduce diferencias de maquetación. |
| Lowercase | Usa Locale.ROOT | Activada por defecto para comparar sin distinguir mayúsculas; conserva los acrónimos como tokens en minúsculas. |
| NormalizeUnicode | Normalización NFKC | Unifica ligaduras y formas compatibles; puede modificar notación técnica, por eso es opcional. |
| JoinLineHyphenation | Une letras separadas por guion y salto de línea antes de una letra minúscula | Activada por defecto; se ejecuta antes de eliminar los saltos. No elimina guiones internos sin salto de línea, ni une números o bloques separados por líneas vacías. |
| ReconstructWordSpacing | Conserva separaciones detectadas al leer las palabras del PDF | Activada por defecto; usa los límites de palabras de PDFBox además de la geometría, sin listas de palabras ni excepciones por artículo. |

La reconstrucción combina los separadores del original con los de la lectura por palabras: puede añadir un límite respaldado por el PDF, pero no borrar uno ya presente, incluso después de puntos, comas u otros signos. Conserva los saltos del original hasta resolver los cortes con guion y después normaliza espacios. Solo acepta candidatos con la misma secuencia de caracteres no blancos; si cambia el contenido, utiliza el original. No inserta espacios indiscriminadamente después de puntos, por lo que conserva decimales, abreviaturas y URLs. La vista `Inspect -Article N` muestra cantidad y lista de tokens entre comillas: `["how", "ai"]` representa dos tokens; una coma usada como separador visual no forma parte de ellos.

Los saltos de línea normales (LF, CRLF, CR y separadores Unicode) se convierten explícitamente en un espacio antes de compactar blancos. La prueba `first sentence.\nSecond sentence` exige `first sentence. second sentence` con normalización y minúsculas, tanto con reconstrucción como sin ella. La regla no depende de las palabras que rodean el salto ni de un artículo concreto. El original conserva su salto sin cambios.

Normalización Unicode permanece desactivada por defecto. No se eliminan stopwords, números ni acrónimos y no se aplican traducción, stemming ni lematización. El equipo puede ajustar posteriormente la configuración desacoplada. Si se combinan `-PreserveText` y opciones de activación, primero se conserva el texto y después se activan las opciones indicadas. El constructor existente de cuatro opciones conserva su comportamiento anterior, sin activar implícitamente la reconstrucción de espacios; el constructor de cinco opciones permite controlarla explícitamente.

La reconstrucción no puede separar con seguridad una cadena como `alsoprovides` si no hay evidencia de un límite de palabras en el PDF: no se inventan divisiones mediante un diccionario. La unión de guiones al final de línea es una heurística y requiere revisar los compuestos técnicos que también pueden contener guiones. El texto original permite cotejar y recuperar siempre lo extraído antes de estas operaciones. En el artículo 2 se comprueban `practitioners`, `relationships` y `also provides` en el procesado, manteniendo los cortes y espacios del original.

El tokenizador recorre explícitamente los puntos de código Unicode. Conserva letras, números y marcas combinantes; mantiene guiones y apóstrofes internos entre caracteres de palabra. La puntuación restante delimita tokens. No hace expansión de abreviaturas: e.g. genera e y g, y 3.5 genera 3 y 5. La lista retiene repeticiones; Jaccard construye después el conjunto. La política de tokenización implementada queda documentada y puede ajustarse tras el acuerdo de preprocesamiento.

## Comprobación del corpus

El ZIP proporcionado contiene 20 PDFs y 398 páginas. Se leyeron los documentos y se cotejaron visualmente sus primeras páginas renderizadas con títulos, autores y límites de los abstracts. Las imágenes y salidas auxiliares de esa revisión quedaron fuera del proyecto. Las pruebas contienen referencias de títulos, nombres y cantidad de autores, inicio/final de cada abstract y controles de exclusión de keywords e introducción. Esto protege esos campos y límites; no sustituye una transcripción de referencia completa para comparar cada carácter del abstract. Lisa confirmó que completó la comprobación manual de los 20 PDFs. Los avisos automáticos de revisión son preventivos y no registran esa aprobación manual.

| Artículo | Autores esperados | Particularidad revisada |
|---|---:|---|
| 01 | 4 | Keywords en columna lateral. |
| 02 | 5 | Guiones visibles al final de línea conservados. |
| 03 | 4 | Marcas de afiliación separadas de autores. |
| 04 | 5 | Abstract a todo el ancho y autores en dos líneas. |
| 05 | 4 | Ligaduras y título del archivo incompleto. |
| 06 | 3 | Ligaduras y autores con afiliaciones. |
| 07 | 7 | Encabezado espaciado y autores en dos líneas. |
| 08 | 3 | Keywords laterales y abstract a la derecha. |
| 09 | 2 | Sin encabezado de introducción; límite visual del bloque confirmado, con observación automática de revisión. |
| 10 | 2 | Keywords laterales. |
| 11 | 5 | Título visible usa transforms y el metadato transform. |
| 12 | 9 | Autores en dos líneas y columna lateral de afiliaciones. |
| 13 | 2 | Metadato de título incorrecto; abstract estructurado y columna editorial excluida. |
| 14 | 3 | Separadores verticales entre autores y KEYWORDS espaciado. |
| 15 | 5 | Citation y Academic Editor fuera del título y autores; abstract en línea con encabezado. |
| 16 | 2 | Cita lateral excluida del abstract. |
| 17 | 3 | Separadores centrales entre autores y keywords tras el abstract. |
| 18 | 4 | Autores en segmentos de una misma línea y ligaduras. |
| 19 | 1 | Se distingue el autor principal de cita y editor. |
| 20 | 4 | Correspondencia y afiliación en columna lateral. |

## Pruebas y trabajo pendiente

Las pruebas cubren lectura multipágina, PDF inválido/cifrado/sin texto, campos ausentes, columnas, encabezados espaciados, abstract estructurado, conservación del original, limpieza opcional, Unicode, tokenización, entradas inválidas, unión vacía y propiedades de Jaccard. Incluyen pruebas de los 20 documentos y comparación real con una referencia independiente. El ejemplo matemático y el análisis de Jaccard están en [casos_estudio/jaccard.md](casos_estudio/jaccard.md).

Lisa confirmó que John y David usarán también los artículos 15 y 19 como caso común del Requerimiento 1. El cálculo de Jaccard usa la configuración predeterminada documentada y conserva los abstracts originales. Compartir la pareja no supone que ya se hayan ejecutado los algoritmos de los compañeros ni que se hayan acordado transformaciones idénticas para todos ellos.

Pendiente de coordinación: conteo formal T(n) según la metodología y los costos definidos en clase e integración con los servicios de Spring Boot y el modelo de datos del equipo. La compilación y las pruebas con Maven, así como la validación manual de los 20 PDFs, están completadas. Las reglas actuales de preprocesamiento están definidas y desacopladas para los ajustes posteriores que acuerde el equipo. No se han implementado algoritmos de John, IA de David ni clustering/base de datos de Steban.
