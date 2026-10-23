/**
 * App.jsx
 *
 * Componente raíz de la aplicación.
 * Su única responsabilidad es componer el layout global:
 *   - Header: título y datos del proyecto
 *   - BucketSortPage: toda la lógica y visualización del algoritmo
 *
 * Si en el futuro se agregan más páginas (Counting Sort, Radix Sort, etc.),
 * aquí se añadirían las rutas con React Router.
 */

import Header         from './components/Header'
import BucketSortPage from './pages/BucketSortPage'
import './App.css'

function App() {
  return (
    <>
      <Header />
      <main>
        <BucketSortPage />
      </main>
    </>
  )
}

export default App
