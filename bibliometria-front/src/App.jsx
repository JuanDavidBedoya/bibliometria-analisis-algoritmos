/**
 * App.jsx
 *
 * Componente raíz con el layout principal:
 *   - Sidebar lateral izquierdo con navegación en tonos morados
 *   - Área de contenido principal con cambio entre:
 *       1. "informacion"  - Información y títulos de los artículos del estudio
 *       2. "countingsort" - Ejecución de Counting Sort y métricas
 *       3. "bucketsort"   - Ejecución de Bucket Sort y métricas
 */

import { useState } from 'react'
import Sidebar         from './components/Sidebar'
import Header          from './components/Header'
import InformacionPage from './pages/InformacionPage'
import CountingSortPage from './pages/CountingSortPage'
import BucketSortPage  from './pages/BucketSortPage'
import './App.css'

function App() {
  const [ventanaActiva, setVentanaActiva] = useState('informacion')

  return (
    <div className="layout-principal">
      {/* Menú lateral izquierdo */}
      <Sidebar
        ventanaActiva={ventanaActiva}
        setVentanaActiva={setVentanaActiva}
        totalArticulos={20}
      />

      {/* Contenido principal */}
      <div className="area-principal">
        <Header ventanaActiva={ventanaActiva} />

        <main className="contenido-vista">
          {ventanaActiva === 'informacion' && <InformacionPage />}
          {ventanaActiva === 'countingsort' && <CountingSortPage />}
          {ventanaActiva === 'bucketsort' && <BucketSortPage />}
        </main>
      </div>
    </div>
  )
}

export default App
