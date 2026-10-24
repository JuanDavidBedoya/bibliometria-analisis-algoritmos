# Algoritmo de Ordenamiento: Counting Sort

Documento técnico del requerimiento de ordenamiento bibliométrico para el ranking de términos frecuentes utilizando **Counting Sort (Ordenamiento por Conteo)**.

---

## 1. Fundamentación Teórica y Complejidad Asintótica

Los algoritmos de ordenamiento clásicos basados en **comparaciones directas** (como QuickSort, MergeSort o HeapSort) tienen una cota inferior matemática demostrada mediante el modelo del árbol de decisión de **$\Omega(n \log n)$**. Ningún algoritmo que decida el orden comparando pares de elementos puede ser más rápido en el caso general.

**Counting Sort** rompe esta limitación porque **no realiza comparaciones entre elementos**. En su lugar, opera como un algoritmo de distribución entera que indexa directamente las claves cuantitativas (en este caso, la frecuencia entera positiva $f \ge 1$ con la que aparece cada término en el corpus):

### Análisis de Complejidad

Sea:
* $n$: Número total de términos distintos en el vocabulario bibliométrico.
* $k$: Rango de las frecuencias de los términos ($k = f_{\text{max}} - f_{\text{min}} + 1$).

El algoritmo realiza 4 etapas de ejecución estrictamente secuenciales:

1. **Determinación del rango $[f_{\text{min}}, f_{\text{max}}]$**: Se recorren los $n$ términos en tiempo $\mathcal{O}(n)$.
2. **Construcción del histograma de frecuencias**: Se crea un arreglo de conteo de tamaño $k$ y se acumula la frecuencia de cada término en $\mathcal{O}(n)$.
3. **Cálculo de sumas acumuladas (Prefix Sums)**: Se transforman los conteos sumando los valores acumulados en $\mathcal{O}(k)$. Para orden **descendente** (ranking de mayor a menor), la suma se propaga desde $k-1$ hacia $0$.
4. **Construcción del arreglo de salida**: Se recorre la lista original de derecha a izquierda en $\mathcal{O}(n)$, ubicando cada elemento directamente en su posición final y garantizando **estabilidad**.

$$\text{Tiempo Total} = \mathcal{O}(n) + \mathcal{O}(n) + \mathcal{O}(k) + \mathcal{O}(n) = \mathcal{O}(n + k)$$

Dado que en un corpus textual la frecuencia máxima de una palabra es acotada ($k \ll n$ o $k \approx \mathcal{O}(n)$), la complejidad asintótica resultante es estrictamente **lineal: $\mathcal{O}(n)$**.

* **Complejidad Temporal**: $\mathcal{O}(n + k)$ (Mejor, peor y caso promedio).
* **Complejidad Espacial Auxiliar**: $\mathcal{O}(n + k)$ (Arreglo de conteo de tamaño $k$ y arreglo de salida de tamaño $n$).
* **Estabilidad**: Sí. Conserva el orden relativo original para términos con igual frecuencia.

---

## 2. Medición Empírica del Desempeño (5 Ejecuciones Consecutivas)

Se ejecutó la implementación sobre el corpus completo de los 20 artículos científicos preprocesados (`datos/procesados/corpus_procesado.json`).

### Tabla 2.1: Detalle de las 5 Ejecuciones y Promedio - Vocabulario Temático (Sin Stopwords)

* **Parámetros del experimento**:
  * Términos distintos ($n$): **1012**
  * Frecuencia mínima ($f_{\text{min}}$): **1**
  * Frecuencia máxima ($f_{\text{max}}$): **77**
  * Rango de frecuencias ($k$): **77**

| Muestra / Iteración | Términos ($n$) | Frec. Mín / Máx | Rango ($k$) | Tiempo Real de Ejecución |
|:---|:---:|:---:|:---:|:---:|
| **Ejecución 1 (En frío / JIT)** | 1012 | 1 / 77 | 77 | **0.3505 ms** |
| **Ejecución 2** | 1012 | 1 / 77 | 77 | **0.2618 ms** |
| **Ejecución 3** | 1012 | 1 / 77 | 77 | **0.2056 ms** |
| **Ejecución 4** | 1012 | 1 / 77 | 77 | **0.2215 ms** |
| **Ejecución 5** | 1012 | 1 / 77 | 77 | **0.2265 ms** |
| **PROMEDIO (5 Ejecuciones)** | **1012** | **1 / 77** | **77** | **0.2532 ms** |

> **Observación Técnica:** La Ejecución 1 refleja la sobrecarga en frío de carga de clases e inicialización del compilador JIT (0.3505 ms). A partir de la Ejecución 2, el tiempo se estabiliza entre 0.20 ms y 0.26 ms, confirmando el comportamiento estrictamente lineal $\mathcal{O}(n + k)$ con un tiempo promedio de **0.2532 ms**.

---

### Tabla 2.2: Comparativa de Escenarios

| Escenario Bibliométrico | Términos ($n$) | Frec. Mín | Frec. Máx | Rango ($k$) | Tiempo Promedio (5 Ejecuciones) |
|:---|:---:|:---:|:---:|:---:|:---:|
| **Vocabulario Temático (Sin Stopwords)** | **1012** | **1** | **77** | **77** | **0.2532 ms** |
| **Vocabulario Completo (Con Stopwords)** | **1140** | **1** | **221** | **221** | **0.2397 ms** |

---

## 3. Validación de Resultados: Rankings Top 15

### Tabla 3.1: Top 15 Términos Temáticos (Sin Stopwords)

| Puesto | Término | Frecuencia | Densidad en Top |
|:---:|:---|:---:|:---:|
| #1 | ai | 77 | 7.61% |
| #2 | education | 57 | 5.63% |
| #3 | research | 46 | 4.55% |
| #4 | learning | 40 | 3.95% |
| #5 | aied | 36 | 3.56% |
| #6 | review | 36 | 3.56% |
| #7 | educational | 33 | 3.26% |
| #8 | assessment | 32 | 3.16% |
| #9 | intelligence | 22 | 2.17% |
| #10 | studies | 21 | 2.08% |
| #11 | higher | 16 | 1.58% |
| #12 | artificial | 16 | 1.58% |
| #13 | technologies | 15 | 1.48% |
| #14 | literature | 15 | 1.48% |
| #15 | systematic | 15 | 1.48% |

---

### Tabla 3.2: Top 15 Vocabulario Completo (Con Stopwords)

| Puesto | Término | Frecuencia |
|:---:|:---|:---:|
| #1 | and | 221 |
| #2 | the | 197 |
| #3 | of | 169 |
| #4 | in | 125 |
| #5 | to | 95 |
| #6 | ai | 77 |
| #7 | a | 61 |
| #8 | education | 57 |
| #9 | for | 54 |
| #10 | research | 46 |
| #11 | learning | 40 |
| #12 | this | 37 |
| #13 | aied | 36 |
| #14 | review | 36 |
| #15 | educational | 33 |

---

## 4. Instrucciones de Ejecución

### Modo Consola Automático
Muestra directamente las tablas del Top 15 y métricas empíricas:
```powershell
java -cp target\classes com.uniquindio.bibliometria.EjecutorCountingSort --auto
```

### Medidor de las 5 Ejecuciones
Ejecuta las 5 iteraciones consecutivas y calcula el promedio aritmético:
```powershell
java -cp target\classes com.uniquindio.bibliometria.util.MedidorEjecuciones
```

### Endpoint REST (Spring Boot)
```http
GET http://localhost:8080/api/counting-sort?topK=15&filtrarStopwords=true
```
