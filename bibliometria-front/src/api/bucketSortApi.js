/**
 * api/bucketSortApi.js
 *
 * Módulo responsable de toda la comunicación con el backend Spring Boot.
 * Centraliza la URL y los parámetros del endpoint de Bucket Sort.
 *
 * Vite hace proxy de /api → http://localhost:8080 (configurado en vite.config.js)
 * por lo que no necesitamos escribir la URL completa del servidor.
 */

const ENDPOINT = '/api/bucket-sort'

/**
 * Llama al endpoint GET /api/bucket-sort y retorna los datos del ordenamiento.
 *
 * @param {number}  topK              - Cuántos términos incluir en el ranking.
 * @param {boolean} filtrarStopwords  - Si se deben excluir palabras vacías.
 * @returns {Promise<Object>}         - JSON con la respuesta del servidor.
 * @throws {Error}                    - Si la respuesta HTTP no es exitosa.
 */
export async function obtenerResultadoBucketSort(topK, filtrarStopwords) {
  const url = `${ENDPOINT}?topK=${topK}&filtrarStopwords=${filtrarStopwords}`
  const respuesta = await fetch(url)

  if (!respuesta.ok) {
    throw new Error(`Error del servidor: ${respuesta.status}`)
  }

  return respuesta.json()
}
