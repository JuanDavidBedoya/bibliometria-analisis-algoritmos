/**
 * components/TablaEjecuciones.jsx
 *
 * Muestra la tabla de las 5 ejecuciones consecutivas del algoritmo
 * y el cálculo exacto del tiempo promedio en milisegundos.
 */

function TablaEjecuciones({
  ejecuciones = [],
  promedio = 0,
  stopwordsFiltradas = true,
  totalElementos = 1012
}) {
  if (!ejecuciones || ejecuciones.length === 0) {
    return null
  }

  // Encontrar el valor máximo entre las ejecuciones para escalar las barras visuales
  const maxTiempo = Math.max(...ejecuciones, promedio, 0.001)

  return (
    <section className="seccion-ejecuciones-card">
      <div className="ejecuciones-header">
        <div className="ejecuciones-badge">MEDICIÓN EMPÍRICA FORMAL · 5 MUESTRAS</div>
        <div className="ejecuciones-titulo-row">
          <div>
            <h3>⏱️ Desempeño Experimental: 5 Ejecuciones y Promedio</h3>
            <p className="ejecuciones-sub">
              Registro secuencial de tiempos reales sobre los {totalElementos.toLocaleString()} términos del vocabulario{' '}
              <strong>{stopwordsFiltradas ? 'temático (sin stopwords)' : 'completo (con stopwords)'}</strong>.
            </p>
          </div>
          <div className="chip-promedio-grande">
            <span className="promedio-etiqueta">Tiempo Promedio</span>
            <span className="promedio-valor">{promedio.toFixed(4)} ms</span>
          </div>
        </div>
      </div>

      <div className="tabla-ejecuciones-wrapper">
        <table className="tabla-ejecuciones">
          <thead>
            <tr>
              <th className="th-iteracion">Iteración / Muestra</th>
              <th className="th-estado">Estado de Ejecución</th>
              <th className="th-tiempo">Tiempo Real (ms)</th>
              <th className="th-barra-tiempo">Escala Relativa</th>
            </tr>
          </thead>
          <tbody>
            {ejecuciones.map((tiempo, idx) => {
              const esPrimera = idx === 0
              const porcentaje = Math.max(12, Math.round((tiempo / maxTiempo) * 100))

              return (
                <tr key={idx} className={esPrimera ? 'fila-primera-corrida' : ''}>
                  <td className="celda-iteracion">
                    <span className="numero-muestra-tag">#{idx + 1}</span>
                    <strong className="nombre-muestra">Ejecución {idx + 1}</strong>
                  </td>
                  <td className="celda-estado">
                    {esPrimera ? (
                      <span className="tag-frio">
                        <span className="punto-frio"></span> En frío (Compilación JIT)
                      </span>
                    ) : (
                      <span className="tag-caliente">
                        <span className="punto-caliente"></span> Régimen estabilizado
                      </span>
                    )}
                  </td>
                  <td className="celda-tiempo">
                    <span className="valor-ms">{tiempo.toFixed(4)}</span>
                    <span className="unidad-ms">ms</span>
                  </td>
                  <td className="celda-barra-tiempo">
                    <div className="barra-tiempo-track">
                      <div
                        className="barra-tiempo-fill"
                        style={{ width: `${porcentaje}%` }}
                      />
                    </div>
                  </td>
                </tr>
              )
            })}

            {/* Fila destacada del Promedio */}
            <tr className="fila-promedio-destacada">
              <td className="celda-iteracion">
                <span className="icono-promedio">★</span>
                <strong>PROMEDIO ARITMÉTICO</strong>
              </td>
              <td className="celda-estado">
                <span className="tag-promedio-resumen">5 muestras independientes</span>
              </td>
              <td className="celda-tiempo">
                <strong className="valor-ms-promedio">{promedio.toFixed(4)}</strong>
                <span className="unidad-ms-promedio">ms</span>
              </td>
              <td className="celda-barra-tiempo">
                <div className="barra-tiempo-track track-promedio">
                  <div
                    className="barra-tiempo-fill fill-promedio"
                    style={{ width: `${Math.round((promedio / maxTiempo) * 100)}%` }}
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="ejecuciones-nota-pie">
        <span>
          💡 <strong>Observación de Rendimiento:</strong> La primera ejecución registra la carga inicial de clases en memoria.
          Las ejecuciones 2 a 5 demuestran la velocidad real del algoritmo en régimen continuo con una dispersión temporal mínima.
        </span>
      </div>
    </section>
  )
}

export default TablaEjecuciones
