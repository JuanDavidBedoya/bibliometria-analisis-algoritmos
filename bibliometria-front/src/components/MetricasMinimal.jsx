/**
 * components/MetricasMinimal.jsx
 *
 * Tarjetas de métricas y parámetros con estilo minimalista en tonos verdes.
 *   - Número de términos distintos (n)
 *   - Frecuencia mínima / máxima (fmin / fmax)
 *   - Número de cubetas utilizado (B)
 *   - Tiempo empírico de ejecución (ms)
 */

function MetricasMinimal({
  tiempoMs = 0,
  totalElementos = 0,
  numeroCubetas = 0,
  frecuenciaMinima = 1,
  frecuenciaMaxima = 1,
  totalArticulos = 20,
  algoritmo = 'Bucket Sort'
}) {
  const metricas = [
    {
      id: 'n',
      etiqueta: 'Términos distintos',
      simbolo: 'n',
      valor: totalElementos.toLocaleString(),
      subtitulo: 'Vocabulario analizado',
      icono: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 7V4h16v3M9 20h6M12 4v16" />
        </svg>
      )
    },
    {
      id: 'frecuencias',
      etiqueta: 'Rango frecuencias',
      simbolo: 'f_mín · f_máx',
      valor: `${frecuenciaMinima} / ${frecuenciaMaxima}`,
      subtitulo: 'Dispersión de ocurrencias',
      icono: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
        </svg>
      )
    },
    {
      id: 'b',
      etiqueta: 'Cubetas activas',
      simbolo: 'B',
      valor: numeroCubetas,
      subtitulo: `B ≈ √n (${numeroCubetas} cubetas)`,
      icono: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" />
          <path d="m3.3 7 8.7 5 8.7-5" />
          <path d="M12 22V12" />
        </svg>
      )
    },
    {
      id: 'tiempo',
      etiqueta: 'Tiempo empírico',
      simbolo: 'O(n + B)',
      valor: `${tiempoMs.toFixed(3)} ms`,
      subtitulo: 'Medido en nanosegundos',
      icono: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
      )
    }
  ]

  return (
    <div className="metricas-min-grid">
      {metricas.map(m => (
        <div key={m.id} className="metrica-min-card metrica-min-card-verde">
          <div className="metrica-min-top">
            <span className="metrica-min-tag metrica-min-tag-verde">{m.simbolo}</span>
            <div className="metrica-min-icon-wrapper metrica-min-icon-verde">{m.icono}</div>
          </div>
          <div className="metrica-min-main">
            <span className="metrica-min-valor">{m.valor}</span>
            <span className="metrica-min-label">{m.etiqueta}</span>
          </div>
          <span className="metrica-min-sub">{m.subtitulo}</span>
        </div>
      ))}
    </div>
  )
}

export default MetricasMinimal
