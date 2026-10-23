/**
 * components/Header.jsx
 *
 * Encabezado de la aplicación con el título y la descripción del proyecto.
 * Componente puramente visual, sin estado ni lógica.
 */

function Header() {
  return (
    <header className="header">
      <h1>📚 Análisis Bibliométrico</h1>
      <p>Ordenamiento de términos con <strong>Bucket Sort</strong></p>
      <p className="subtitulo">Algoritmos y Complejidad · Universidad del Quindío</p>
    </header>
  )
}

export default Header
