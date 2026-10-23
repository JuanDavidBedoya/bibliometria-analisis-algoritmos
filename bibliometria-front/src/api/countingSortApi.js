/**
 * api/countingSortApi.js
 *
 * Módulo para interactuar con el endpoint de Counting Sort del backend Spring Boot.
 * Incluye fallback de ejecución local directa de Counting Sort sobre el corpus
 * si el servidor backend no se encuentra activo.
 */

import { articulosDefecto } from '../data/articulosDefecto'

const ENDPOINT_COUNTING_SORT = '/api/counting-sort'

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
 * Consulta el endpoint /api/counting-sort del backend Spring Boot.
 * Si el servidor está apagado o falla la red, ejecuta el algoritmo Counting Sort
 * de manera local sobre el corpus para garantizar disponibilidad ininterrumpida.
 *
 * @param {number}  topK              - Cantidad de términos a incluir en el ranking.
 * @param {boolean} filtrarStopwords  - Exclusión de palabras funcionales.
 * @returns {Promise<Object>}         - Resultado formal con métricas y lista ordenada.
 */
export async function obtenerResultadoCountingSort(topK, filtrarStopwords) {
  try {
    const url = `${ENDPOINT_COUNTING_SORT}?topK=${topK}&filtrarStopwords=${filtrarStopwords}`
    const respuesta = await fetch(url)

    if (respuesta.ok) {
      const json = await respuesta.json()
      if (json && json.terminos) {
        return json
      }
    }
  } catch {
    // Si el backend no está disponible, se procede con la ejecución local
  }

  // Fallback: Ejecución del algoritmo Counting Sort en el cliente
  return ejecutarCountingSortLocal(topK, filtrarStopwords)
}

/**
 * Implementación explícita del algoritmo Counting Sort en JavaScript
 * siguiendo las 4 fases asintóticas O(n + k).
 */
function ejecutarCountingSortLocal(topK, filtrarStopwords) {
  const inicioNano = performance.now()

  // 1. Extracción y conteo del vocabulario
  const mapaFrecuencias = new Map()

  for (const art of articulosDefecto) {
    const texto = art.resumenOriginal || ''
    const tokens = texto.toLowerCase().match(/\b[a-z]{2,}\b/g) || []

    for (const token of tokens) {
      if (filtrarStopwords && STOPWORDS_INGLES.has(token)) {
        continue
      }
      mapaFrecuencias.set(token, (mapaFrecuencias.get(token) || 0) + 1)
    }
  }

  const lista = []
  for (const [termino, frecuencia] of mapaFrecuencias.entries()) {
    lista.push({ termino, frecuencia })
  }

  const n = lista.length
  if (n === 0) {
    return {
      algoritmo: 'Counting Sort',
      terminos: [],
      tiempoMs: 0,
      totalElementos: 0,
      rangoK: 0,
      frecuenciaMinima: 0,
      frecuenciaMaxima: 0,
      topK,
      stopwordsFiltradas: filtrarStopwords,
      totalArticulos: articulosDefecto.length
    }
  }

  // FASE 1: Determinación de rango [min, max] en O(n)
  let minFrecuencia = lista[0].frecuencia
  let maxFrecuencia = lista[0].frecuencia

  for (let i = 1; i < n; i++) {
    const f = lista[i].frecuencia
    if (f < minFrecuencia) minFrecuencia = f
    if (f > maxFrecuencia) maxFrecuencia = f
  }

  const k = maxFrecuencia - minFrecuencia + 1

  // FASE 2: Histograma de frecuencias en O(n)
  const conteo = new Int32Array(k)
  for (let i = 0; i < n; i++) {
    conteo[lista[i].frecuencia - minFrecuencia]++
  }

  // FASE 3: Sumas acumuladas (Prefix Sums) descendentes en O(k)
  // Se acumula desde la frecuencia más alta hacia la más baja
  for (let i = k - 2; i >= 0; i--) {
    conteo[i] += conteo[i + 1]
  }

  // FASE 4: Ubicación estable en arreglo de salida en O(n)
  const salida = new Array(n)
  for (let i = n - 1; i >= 0; i--) {
    const actual = lista[i]
    const clave = actual.frecuencia - minFrecuencia
    const pos = conteo[clave] - 1
    salida[pos] = actual
    conteo[clave]--
  }

  const finNano = performance.now()
  const duracionMs = Math.max(0.005, finNano - inicioNano)

  const limite = Math.min(topK, n)
  const topTerminos = salida.slice(0, limite)

  return {
    algoritmo: 'Counting Sort',
    terminos: topTerminos,
    tiempoMs: duracionMs,
    totalElementos: n,
    rangoK: k,
    frecuenciaMinima: minFrecuencia,
    frecuenciaMaxima: maxFrecuencia,
    topK,
    stopwordsFiltradas: filtrarStopwords,
    totalArticulos: articulosDefecto.length
  }
}
