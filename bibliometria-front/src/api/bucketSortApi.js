/**
 * api/bucketSortApi.js
 *
 * Módulo para interactuar con el backend Spring Boot.
 * Permite consultar el endpoint de Bucket Sort y obtener la lista de artículos del estudio.
 */

import { articulosDefecto } from '../data/articulosDefecto'

const ENDPOINT_BUCKET_SORT = '/api/bucket-sort'
const ENDPOINT_ARTICULOS = '/api/articulos'

/**
 * Llama al endpoint GET /api/bucket-sort y retorna los datos del ordenamiento.
 *
 * @param {number}  topK              - Cuántos términos incluir en el ranking.
 * @param {boolean} filtrarStopwords  - Si se deben excluir palabras vacías.
 * @returns {Promise<Object>}         - JSON con la respuesta del servidor.
 */
export async function obtenerResultadoBucketSort(topK, filtrarStopwords) {
  const url = `${ENDPOINT_BUCKET_SORT}?topK=${topK}&filtrarStopwords=${filtrarStopwords}`
  const respuesta = await fetch(url)

  if (!respuesta.ok) {
    throw new Error(`Error en el servidor (${respuesta.status}). Verifique que el backend esté activo.`)
  }

  return respuesta.json()
}

/**
 * Obtiene la lista de artículos científicos utilizados en el estudio.
 * Si el servidor está activo, los consulta desde /api/articulos;
 * de lo contrario, utiliza los 20 artículos precargados del corpus.
 *
 * @returns {Promise<Array>} - Lista de objetos de artículos.
 */
export async function obtenerArticulos() {
  try {
    const respuesta = await fetch(ENDPOINT_ARTICULOS)
    if (respuesta.ok) {
      const datos = await respuesta.json()
      if (Array.isArray(datos) && datos.length > 0) {
        return datos
      }
    }
  } catch {
    // Si el backend aún no se ha iniciado o hay error de red, usar datos del corpus precargado
  }
  return articulosDefecto
}
