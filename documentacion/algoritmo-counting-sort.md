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

## 2. Medición Empírica y Validación de Resultados

Se ejecutó la implementación sobre el corpus completo de los 20 artículos científicos preprocesados (`datos/procesados/corpus_procesado.json`).

### Tabla 1: Top 15 Términos Temáticos (Sin Stopwords)

* **Parámetros**: $n = 1012$ términos distintos, $f_{\text{min}} = 1$, $f_{\text{max}} = 77$, rango $k = 77$.
* **Tiempo real de ejecución**: **0.3271 ms**

| Puesto | Término | Frecuencia |
|:---:|:---|:---:|
| #1 | ai | 77 |
| #2 | education | 57 |
| #3 | research | 46 |
| #4 | learning | 40 |
| #5 | aied | 36 |
| #6 | review | 36 |
| #7 | educational | 33 |
| #8 | assessment | 32 |
| #9 | intelligence | 22 |
| #10 | studies | 21 |
| #11 | higher | 16 |
| #12 | artificial | 16 |
| #13 | technologies | 15 |
| #14 | literature | 15 |
| #15 | systematic | 15 |

---

### Tabla 2: Top 15 Vocabulario Completo (Con Stopwords)

* **Parámetros**: $n = 1140$ términos distintos, $f_{\text{min}} = 1$, $f_{\text{max}} = 221$, rango $k = 221$.
* **Tiempo real de ejecución**: **0.2192 ms**

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

## 3. Instrucciones de Ejecución

### Modo Consola Automático
Muestra directamente las tablas del Top 15 y métricas empíricas:
```powershell
java -cp target\classes com.uniquindio.bibliometria.EjecutorCountingSort --auto
```

### Modo Consola Interactivo
Permite seleccionar vocabulario con/sin stopwords y personalizar el tamaño del Top:
```powershell
java -cp target\classes com.uniquindio.bibliometria.EjecutorCountingSort
```

### Endpoint REST (Spring Boot)
Con la aplicación Spring Boot iniciada (`mvn spring-boot:run` o `BibliometriaApplication`):
```http
GET http://localhost:8080/api/counting-sort?topK=15&filtrarStopwords=true
```
Respuesta JSON:
```json
{
  "algoritmo": "Counting Sort",
  "terminos": [
    { "termino": "ai", "frecuencia": 77 },
    { "termino": "education", "frecuencia": 57 }
  ],
  "tiempoMs": 0.3271,
  "totalElementos": 1012,
  "rangoK": 77,
  "frecuenciaMinima": 1,
  "frecuenciaMaxima": 77,
  "topK": 15,
  "stopwordsFiltradas": true,
  "totalArticulos": 20
}
```
