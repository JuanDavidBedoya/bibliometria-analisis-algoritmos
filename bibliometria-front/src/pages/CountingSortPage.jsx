/**
 * pages/CountingSortPage.jsx
 *
 * Ventana: Ejecución interactiva del algoritmo Counting Sort.
 * Muestra el panel de configuración, las métricas del algoritmo requeridas
 * (n, k, fmin/fmax), la tabla con las 5 ejecuciones y su promedio empírico,
 * y el ranking visual en tabla.
 */

import { useState } from 'react'
import { obtenerResultadoCountingSort } from '../api/countingSortApi'
import PanelControles   from '../components/PanelControles'
import MensajeError     from '../components/MensajeError'
import TarjetasMetricas from '../components/TarjetasMetricas'
import TablaEjecuciones from '../components/TablaEjecuciones'
import TablaResultados  from '../components/TablaResultados'

function CountingSortPage() {
  const [topK, setTopK]                         = useState(15)
  const [filtrarStopwords, setFiltrarStopwords] = useState(true)

  const [resultado, setResultado] = useState(null)
  const [cargando, setCargando]   = useState(false)
  const [error, setError]         = useState(null)

  async function ejecutarCountingSort() {
    setCargando(true)
    setError(null)

    try {
      const datos = await obtenerResultadoCountingSort(topK, filtrarStopwords)
      setResultado(datos)
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="pagina-contenedor">
      {/* Encabezado */}
      <header className="pagina-header">
        <div className="header-badge">ALGORITMOS DE ORDENAMIENTO · SEGUIMIENTO</div>
        <h1>Algoritmo Counting Sort (Ordenamiento por Conteo)</h1>
        <p className="pagina-descripcion">
          Algoritmo de ordenamiento entero no comparativo con complejidad asintótica <strong>O(n + k)</strong>.
          Calcula un histograma de frecuencias de los términos y genera sumas acumuladas (prefix sums)
          para ubicar cada elemento de manera directa y determinista en tiempo estrictamente lineal.
        </p>
      </header>

      {/* Panel de Controles */}
      <PanelControles
        topK={topK}
        setTopK={setTopK}
        filtrarStopwords={filtrarStopwords}
        setFiltrarStopwords={setFiltrarStopwords}
        cargando={cargando}
        onEjecutar={ejecutarCountingSort}
        nombreAlgoritmo="Counting Sort"
      />

      {/* Error si el backend no está disponible */}
      {error && <MensajeError mensaje={error} />}

      {/* Resultados */}
      {resultado && (
        <section className="seccion-resultados-morada">
          {/* Métricas y Parámetros */}
          <TarjetasMetricas
            tiempoMs={resultado.tiempoPromedioMs || resultado.tiempoMs || 0}
            totalElementos={resultado.totalElementos || 0}
            rangoK={resultado.rangoK || resultado.numeroCubetas || 0}
            numeroCubetas={resultado.rangoK || resultado.numeroCubetas || 0}
            frecuenciaMinima={resultado.frecuenciaMinima ?? 1}
            frecuenciaMaxima={resultado.frecuenciaMaxima ?? 1}
            totalArticulos={resultado.totalArticulos || 20}
            algoritmo="Counting Sort"
          />

          {/* APARTADO REQUERIDO: TABLA DE LAS 5 EJECUCIONES Y PROMEDIO */}
          {resultado.ejecucionesMs && resultado.ejecucionesMs.length > 0 && (
            <TablaEjecuciones
              ejecuciones={resultado.ejecucionesMs}
              promedio={resultado.tiempoPromedioMs || resultado.tiempoMs || 0}
              stopwordsFiltradas={resultado.stopwordsFiltradas}
              totalElementos={resultado.totalElementos}
            />
          )}

          {/* Tabla de Ranking */}
          <div className="ranking-header-row">
            <div>
              <h2>🏆 Ranking de Términos (Top {resultado.topK})</h2>
              <span className="ranking-subtitulo">
                Vocabulario {resultado.stopwordsFiltradas ? 'Temático (sin stopwords)' : 'Completo (con stopwords)'}
              </span>
            </div>
            <div className="chip-resumen-algoritmo">
              <span>Algoritmo:</span> <strong>Counting Sort</strong>
            </div>
          </div>

          <TablaResultados terminos={resultado.terminos} />

          <div className="resultado-pie-info">
            <span>
              ℹ️ Los {resultado.totalElementos} términos fueron ordenados con un rango de frecuencias k = {resultado.rangoK || resultado.numeroCubetas}. Tiempo promedio de las 5 ejecuciones: {(resultado.tiempoPromedioMs || resultado.tiempoMs)?.toFixed(4)} ms.
            </span>
          </div>
        </section>
      )}

      {/* Mensaje de bienvenida si aún no se ha ejecutado */}
      {!resultado && !cargando && !error && (
        <div className="bienvenida-box">
          <div className="bienvenida-icon">📊</div>
          <h3>Listo para ejecutar Counting Sort</h3>
          <p>
            Configura el tamaño del ranking deseado y el filtro de stopwords, luego presiona
            <strong> "Ejecutar Counting Sort"</strong> para procesar el corpus en tiempo lineal O(n + k),
            calcular las 5 ejecuciones empíricas y visualizar el ranking.
          </p>
        </div>
      )}
    </div>
  )
}

export default CountingSortPage
