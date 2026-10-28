package com.uniquindio.bibliometria.algoritmos.ordenamiento;

import java.util.List;
import java.util.Objects;

import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;

/**
 * Encapsula el resultado de la ejecución de un algoritmo de ordenamiento,
 * incluyendo la lista ordenada, métricas de desempeño empírico (tiempo real en milisegundos)
 * y utilidades para obtener rankings (ej. Top 15).
 */
public record ResultadoOrdenamiento(
        String algoritmo,
        List<TerminoFrecuencia> terminosOrdenados,
        double tiempoMilisegundos,
        int totalElementos,
        int numeroCubetas,
        int frecuenciaMinima,
        int frecuenciaMaxima
) {
    public ResultadoOrdenamiento {
        Objects.requireNonNull(algoritmo, "algoritmo no puede ser nulo");
        terminosOrdenados = terminosOrdenados == null ? List.of() : List.copyOf(terminosOrdenados);
    }

    /** Constructor de compatibilidad sin frecuencia mínima ni máxima explícitas. */
    public ResultadoOrdenamiento(
            String algoritmo,
            List<TerminoFrecuencia> terminosOrdenados,
            double tiempoMilisegundos,
            int totalElementos,
            int numeroCubetas
    ) {
        this(algoritmo, terminosOrdenados, tiempoMilisegundos, totalElementos, numeroCubetas, 0, 0);
    }

    /**
     * Retorna los primeros K términos más frecuentes (Top K).
     */
    public List<TerminoFrecuencia> getTop(int k) {
        int limite = Math.min(k, terminosOrdenados.size());
        return terminosOrdenados.subList(0, limite);
    }

    /**
     * Imprime una tabla formateada en consola con el Top K de términos más frecuentes
     * y los parámetros requeridos del algoritmo.
     */
    public void imprimirTop(int k) {
        // Método vaciado para no imprimir por terminal
    }
}
