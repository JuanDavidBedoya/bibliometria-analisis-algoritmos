/**
 * pages/InformacionPage.jsx
 *
 * Ventana 1: Información general del estudio bibliométrico.
 * Muestra los títulos y metadatos de todos los artículos científicos utilizados.
 */

import { useState, useEffect } from 'react'
import { obtenerArticulos } from '../api/bucketSortApi'

function InformacionPage() {
  const [articulos, setArticulos] = useState([])
  const [busqueda, setBusqueda] = useState('')
  const [expandidoId, setExpandidoId] = useState(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    async function cargar() {
      setCargando(true)
      const datos = await obtenerArticulos()
      setArticulos(datos)
      setCargando(false)
    }
    cargar()
  }, [])

  const filtrados = articulos.filter(a => {
    const termino = busqueda.toLowerCase().trim()
    if (!termino) return true
    const titulo = (a.titulo || '').toLowerCase()
    const archivo = (a.archivo || '').toLowerCase()
    const autores = (a.autores || []).join(' ').toLowerCase()
    return titulo.includes(termino) || autores.includes(termino) || archivo.includes(termino)
  })

  const totalPalabrasCorpus = articulos.reduce((acc, a) => acc + (a.totalPalabras || 0), 0)

  return (
    <div className="pagina-contenedor">
      {/* Encabezado de la ventana */}
      <header className="pagina-header">
        <div className="header-badge">VENTANA 1 · INFORMACIÓN</div>
        <h1>Artículos Científicos del Estudio</h1>
        <p className="pagina-descripcion">
          Corpus documental de 20 artículos científicos en formato PDF centrados en el impacto
          y aplicación de la <strong>Inteligencia Artificial en la Educación (AIEd)</strong>.
          Estos documentos fueron preprocesados y analizados para la aplicación de los algoritmos de ordenamiento.
        </p>
      </header>

      {/* Banner de estadísticas rápidas */}
      <div className="stats-banner">
        <div className="stat-card">
          <span className="stat-numero">{articulos.length}</span>
          <span className="stat-etiqueta">Artículos Seleccionados</span>
        </div>
        <div className="stat-card">
          <span className="stat-numero">100%</span>
          <span className="stat-etiqueta">Preprocesados a JSON</span>
        </div>
        <div className="stat-card">
          <span className="stat-numero">AIEd</span>
          <span className="stat-etiqueta">Dominio Temático</span>
        </div>
        <div className="stat-card">
          <span className="stat-numero">{totalPalabrasCorpus.toLocaleString()}</span>
          <span className="stat-etiqueta">Palabras Totales en Abstracts</span>
        </div>
      </div>

      {/* Barra de búsqueda y conteo */}
      <div className="busqueda-barra">
        <div className="busqueda-input-wrapper">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder="Buscar por título, autor o nombre de archivo..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
          {busqueda && (
            <button className="limpiar-busqueda" onClick={() => setBusqueda('')}>
              ✕
            </button>
          )}
        </div>
        <div className="conteo-articulos">
          Mostrando <strong>{filtrados.length}</strong> de {articulos.length} artículos
        </div>
      </div>

      {/* Listado de artículos */}
      {cargando ? (
        <div className="cargando-estado">
          <div className="spinner"></div>
          <p>Cargando artículos del corpus documental...</p>
        </div>
      ) : filtrados.length === 0 ? (
        <div className="sin-resultados">
          <p>No se encontraron artículos con el término <strong>"{busqueda}"</strong>.</p>
          <button className="btn-secundario" onClick={() => setBusqueda('')}>Limpiar filtro</button>
        </div>
      ) : (
        <div className="lista-articulos">
          {filtrados.map((art) => {
            const estaExpandido = expandidoId === art.id
            return (
              <article key={art.id} className={`articulo-tarjeta ${estaExpandido ? 'expandido' : ''}`}>
                <div className="articulo-header">
                  <div className="articulo-id-badge">#{art.id}</div>
                  <div className="articulo-meta-top">
                    <span className="articulo-archivo" title={art.archivo}>
                      📄 {art.archivo}
                    </span>
                    {art.totalPalabras > 0 && (
                      <span className="articulo-tokens-badge">
                        {art.totalPalabras} palabras
                      </span>
                    )}
                  </div>
                </div>

                <h2 className="articulo-titulo">{art.titulo}</h2>

                {/* Autores */}
                <div className="articulo-autores">
                  <span className="autores-icono">👥 Autores:</span>
                  <div className="autores-tags">
                    {(art.autores || []).map((autor, idx) => (
                      <span key={idx} className="autor-tag">{autor}</span>
                    ))}
                  </div>
                </div>

                {/* Resumen desplegable */}
                {art.resumenOriginal && (
                  <div className="articulo-abstract-seccion">
                    <button
                      className="btn-toggle-abstract"
                      onClick={() => setExpandidoId(estaExpandido ? null : art.id)}
                    >
                      {estaExpandido ? '▲ Ocultar Resumen Original (Abstract)' : '▼ Ver Resumen Original (Abstract)'}
                    </button>
                    {estaExpandido && (
                      <div className="abstract-contenido">
                        <p>{art.resumenOriginal}</p>
                      </div>
                    )}
                  </div>
                )}
              </article>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default InformacionPage
