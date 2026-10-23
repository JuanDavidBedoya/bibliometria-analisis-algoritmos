/**
 * components/TarjetasMetricas.jsx
 *
 * Muestra las métricas de desempeño del algoritmo Bucket Sort:
 * tiempo de ejecución, total de elementos ordenados, número de cubetas
 * y cantidad de artículos del corpus.
 *
 * Props:
 *   tiempoMs       {number} - Tiempo real de ejecución en milisegundos.
 *   totalElementos {number} - Cantidad de términos únicos ordenados.
 *   numeroCubetas  {number} - Número de cubetas usadas por Bucket Sort.
 *   totalArticulos {number} - Cantidad de artículos en el corpus.
 */

function TarjetasMetricas({ tiempoMs, totalElementos, numeroCubetas, totalArticulos }) {
  const metricas = [
    { valor: `${tiempoMs.toFixed(3)} ms`, etiqueta: 'Tiempo de ejecución' },
    { valor: totalElementos,              etiqueta: 'Términos ordenados'   },
    { valor: numeroCubetas,               etiqueta: 'Cubetas (buckets)'    },
    { valor: totalArticulos,              etiqueta: 'Artículos del corpus' },
  ]

  return (
    <div className="metricas">
      {metricas.map(m => (
        <div className="metrica" key={m.etiqueta}>
          <span className="valor">{m.valor}</span>
          <span className="etiqueta">{m.etiqueta}</span>
        </div>
      ))}
    </div>
  )
}

export default TarjetasMetricas
