/**
 * components/MensajeError.jsx
 *
 * Muestra un bloque de error cuando el backend no responde
 * o devuelve un código HTTP de error.
 *
 * Props:
 *   mensaje {string} - Texto del error a mostrar.
 */

function MensajeError({ mensaje }) {
  return (
    <section className="error">
      <p>⚠️ {mensaje}</p>
      <p className="hint">¿Está el servidor Spring Boot corriendo en el puerto 8080?</p>
    </section>
  )
}

export default MensajeError
