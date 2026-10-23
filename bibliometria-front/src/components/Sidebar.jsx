/**
 * components/Sidebar.jsx
 * Menú lateral colapsable con paleta de colores verde.
 */
import { useEffect } from 'react'

function Sidebar({ ventanaActiva, setVentanaActiva, totalArticulos = 20, isOpen, setIsOpen }) {
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
      badge: `${totalArticulos}`
    },
    {
      id: 'countingsort',
      titulo: 'Counting Sort',
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
      titulo: 'Bucket Sort',
      subtitulo: 'Ranking y métricas O(n)',
      icono: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M3 6h18" />
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
          <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
          <line x1="10" y1="11" x2="10" y2="17" />
          <line x1="14" y1="11" x2="14" y2="17" />
        </svg>
      ),
      badge: 'O(n)'
    }
  ]

  // Persist sidebar state
  useEffect(() => {
    localStorage.setItem('sidebarAbierto', String(isOpen))
  }, [isOpen])

  return (
    <aside className={`sidebar ${isOpen ? 'sidebar-expandido' : 'sidebar-colapsado'}`}>
      {/* Botón hamburguesa toggle */}
      <button
        className="sidebar-toggle-btn"
        onClick={() => setIsOpen(!isOpen)}
        title={isOpen ? 'Colapsar menú' : 'Expandir menú'}
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
          <line x1="3" y1="6"  x2="21" y2="6"  />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="3" y1="18" x2="21" y2="18" />
        </svg>
      </button>

      {/* Brand */}
      {isOpen && (
        <div className="sidebar-brand">
          <div className="brand-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
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
      )}

      {isOpen && <div className="sidebar-section-title">MENÚ PRINCIPAL</div>}

      {/* Navegación */}
      <nav className="sidebar-nav">
        {items.map(item => {
          const activo = ventanaActiva === item.id
          return (
            <button
              key={item.id}
              className={`nav-btn ${activo ? 'activo' : ''}`}
              onClick={() => setVentanaActiva(item.id)}
              title={!isOpen ? item.titulo : undefined}
            >
              <div className="nav-btn-icon">{item.icono}</div>
              {isOpen && (
                <div className="nav-btn-text">
                  <span className="nav-btn-title">{item.titulo}</span>
                  <span className="nav-btn-sub">{item.subtitulo}</span>
                </div>
              )}
              {isOpen && item.badge && (
                <span className="nav-btn-badge">{item.badge}</span>
              )}
            </button>
          )
        })}
      </nav>

      {/* Footer */}
      {isOpen && (
        <div className="sidebar-footer">
          <div className="footer-card">
            <div className="footer-indicator"></div>
            <div>
              <strong>Universidad del Quindío</strong>
              <p>Ingeniería de Sistemas · 2026</p>
            </div>
          </div>
        </div>
      )}
    </aside>
  )
}

export default Sidebar
