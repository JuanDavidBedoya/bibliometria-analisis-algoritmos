package com.uniquindio.bibliometria.algoritmos.ordenamiento;

import java.util.ArrayList;
import java.util.List;

import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;

/**
 * Implementación explícita, simple y paso a paso del algoritmo COUNTING SORT (Ordenamiento por Conteo).
 *
 * =========================================================================================
 * FUNDAMENTACIÓN TEÓRICA Y COMPLEJIDAD ASINTÓTICA:
 * =========================================================================================
 * A diferencia de los algoritmos de ordenamiento basados en comparaciones entre pares de claves
 * (como QuickSort o MergeSort, cuya cota inferior teórica demostrada por el árbol de decisión
 * es Ω(n log n)), Counting Sort es un algoritmo de ordenamiento que no utiliza comparaciones directas.
 *
 * Aprovecha que las claves a ordenar son números enteros positivos que representan la frecuencia
 * de aparición de cada término dentro del corpus documental bibliométrico.
 *
 * Sea n el número total de términos a ordenar y k el rango de valores posibles de las frecuencias
 * (k = maxFrecuencia - minFrecuencia + 1):
 *
 * El algoritmo opera en 4 fases lineales bien definidas:
 *   1. Análisis de Rango [O(n)]:
 *      Se recorre la lista una vez para determinar la frecuencia mínima (min) y máxima (max).
 *      El rango de dispersión es k = max - min + 1.
 *
 *   2. Conteo de Frecuencias [O(n)]:
 *      Se crea un arreglo auxiliar de enteros de tamaño k, inicializado en ceros.
 *      Por cada elemento de la lista de entrada con frecuencia f, se incrementa la posición
 *      (f - min), calculando el histograma exacto de repetición de cada frecuencia.
 *
 *   3. Sumas Acumuladas o Frecuencias Acumuladas (Prefix Sums) [O(k)]:
 *      Se transforma el arreglo de conteo sumando los valores acumulados. Para el caso de
 *      ordenamiento DESCENDENTE (ranking bibliométrico de mayor a menor frecuencia), la acumulación
 *      se realiza de derecha a izquierda (desde la frecuencia máxima hacia la mínima).
 *      Esto determina de forma directa y determinista el índice final que ocupará cada elemento
 *      en el arreglo ordenado de salida.
 *
 *   4. Ubicación en el Arreglo de Salida y Estabilidad [O(n)]:
 *      Se itera la lista de entrada de atrás hacia adelante (desde n-1 hasta 0), colocando cada
 *      término en su posición final calculada y decrementando el conteo acumulado correspondiente.
 *      El recorrido en reversa garantiza que el algoritmo sea ESTABLE (los términos con igual
 *      frecuencia conservan el orden relativo original).
 *
 * COMPLEJIDAD ASINTÓTICA TOTAL:
 *   - Complejidad Temporal: O(n + k) en el mejor, peor y caso promedio.
 *     En el contexto bibliométrico, donde k representa la frecuencia máxima de una palabra
 *     (k << n o k ≈ O(n)), el tiempo de ejecución es estrictamente LINEAL: O(n).
 *   - Complejidad Espacial Auxiliar: O(n + k), correspondiente al arreglo de conteo de tamaño k
 *     y al arreglo de salida de tamaño n.
 *
 * REGLA DE TRANSPARENCIA:
 * Esta clase no utiliza funciones de alto nivel (como Collections.sort() ni Arrays.sort()).
 * Todo el ordenamiento se implementa con arreglos nativos y ciclos elementales.
 */
public final class CountingSort {

    private CountingSort() {
        // Constructor privado para clase utilitaria estática
    }

    /**
     * Ordena una lista de términos por su frecuencia en orden descendente (mayor a menor),
     * correspondiente al ranking bibliométrico solicitado.
     *
     * @param lista lista de pares (término, frecuencia) a ordenar.
     * @return {@link ResultadoOrdenamiento} con la lista ordenada y las métricas de ejecución.
     */
    public static ResultadoOrdenamiento ordenarDescendente(List<TerminoFrecuencia> lista) {
        return ordenar(lista, true);
    }

    /**
     * Ordena una lista de términos por su frecuencia especificando la dirección del ordenamiento.
     *
     * @param lista lista de pares (término, frecuencia) a ordenar.
     * @param descendente true para mayor a menor (ranking); false para menor a mayor.
     * @return {@link ResultadoOrdenamiento} con la lista ordenada y las métricas de ejecución.
     */
    public static ResultadoOrdenamiento ordenar(List<TerminoFrecuencia> lista, boolean descendente) {
        if (lista == null || lista.isEmpty()) {
            return new ResultadoOrdenamiento("Counting Sort", List.of(), 0.0, 0, 0, 0, 0);
        }

        int n = lista.size();

        // Inicio de la medición empírica de tiempo real en nanosegundos
        long tiempoInicioNano = System.nanoTime();

        // Caso base: si la lista contiene un solo elemento, ya está ordenada
        if (n == 1) {
            long tiempoFinNano = System.nanoTime();
            double duracionMs = (tiempoFinNano - tiempoInicioNano) / 1_000_000.0;
            int f = lista.get(0).getFrecuencia();
            return new ResultadoOrdenamiento("Counting Sort", new ArrayList<>(lista), duracionMs, 1, 1, f, f);
        }

        // -------------------------------------------------------------------------
        // PASO 1: Encontrar los valores mínimo y máximo de frecuencia en O(n)
        // -------------------------------------------------------------------------
        int minFrecuencia = lista.get(0).getFrecuencia();
        int maxFrecuencia = lista.get(0).getFrecuencia();

        for (int i = 1; i < n; i++) {
            int f = lista.get(i).getFrecuencia();
            if (f < minFrecuencia) {
                minFrecuencia = f;
            }
            if (f > maxFrecuencia) {
                maxFrecuencia = f;
            }
        }

        // Si todas las frecuencias son exactamente iguales, la lista ya se encuentra ordenada
        if (minFrecuencia == maxFrecuencia) {
            long tiempoFinNano = System.nanoTime();
            double duracionMs = (tiempoFinNano - tiempoInicioNano) / 1_000_000.0;
            return new ResultadoOrdenamiento("Counting Sort", new ArrayList<>(lista), duracionMs, n, 1, minFrecuencia, maxFrecuencia);
        }

        // Rango de frecuencias k: define el tamaño del arreglo de conteo
        int k = maxFrecuencia - minFrecuencia + 1;

        // -------------------------------------------------------------------------
        // PASO 2: Construir el arreglo de conteo (Histograma de frecuencias) en O(n)
        // Cada índice (frecuencia - minFrecuencia) almacena cuántas veces aparece dicha frecuencia
        // -------------------------------------------------------------------------
        int[] conteo = new int[k];
        for (int i = 0; i < n; i++) {
            int frecuencia = lista.get(i).getFrecuencia();
            conteo[frecuencia - minFrecuencia]++;
        }

        // -------------------------------------------------------------------------
        // PASO 3: Calcular las sumas acumuladas (Prefix Sums) en O(k)
        // Transforma el arreglo de conteo para que conteo[i] indique la última posición
        // que ocupará un elemento con dicha clave en el arreglo de salida.
        // -------------------------------------------------------------------------
        if (descendente) {
            // Para orden descendente (mayor a menor frecuencia), acumulamos desde
            // la frecuencia más alta (k - 1) hacia la más baja (0)
            for (int i = k - 2; i >= 0; i--) {
                conteo[i] += conteo[i + 1];
            }
        } else {
            // Para orden ascendente (menor a mayor), acumulamos de izquierda a derecha
            for (int i = 1; i < k; i++) {
                conteo[i] += conteo[i - 1];
            }
        }

        // -------------------------------------------------------------------------
        // PASO 4: Construir el arreglo de salida en O(n) garantizando ESTABILIDAD
        // Recorremos la lista original de derecha a izquierda (desde n - 1 hasta 0).
        // -------------------------------------------------------------------------
        TerminoFrecuencia[] salida = new TerminoFrecuencia[n];

        for (int i = n - 1; i >= 0; i--) {
            TerminoFrecuencia actual = lista.get(i);
            int claveIndice = actual.getFrecuencia() - minFrecuencia;

            // La posición en el arreglo ordenado (base 0) es conteo[claveIndice] - 1
            int posicionSalida = conteo[claveIndice] - 1;
            salida[posicionSalida] = actual;

            // Se decrementa el contador para que el siguiente elemento con igual clave
            // ocupe la posición inmediatamente anterior (preserva estabilidad)
            conteo[claveIndice]--;
        }

        // Convertir el arreglo elemental a la lista de resultado
        List<TerminoFrecuencia> resultado = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            resultado.add(salida[i]);
        }

        // Fin de la medición empírica de tiempo real
        long tiempoFinNano = System.nanoTime();
        double duracionMs = (tiempoFinNano - tiempoInicioNano) / 1_000_000.0;

        return new ResultadoOrdenamiento("Counting Sort", resultado, duracionMs, n, k, minFrecuencia, maxFrecuencia);
    }
}
