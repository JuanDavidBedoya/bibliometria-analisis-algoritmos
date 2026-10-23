/**
 * pages/BucketSortPage.jsx
 *
 * Página principal de la aplicación. Orquesta todos los componentes
 * y contiene el estado y la lógica de negocio del flujo de Bucket Sort:
 *
 *   1. El usuario configura los parámetros (Top K, stopwords).
 *   2. Presiona el botón → se llama a la API del backend.
 *   3. Se muestran las métricas y la tabla de resultados.
 */

import { useState } from 'react'
import { obtenerResultadoBucketSort } from '../api/bucketSortApi'
import PanelControles   from '../components/PanelControles'
import MensajeError     from '../components/MensajeError'
import TarjetasMetricas from '../components/TarjetasMetricas'
import TablaResultados  from '../components/TablaResultados'

function BucketSortPage() {
  // ─── Estado de parámetros ──────────────────────────────────────────────────
  const [topK, setTopK]                         = useState(15)
  const [filtrarStopwords, setFiltrarStopwords] = useState(true)

  // ─── Estado de la petición ─────────────────────────────────────────────────
  const [resultado, setResultado] = useState(null)
  const [cargando, setCargando]   = useState(false)
  const [error, setError]         = useState(null)

  // ─── Llamada al backend ────────────────────────────────────────────────────
  async function ejecutarBucketSort() {
    setCargando(true)
    setError(null)
    setResultado(null)

    try {
      const datos = await obtenerResultadoBucketSort(topK, filtrarStopwords)
      setResultado(datos)
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  // ─── Render ────────────────────────────────────────────────────────────────
  return (
    <div className="app">

      {/* Panel de parámetros y botón de ejecución */}
      <PanelControles
        topK={topK}
        setTopK={setTopK}
        filtrarStopwords={filtrarStopwords}
        setFiltrarStopwords={setFiltrarStopwords}
        cargando={cargando}
        onEjecutar={ejecutarBucketSort}
      />

      {/* Error de conexión o servidor */}
      {error && <MensajeError mensaje={error} />}

      {/* Resultados del ordenamiento */}
      {resultado && (
        <section className="resultados">

          <TarjetasMetricas
            tiempoMs={resultado.tiempoMs}
            totalElementos={resultado.totalElementos}
            numeroCubetas={resultado.numeroCubetas}
            totalArticulos={resultado.totalArticulos}
          />

          <h2>🏆 Top {resultado.topK} — Términos más frecuentes</h2>

          <TablaResultados terminos={resultado.terminos} />

          <p className="nota">
            Algoritmo: <strong>{resultado.algoritmo}</strong> ·{' '}
            Stopwords filtradas: <strong>{resultado.stopwordsFiltradas ? 'Sí' : 'No'}</strong>
          </p>

        </section>
      )}

    </div>
  )
}

export default BucketSortPage
