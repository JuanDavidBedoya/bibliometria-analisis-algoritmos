/**
 * api/bucketSortApi.js
 *
 * Módulo para interactuar con el backend Spring Boot.
 * Permite consultar el endpoint de Bucket Sort y obtener la lista de artículos del estudio.
 * Incluye fallback de ejecución local si el servidor no está activo.
 */

import { articulosDefecto } from '../data/articulosDefecto'

const ENDPOINT_BUCKET_SORT = '/api/bucket-sort'
const ENDPOINT_ARTICULOS = '/api/articulos'

const STOPWORDS_INGLES = new Set([
  'a', 'about', 'above', 'after', 'again', 'against', 'all', 'also', 'am', 'an', 'and', 'any', 'are',
  'as', 'at', 'be', 'because', 'been', 'before', 'being', 'below', 'between', 'both', 'but', 'by',
  'can', 'could', 'did', 'do', 'does', 'doing', 'down', 'during', 'each', 'e', 'g', 'few', 'for', 'from',
  'further', 'had', 'has', 'have', 'having', 'he', 'her', 'here', 'hers', 'herself', 'him', 'himself',
  'his', 'how', 'i', 'if', 'in', 'into', 'is', 'it', 'its', 'itself', 'just', 'me', 'more', 'most',
  'my', 'myself', 'no', 'nor', 'not', 'now', 'of', 'off', 'on', 'once', 'only', 'or', 'other',
  'our', 'ours', 'ourselves', 'out', 'over', 'own', 's', 'same', 'she', 'should', 'so', 'some',
  'such', 't', 'than', 'that', 'the', 'their', 'theirs', 'them', 'themselves', 'then', 'there',
  'these', 'they', 'this', 'those', 'through', 'to', 'too', 'under', 'until', 'up', 'very', 'was',
  'we', 'were', 'what', 'when', 'where', 'which', 'while', 'who', 'whom', 'why', 'will', 'with',
  'would', 'you', 'your', 'yours', 'yourself', 'yourselves'
])

/**
 * Llama al endpoint GET /api/bucket-sort y retorna los datos del ordenamiento.
 *
 * @param {number}  topK              - Cuántos términos incluir en el ranking.
 * @param {boolean} filtrarStopwords  - Si se deben excluir palabras vacías.
 * @returns {Promise<Object>}         - JSON con la respuesta del servidor.
 */
export async function obtenerResultadoBucketSort(topK, filtrarStopwords) {
  try {
    const url = `${ENDPOINT_BUCKET_SORT}?topK=${topK}&filtrarStopwords=${filtrarStopwords}`
    const respuesta = await fetch(url)

    if (respuesta.ok) {
      const json = await respuesta.json()
      if (json && json.terminos) {
        return json
      }
    }
  } catch {
    // Si el backend no está disponible, ejecutar localmente
  }

  return ejecutarBucketSortLocal(topK, filtrarStopwords)
}

function ejecutarBucketSortLocal(topK, filtrarStopwords) {
  const inicioNano = performance.now()

  const mapa = new Map()
  for (const art of articulosDefecto) {
    const texto = art.resumenOriginal || ''
    const tokens = texto.toLowerCase().match(/\b[a-z]{2,}\b/g) || []

    for (const token of tokens) {
      if (filtrarStopwords && STOPWORDS_INGLES.has(token)) continue
      mapa.set(token, (mapa.get(token) || 0) + 1)
    }
  }

  const lista = []
  for (const [termino, frecuencia] of mapa.entries()) {
    lista.push({ termino, frecuencia })
  }

  const n = lista.length
  if (n === 0) {
    return {
      algoritmo: 'Bucket Sort',
      terminos: [],
      tiempoMs: 0,
      totalElementos: 0,
      numeroCubetas: 0,
      frecuenciaMinima: 0,
      frecuenciaMaxima: 0,
      topK,
      stopwordsFiltradas: filtrarStopwords,
      totalArticulos: articulosDefecto.length
    }
  }

  let minF = lista[0].frecuencia
  let maxF = lista[0].frecuencia
  for (let i = 1; i < n; i++) {
    if (lista[i].frecuencia < minF) minF = lista[i].frecuencia
    if (lista[i].frecuencia > maxF) maxF = lista[i].frecuencia
  }

  let numCubetas = Math.max(10, Math.ceil(Math.sqrt(n)))
  const rango = maxF - minF + 1
  if (numCubetas > rango) numCubetas = rango

  const cubetas = Array.from({ length: numCubetas }, () => [])

  for (let i = 0; i < n; i++) {
    const elem = lista[i]
    let idx = Math.floor(((elem.frecuencia - minF) * (numCubetas - 1)) / (maxF - minF || 1))
    if (idx < 0) idx = 0
    if (idx >= numCubetas) idx = numCubetas - 1
    cubetas[idx].push(elem)
  }

  for (let i = 0; i < numCubetas; i++) {
    cubetas[i].sort((a, b) => b.frecuencia - a.frecuencia || a.termino.localeCompare(b.termino))
  }

  const salida = []
  for (let i = numCubetas - 1; i >= 0; i--) {
    salida.push(...cubetas[i])
  }

  const duracionMs = Math.max(0.01, performance.now() - inicioNano)
  const topTerminos = salida.slice(0, Math.min(topK, n))

  return {
    algoritmo: 'Bucket Sort',
    terminos: topTerminos,
    tiempoMs: duracionMs,
    totalElementos: n,
    numeroCubetas: numCubetas,
    frecuenciaMinima: minF,
    frecuenciaMaxima: maxF,
    topK,
    stopwordsFiltradas: filtrarStopwords,
    totalArticulos: articulosDefecto.length
  }
}

/**
 * Obtiene la lista de artículos científicos utilizados en el estudio.
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
    // Si el backend no responde, usar corpus precargado
  }
  return articulosDefecto
}
