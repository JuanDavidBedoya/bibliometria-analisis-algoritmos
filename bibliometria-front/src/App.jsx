/**
 * App.jsx - Componente raíz con layout principal en tonos verde claro.
 */
import { useState } from 'react'
import Sidebar          from './components/Sidebar'
import InformacionPage  from './pages/InformacionPage'
import CountingSortPage from './pages/CountingSortPage'
import BucketSortPage   from './pages/BucketSortPage'
import RadixSortPage    from './pages/RadixSortPage'
import './App.css'

function App() {
  const [ventanaActiva, setVentanaActiva]   = useState('informacion')
  const [sidebarAbierto, setSidebarAbierto] = useState(true)

  return (
    <div className="layout-principal">
      <Sidebar
        ventanaActiva={ventanaActiva}
        setVentanaActiva={setVentanaActiva}
        totalArticulos={20}
        isOpen={sidebarAbierto}
        setIsOpen={setSidebarAbierto}
      />

      <div className={`area-principal ${sidebarAbierto ? '' : 'sidebar-cerrado'}`}>
        <main className="contenido-vista">
          {ventanaActiva === 'informacion'  && <InformacionPage />}
          {ventanaActiva === 'countingsort' && <CountingSortPage />}
          {ventanaActiva === 'bucketsort'   && <BucketSortPage />}
          {ventanaActiva === 'radixsort'    && <RadixSortPage />}
        </main>
      </div>
    </div>
  )
}

export default App
