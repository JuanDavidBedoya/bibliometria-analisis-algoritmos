/**
 * components/Sidebar.jsx
 *
 * Barra lateral con diseño minimalista en paleta de colores VERDE.
 * Tipografía Poppins, estados limpios y colapsado discreto.
 */
import { useEffect } from 'react'

function Sidebar({ ventanaActiva, setVentanaActiva, totalArticulos = 20, isOpen, setIsOpen }) {
  const items = [
    {
      id: 'informacion',
      titulo: 'Información',
      subtitulo: 'Corpus de artículos',
      icono: (
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          <line x1="9" y1="7" x2="15" y2="7" />
          <line x1="9" y1="11" x2="15" y2="11" />
        </svg>
      ),
      badge: `${totalArticulos}`
    },
    {
      id: 'bucketsort',
      titulo: 'Bucket Sort',
      subtitulo: 'Distribución O(n+B)',
      icono: (
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" />
          <path d="m3.3 7 8.7 5 8.7-5" />
          <path d="M12 22V12" />
        </svg>
      ),
      badge: 'O(n+B)'
    },
    {
      id: 'countingsort',
      titulo: 'Counting Sort',
      subtitulo: 'Histograma O(n+k)',
      icono: (
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="20" x2="18" y2="10" />
          <line x1="12" y1="20" x2="12" y2="4" />
          <line x1="6" y1="20" x2="6" y2="14" />
        </svg>
      ),
      badge: 'O(n+k)'
    },
    {
      id: 'radixsort',
      titulo: 'Radix Sort',
      subtitulo: 'Por dígitos O(d·(n+b))',
      icono: (
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="7" height="7" />
          <rect x="14" y="3" width="7" height="7" />
          <rect x="14" y="14" width="7" height="7" />
          <rect x="3" y="14" width="7" height="7" />
        </svg>
      ),
      badge: 'O(d·(n+b))'
    }
  ]

  useEffect(() => {
    localStorage.setItem('sidebarAbierto', String(isOpen))
  }, [isOpen])

  return (
    <aside className={`sidebar-minimal sidebar-verde ${isOpen ? 'is-open' : 'is-collapsed'}`}>
      {/* Encabezado minimalista */}
      <div className="sidebar-min-header">
        <button
          className="sidebar-min-toggle"
          onClick={() => setIsOpen(!isOpen)}
          title={isOpen ? 'Contraer menú' : 'Expandir menú'}
          aria-label="Alternar menú lateral"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            {isOpen ? (
              <>
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </>
            ) : (
              <>
                <line x1="4" y1="7" x2="20" y2="7" />
                <line x1="4" y1="12" x2="20" y2="12" />
                <line x1="4" y1="17" x2="20" y2="17" />
              </>
            )}
          </svg>
        </button>

        {isOpen && (
          <div className="sidebar-min-brand">
            <div className="sidebar-min-logo sidebar-min-logo-verde">
              <span>B</span>
            </div>
            <div className="sidebar-min-title-group">
              <span className="sidebar-min-title">Bibliometría</span>
              <span className="sidebar-min-subtitle">Análisis 2026-2</span>
            </div>
          </div>
        )}
      </div>

      {isOpen && <div className="sidebar-min-section-label">ALGORITMOS & CORPUS</div>}

      {/* Lista de navegación simple en verde */}
      <nav className="sidebar-min-nav">
        {items.map(item => {
          const activo = ventanaActiva === item.id
          return (
            <button
              key={item.id}
              className={`sidebar-min-item ${activo ? 'activo activo-verde' : ''}`}
              onClick={() => setVentanaActiva(item.id)}
              title={!isOpen ? `${item.titulo} (${item.badge})` : undefined}
            >
              <span className="sidebar-min-icon">{item.icono}</span>
              {isOpen && (
                <div className="sidebar-min-text">
                  <span className="sidebar-min-item-title">{item.titulo}</span>
                  <span className="sidebar-min-item-sub">{item.subtitulo}</span>
                </div>
              )}
              {isOpen && item.badge && (
                <span className="sidebar-min-badge">{item.badge}</span>
              )}
            </button>
          )
        })}
      </nav>

      {/* Pie minimalista */}
      {isOpen && (
        <div className="sidebar-min-footer">
          <div className="sidebar-min-uq">
            <span className="sidebar-min-dot sidebar-min-dot-verde"></span>
            <div>
              <p className="sidebar-min-uq-text">Universidad del Quindío</p>
              <p className="sidebar-min-uq-sub">Ingeniería de Sistemas</p>
            </div>
          </div>
        </div>
      )}
    </aside>
  )
}

export default Sidebar
