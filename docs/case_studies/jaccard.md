# Jaccard: implementación y caso de estudio común

## Definición e implementación

J(A,B) = |A ∩ B| / |A ∪ B|. A y B contienen los tokens distintos de los abstracts procesados. Las repeticiones no aportan peso; el orden no modifica el coeficiente. `JaccardSimilarity` construye los conjuntos mediante bucles y estructuras básicas (`LinkedHashSet`), cuenta la intersección y construye la unión explícitamente. No utiliza una función de similitud externa.

La política para una unión vacía es obligatoria: `REJECT` rechaza el caso indefinido 0/0; `ZERO` y `ONE` permiten convenciones explícitas. La consola usa `REJECT`. Si solo un conjunto está vacío, la unión no está vacía y el resultado es 0. Entradas nulas y tokens nulos o blancos se rechazan.

## Pseudocódigo

```text
A = conjunto vacío
B = conjunto vacío
para cada token en entradaA: añadir token a A
para cada token en entradaB: añadir token a B
intersección = conjunto vacío
unión = conjunto vacío
para cada token en A:
    añadir token a unión
    si B contiene token: añadir token a intersección
para cada token en B: añadir token a unión
si unión está vacía: aplicar la política explícita
si no: devolver tamaño(intersección) / tamaño(unión)
```

Después del primer bucle sobre A, la unión contiene A y la intersección contiene exactamente los elementos de A presentes en B. El siguiente bucle incorpora los elementos de B que faltan en la unión. Por tanto, las cardinalidades obtenidas corresponden a A ∩ B y A ∪ B. La división se hace en punto flotante para evitar división entera. Como A ∩ B es subconjunto de A ∪ B, el coeficiente queda entre 0 y 1 para unión no vacía.

## Análisis algorítmico

Sean n y m los números de tokens de las entradas, a = |A| y b = |B|. Hay n + m + a + b iteraciones principales. La primera fase realiza n + m inserciones; el recorrido de A realiza a inserciones en la unión, a consultas de pertenencia y c = |A ∩ B| inserciones en la intersección; el recorrido de B realiza b inserciones en la unión. La creación de listas inmutables recorre a + b + c + u elementos, con u = |A ∪ B|.

Bajo el modelo de coste esperado constante para operaciones de tablas hash y tokens de longitud acotada, el tiempo es O(n + m) y el espacio adicional O(a + b), incluyendo conjuntos y resultado. Si L es el total de caracteres de los tokens, el cálculo y comparación de cadenas añade coste dependiente de L. Colisiones y comparaciones de cadenas impiden afirmar que cada operación hash tiene coste constante en todos los casos. Un límite conservador con búsquedas lineales por colisiones es cuadrático en el número de tokens, multiplicado por el coste de comparación de las cadenas; la implementación concreta de Java puede usar árboles en determinadas cubetas. Este análisis es general: falta conocer el criterio/formato exacto utilizado en la asignatura para adaptar el conteo solicitado.

## Caso común confirmado: artículos 15 y 19

Lisa confirmó que John y David usarán también esta pareja para el Requerimiento 1. Esta sección documenta exclusivamente la ejecución de Jaccard; no atribuye resultados ni implementaciones a los demás integrantes.

- A: artículo 15, *A Systematic Review of Generative AI for Teaching and Learning Practice*, Bayode Ogunleye y colaboradores.
- B: artículo 19, *What Is the Impact of ChatGPT on Education? A Rapid Review of the Literature*, Chung Kwan Lo.

Ambos abstracts pertenecen al corpus estático entregado y tratan IA en educación con enfoques distintos: IA generativa para enseñanza/aprendizaje y ChatGPT. El algoritmo consume los abstracts, sin incorporar títulos, autores, keywords o introducciones a los conjuntos.

## Configuración reproducible

La ejecución usa las reglas actuales de Lisa:

- Reconstrucción de espacios apoyada en límites de palabras del PDF y en separadores del original.
- Unión de palabras partidas por guion y salto de línea antes de normalizar espacios.
- Normalización de espacios y conversión a minúsculas con Locale.ROOT.
- Tokenización explícita por letras/números Unicode y marcas combinantes, conservando guiones y apóstrofes internos.
- Sin eliminación de stopwords, números o acrónimos; sin traducción, stemming ni lematización.
- Normalización Unicode NFKC desactivada.
- Unión vacía: política REJECT.

Los abstracts originales permanecen separados e intactos en `ExtractedArticle`; las operaciones se aplican a la representación procesada. La selección de la misma pareja no impone que los algoritmos de IA deban consumir los mismos tokens: el equipo debe identificar la versión del abstract utilizada en cada comparación.

Desde la raíz del repositorio:

```powershell
.\scripts\pdf-processing.ps1 -Mode Compare -Corpus 'C:\Users\HP\Downloads\Articulos Proyecto Algoritmos.zip' -Left 15 -Right 19 -HideFontWarnings
```

El comando lee únicamente esos dos PDFs, imprime los abstracts procesados y los conjuntos y no modifica el ZIP ni guarda salidas en el proyecto. `-HideFontWarnings` solo filtra avisos de fuentes; no cambia la extracción ni el cálculo.

### Abstract procesado A

```text
the use of generative artificial intelligence (genai) in academia is a subjective and hotly debated topic. currently, there are no agreed guidelines towards the usage of genai systems in higher education (he) and, thus, it is still unclear how to make effective use of the technology for teaching and learning practice. this paper provides an overview of the current state of research on genai for teaching and learning in he. to this end, this study conducted a systematic review of relevant studies indexed by scopus, using the preferred reporting items for systematic reviews and meta-analyses (prisma) guidelines. the search criteria revealed a total of 625 research papers, of which 355 met the final inclusion criteria. the findings from the review showed the current state and the future trends in documents, citations, document sources/authors, keywords, and co-authorship. the research gaps identified suggest that while some authors have looked at understanding the detection of ai-generated text, it may be beneficial to understand how genai can be incorporated into supporting the educational curriculum for assessments, teaching, and learning delivery. furthermore, there is a need for additional interdisciplinary, multidimensional studies in he through collaboration. this will strengthen the awareness and understanding of students, tutors, and other stakeholders, which will be instrumental in formulating guidelines, frameworks, and policies for genai usage.
```

### Abstract procesado B

```text
an artiﬁcial intelligence-based chatbot, chatgpt, was launched in november 2022 and is capable of generating cohesive and informative human-like responses to user input. this rapid review of the literature aims to enrich our understanding of chatgpt’s capabilities across subject domains, how it can be used in education, and potential issues raised by researchers during the ﬁrst three months of its release (i.e., december 2022 to february 2023). a search of the relevant databases and google scholar yielded 50 articles for content analysis (i.e., open coding, axial coding, and selective coding). the ﬁndings of this review suggest that chatgpt’s performance varied across subject domains, ranging from outstanding (e.g., economics) and satisfactory (e.g., programming) to unsatisfactory (e.g., mathematics). although chatgpt has the potential to serve as an assistant for instructors (e.g., to generate course materials and provide suggestions) and a virtual tutor for students (e.g., to answer questions and facilitate collaboration), there were challenges associated with its use (e.g., generating incorrect or fake information and bypassing plagiarism detectors). immediate action should be taken to update the assessment methods and institutional policies in schools and universities. instructor training and student education are also essential to respond to the impact of chatgpt on the educational environment.
```

## Conjuntos ejecutados

La lista de tokens puede contener repeticiones; los conjuntos siguientes solo incluyen valores distintos, manteniendo el orden de primera aparición para facilitar la revisión.

### Conjunto A

```text
[the, use, of, generative, artificial, intelligence, genai, in, academia, is, a, subjective, and, hotly, debated, topic, currently, there, are, no, agreed, guidelines, towards, usage, systems, higher, education, he, thus, it, still, unclear, how, to, make, effective, technology, for, teaching, learning, practice, this, paper, provides, an, overview, current, state, research, on, end, study, conducted, systematic, review, relevant, studies, indexed, by, scopus, using, preferred, reporting, items, reviews, meta-analyses, prisma, search, criteria, revealed, total, 625, papers, which, 355, met, final, inclusion, findings, from, showed, future, trends, documents, citations, document, sources, authors, keywords, co-authorship, gaps, identified, suggest, that, while, some, have, looked, at, understanding, detection, ai-generated, text, may, be, beneficial, understand, can, incorporated, into, supporting, educational, curriculum, assessments, delivery, furthermore, need, additional, interdisciplinary, multidimensional, through, collaboration, will, strengthen, awareness, students, tutors, other, stakeholders, instrumental, formulating, frameworks, policies]
```

### Conjunto B

```text
[an, artiﬁcial, intelligence-based, chatbot, chatgpt, was, launched, in, november, 2022, and, is, capable, of, generating, cohesive, informative, human-like, responses, to, user, input, this, rapid, review, the, literature, aims, enrich, our, understanding, chatgpt’s, capabilities, across, subject, domains, how, it, can, be, used, education, potential, issues, raised, by, researchers, during, ﬁrst, three, months, its, release, i, e, december, february, 2023, a, search, relevant, databases, google, scholar, yielded, 50, articles, for, content, analysis, open, coding, axial, selective, ﬁndings, suggest, that, performance, varied, ranging, from, outstanding, g, economics, satisfactory, programming, unsatisfactory, mathematics, although, has, serve, as, assistant, instructors, generate, course, materials, provide, suggestions, virtual, tutor, students, answer, questions, facilitate, collaboration, there, were, challenges, associated, with, use, incorrect, or, fake, information, bypassing, plagiarism, detectors, immediate, action, should, taken, update, assessment, methods, institutional, policies, schools, universities, instructor, training, student, are, also, essential, respond, impact, on, educational, environment]
```

### Intersección

```text
[the, use, of, in, is, a, and, there, are, education, it, how, to, for, this, an, on, review, relevant, by, search, from, suggest, that, understanding, be, can, educational, collaboration, students, policies]
```

### Unión

```text
[the, use, of, generative, artificial, intelligence, genai, in, academia, is, a, subjective, and, hotly, debated, topic, currently, there, are, no, agreed, guidelines, towards, usage, systems, higher, education, he, thus, it, still, unclear, how, to, make, effective, technology, for, teaching, learning, practice, this, paper, provides, an, overview, current, state, research, on, end, study, conducted, systematic, review, relevant, studies, indexed, by, scopus, using, preferred, reporting, items, reviews, meta-analyses, prisma, search, criteria, revealed, total, 625, papers, which, 355, met, final, inclusion, findings, from, showed, future, trends, documents, citations, document, sources, authors, keywords, co-authorship, gaps, identified, suggest, that, while, some, have, looked, at, understanding, detection, ai-generated, text, may, be, beneficial, understand, can, incorporated, into, supporting, educational, curriculum, assessments, delivery, furthermore, need, additional, interdisciplinary, multidimensional, through, collaboration, will, strengthen, awareness, students, tutors, other, stakeholders, instrumental, formulating, frameworks, policies, artiﬁcial, intelligence-based, chatbot, chatgpt, was, launched, november, 2022, capable, generating, cohesive, informative, human-like, responses, user, input, rapid, literature, aims, enrich, our, chatgpt’s, capabilities, across, subject, domains, used, potential, issues, raised, researchers, during, ﬁrst, three, months, its, release, i, e, december, february, 2023, databases, google, scholar, yielded, 50, articles, content, analysis, open, coding, axial, selective, ﬁndings, performance, varied, ranging, outstanding, g, economics, satisfactory, programming, unsatisfactory, mathematics, although, has, serve, as, assistant, instructors, generate, course, materials, provide, suggestions, virtual, tutor, answer, questions, facilitate, were, challenges, associated, with, incorrect, or, fake, information, bypassing, plagiarism, detectors, immediate, action, should, taken, update, assessment, methods, institutional, schools, universities, instructor, training, student, also, essential, respond, impact, environment]
```

## Cálculo matemático e interpretación

| Cantidad | Valor |
|---|---:|
| Cardinalidad A | 133 |
| Cardinalidad B | 141 |
| Cardinalidad de la intersección | 31 |
| Cardinalidad de la unión | 243 |

Comprobación de la unión: |A ∪ B| = |A| + |B| − |A ∩ B| = 133 + 141 − 31 = 243.

J(A,B) = |A ∩ B| / |A ∪ B| = 31 / 243 = **0.127572016461**, aproximadamente **12.76 %**.

Los 31 tokens compartidos representan esa proporción de los 243 tokens distintos presentes en cualquiera de los abstracts. El valor describe coincidencia léxica de conjuntos; no representa un porcentaje de similitud semántica ni de plagio. No se asigna un umbral de alta o baja similitud porque el equipo no lo ha definido.

La ausencia de NFKC tiene un efecto observable: `artificial` y `artiﬁcial`, así como `findings` y `ﬁndings`, son tokens distintos aunque las ligaduras se vean similares. Se documenta esta limitación de la configuración actual; no se normalizan silenciosamente esos caracteres. El resultado depende tanto de la tokenización y limpieza como del algoritmo.

## Verificación y pendientes académicos

La prueba de integración del caso usa las mismas opciones predeterminadas que el comando. Contrasta intersección y unión con una referencia independiente basada en listas y búsquedas lineales, y comprueba las cuatro cardinalidades anteriores. Las pruebas unitarias cubren igualdad, disjunción, coincidencia parcial, repetidos, simetría, límites, entradas vacías y entradas inválidas.

La pareja común y el cálculo de Jaccard quedan definidos en esta documentación. Falta adaptar el conteo/análisis general anterior al formato exacto empleado en la asignatura cuando se disponga de ese criterio, e integrar el módulo con los contratos del backend y del modelo de datos del equipo. La confirmación de la pareja no equivale a afirmar que ya se ejecutaron los algoritmos de John o David.
