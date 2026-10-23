/**
 * api/countingSortApi.js
 *
 * Módulo para interactuar con el backend Spring Boot.
 * Permite consultar el endpoint de Counting Sort (/api/counting-sort).
 */

const ENDPOINT_COUNTING_SORT = '/api/counting-sort'

/**
 * Llama al endpoint GET /api/counting-sort y retorna los datos del ordenamiento.
 *
 * @param {number}  topK              - Cuántos términos incluir en el ranking.
 * @param {boolean} filtrarStopwords  - Si se deben excluir palabras vacías.
 * @returns {Promise<Object>}         - JSON con la respuesta del servidor.
 */
export async function obtenerResultadoCountingSort(topK, filtrarStopwords) {
  const url = `${ENDPOINT_COUNTING_SORT}?topK=${topK}&filtrarStopwords=${filtrarStopwords}`
  const respuesta = await fetch(url)

  if (!respuesta.ok) {
    throw new Error(`Error en el servidor (${respuesta.status}). Verifique que el backend esté activo.`)
  }

  return respuesta.json()
}
