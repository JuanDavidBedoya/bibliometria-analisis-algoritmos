/**
 * components/Sidebar.jsx
 *
 * Menú lateral izquierdo con la paleta de colores morados.
 * Permite alternar entre las tres ventanas de la aplicación:
 *   1. "informacion"  - Información y títulos de los artículos del estudio
 *   2. "countingsort" - Ejecución y análisis del algoritmo Counting Sort
 *   3. "bucketsort"   - Ejecución y análisis del algoritmo Bucket Sort
 */

function Sidebar({ ventanaActiva, setVentanaActiva, totalArticulos = 20 }) {
  const items = [
    {
      id: 'informacion',
      titulo: 'Información del Corpus',
      subtitulo: 'Artículos del estudio',
      icono: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          <line x1="9" y1="7" x2="15" y2="7" />
          <line x1="9" y1="11" x2="15" y2="11" />
        </svg>
      ),
      badge: `${totalArticulos} arts.`
    },
    {
      id: 'countingsort',
      titulo: 'Algoritmo Counting Sort',
      subtitulo: 'Conteo y prefijos O(n+k)',
      icono: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="20" x2="18" y2="10" />
          <line x1="12" y1="20" x2="12" y2="4" />
          <line x1="6" y1="20" x2="6" y2="14" />
        </svg>
      ),
      badge: 'O(n+k)'
    },
    {
      id: 'bucketsort',
      titulo: 'Algoritmo Bucket Sort',
      subtitulo: 'Ranking y métricas O(n)',
      icono: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2v20" />
          <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
          <rect x="3" y="3" width="18" height="18" rx="2" />
        </svg>
      ),
      badge: 'O(n)'
    }
  ]

  return (
    <aside className="sidebar">
      {/* Encabezado del menú */}
      <div className="sidebar-brand">
        <div className="brand-icon">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
        </div>
        <div className="brand-info">
          <h2>Bibliometría</h2>
          <span>Análisis de Algoritmos</span>
        </div>
      </div>

      <div className="sidebar-section-title">MENÚ PRINCIPAL</div>

      {/* Lista de navegación */}
      <nav className="sidebar-nav">
        {items.map(item => {
          const activo = ventanaActiva === item.id
          return (
            <button
              key={item.id}
              className={`nav-btn ${activo ? 'activo' : ''}`}
              onClick={() => setVentanaActiva(item.id)}
            >
              <div className="nav-btn-icon">{item.icono}</div>
              <div className="nav-btn-text">
                <span className="nav-btn-title">{item.titulo}</span>
                <span className="nav-btn-sub">{item.subtitulo}</span>
              </div>
              {item.badge && <span className="nav-btn-badge">{item.badge}</span>}
            </button>
          )
        })}
      </nav>

      {/* Pie del menú */}
      <div className="sidebar-footer">
        <div className="footer-card">
          <div className="footer-indicator"></div>
          <div>
            <strong>Universidad del Quindío</strong>
            <p>Ingeniería de Sistemas · 2026</p>
          </div>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
