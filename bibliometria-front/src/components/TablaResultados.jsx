/**
 * components/TablaResultados.jsx
 *
 * Tabla que muestra el ranking de términos más frecuentes ordenados por Bucket Sort.
 * Incluye una barra de progreso visual proporcional a la frecuencia máxima.
 *
 * Props:
 *   terminos {Array<{termino: string, frecuencia: number}>} - Lista ordenada de términos.
 */

function TablaResultados({ terminos }) {
  // Frecuencia del término más alto (siempre el primero, ya viene ordenado)
  const maxFrecuencia = terminos[0]?.frecuencia ?? 1

  return (
    <table className="tabla">
      <thead>
        <tr>
          <th>#</th>
          <th>Término</th>
          <th>Frecuencia</th>
          <th>Barra</th>
        </tr>
      </thead>
      <tbody>
        {terminos.map((item, idx) => {
          const porcentaje = Math.round((item.frecuencia / maxFrecuencia) * 100)
          return (
            <tr key={item.termino}>
              <td className="rank">#{idx + 1}</td>
              <td className="termino">{item.termino}</td>
              <td className="freq">{item.frecuencia}</td>
              <td className="barra-celda">
                <div className="barra" style={{ width: `${porcentaje}%` }} />
              </td>
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}

export default TablaResultados
