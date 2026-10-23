/**
 * components/Header.jsx
 *
 * Barra superior (topbar) dentro del área de contenido principal.
 */

function Header({ ventanaActiva }) {
  return (
    <header className="topbar">
      <div className="topbar-breadcrumb">
        <span className="breadcrumb-root">Sistema Bibliométrico</span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-active">
          {ventanaActiva === 'informacion' ? 'Información del Corpus' : 'Ejecución Bucket Sort'}
        </span>
      </div>

      <div className="topbar-actions">
        <span className="topbar-badge-corpus">
          <span className="punto-verde"></span>
          20 Artículos Indexados
        </span>
        <span className="topbar-universidad">
          Universidad del Quindío
        </span>
      </div>
    </header>
  )
}

export default Header
