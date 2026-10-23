/**
 * pages/BucketSortPage.jsx
 *
 * Ventana 2: Ejecución interactiva del algoritmo Bucket Sort.
 * Muestra el panel de configuración, las métricas del algoritmo requeridas
 * (n, B, fmin/fmax) y el ranking en tabla visual.
 */

import { useState } from 'react'
import { obtenerResultadoBucketSort } from '../api/bucketSortApi'
import PanelControles   from '../components/PanelControles'
import MensajeError     from '../components/MensajeError'
import TarjetasMetricas from '../components/TarjetasMetricas'
import TablaResultados  from '../components/TablaResultados'

function BucketSortPage() {
  const [topK, setTopK]                         = useState(15)
  const [filtrarStopwords, setFiltrarStopwords] = useState(true)

  const [resultado, setResultado] = useState(null)
  const [cargando, setCargando]   = useState(false)
  const [error, setError]         = useState(null)

  async function ejecutarBucketSort() {
    setCargando(true)
    setError(null)

    try {
      const datos = await obtenerResultadoBucketSort(topK, filtrarStopwords)
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
        <div className="header-badge">VENTANA 2 · ANÁLISIS DE ALGORITMOS</div>
        <h1>Algoritmo Bucket Sort (Ordenamiento por Cubetas)</h1>
        <p className="pagina-descripcion">
          Algoritmo de distribución lineal con complejidad esperada <strong>O(n + B)</strong>.
          Mapea las frecuencias de los términos a <em>B</em> cubetas mediante dispersión cuantitativa
          y realiza inserción interna para generar el ranking bibliométrico sin requerir comparaciones globales.
        </p>
      </header>

      {/* Panel de Controles */}
      <PanelControles
        topK={topK}
        setTopK={setTopK}
        filtrarStopwords={filtrarStopwords}
        setFiltrarStopwords={setFiltrarStopwords}
        cargando={cargando}
        onEjecutar={ejecutarBucketSort}
        nombreAlgoritmo="Bucket Sort"
      />

      {/* Error si el backend no está disponible */}
      {error && <MensajeError mensaje={error} />}

      {/* Resultados */}
      {resultado && (
        <section className="seccion-resultados-morada">
          {/* APARTADO REQUERIDO: Métricas y Parámetros */}
          <TarjetasMetricas
            tiempoMs={resultado.tiempoMs || 0}
            totalElementos={resultado.totalElementos || 0}
            numeroCubetas={resultado.numeroCubetas || 0}
            frecuenciaMinima={resultado.frecuenciaMinima ?? 1}
            frecuenciaMaxima={resultado.frecuenciaMaxima ?? 1}
            totalArticulos={resultado.totalArticulos || 20}
            algoritmo="Bucket Sort"
          />

          {/* Tabla de Ranking */}
          <div className="ranking-header-row">
            <div>
              <h2>🏆 Ranking de Términos (Top {resultado.topK})</h2>
              <span className="ranking-subtitulo">
                Vocabulario {resultado.stopwordsFiltradas ? 'Temático (sin stopwords)' : 'Completo (con stopwords)'}
              </span>
            </div>
            <div className="chip-resumen-algoritmo">
              <span>Algoritmo:</span> <strong>{resultado.algoritmo}</strong>
            </div>
          </div>

          <TablaResultados terminos={resultado.terminos} />

          <div className="resultado-pie-info">
            <span>
              ℹ️ Los {resultado.totalElementos} términos fueron distribuidos en {resultado.numeroCubetas} cubetas
              en {resultado.tiempoMs?.toFixed(4)} milisegundos mediante Bucket Sort.
            </span>
          </div>
        </section>
      )}

      {/* Mensaje de bienvenida si aún no se ha ejecutado */}
      {!resultado && !cargando && !error && (
        <div className="bienvenida-box">
          <div className="bienvenida-icon">✨</div>
          <h3>Listo para ejecutar el algoritmo</h3>
          <p>
            Configura el tamaño del ranking deseado y el filtro de stopwords, luego presiona
            <strong> "Ejecutar Bucket Sort"</strong> para procesar el corpus y ver las métricas de cubetas y frecuencias.
          </p>
        </div>
      )}
    </div>
  )
}

export default BucketSortPage
