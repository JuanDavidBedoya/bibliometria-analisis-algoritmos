/**
 * components/PanelControles.jsx
 *
 * Panel con los parámetros configurables del algoritmo (Top K y filtro de stopwords)
 * y el botón para disparar la ejecución del Bucket Sort.
 *
 * Props:
 *   topK              {number}   - Valor actual del Top K.
 *   setTopK           {Function} - Setter para actualizar el Top K.
 *   filtrarStopwords  {boolean}  - Si el filtro de stopwords está activo.
 *   setFiltrarStopwords {Function} - Setter del filtro.
 *   cargando          {boolean}  - True mientras se espera respuesta del servidor.
 *   onEjecutar        {Function} - Callback que dispara la llamada al backend.
 */

function PanelControles({ topK, setTopK, filtrarStopwords, setFiltrarStopwords, cargando, onEjecutar }) {
  return (
    <section className="controles">
      <h2>⚙️ Parámetros</h2>

      {/* Campo: Top K */}
      <div className="campo">
        <label htmlFor="topK">Top K términos:</label>
        <input
          id="topK"
          type="number"
          min={1}
          max={100}
          value={topK}
          onChange={e => setTopK(Number(e.target.value))}
        />
      </div>

      {/* Campo: Filtrar stopwords */}
      <div className="campo">
        <label htmlFor="stopwords">Filtrar stopwords:</label>
        <input
          id="stopwords"
          type="checkbox"
          checked={filtrarStopwords}
          onChange={e => setFiltrarStopwords(e.target.checked)}
        />
        <span className="hint">(excluye palabras como &quot;the&quot;, &quot;and&quot;, &quot;of&quot;…)</span>
      </div>

      {/* Botón de ejecución */}
      <button
        className="btn-ejecutar"
        onClick={onEjecutar}
        disabled={cargando}
      >
        {cargando ? '⏳ Ejecutando...' : '▶ Ejecutar Bucket Sort'}
      </button>
    </section>
  )
}

export default PanelControles
