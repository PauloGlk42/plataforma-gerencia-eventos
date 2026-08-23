// GET /api/eventos espera `de`/`ate` como instante ISO completo (OffsetDateTime),
// não uma data solta — os inputs <input type="date"> só dão "AAAA-MM-DD". Constrói
// o instante a partir da meia-noite/fim do dia no fuso do navegador.
function inicioDoDiaISO(dataStr) {
  if (!dataStr) return null
  return new Date(`${dataStr}T00:00:00`).toISOString()
}

function fimDoDiaISO(dataStr) {
  if (!dataStr) return null
  return new Date(`${dataStr}T23:59:59`).toISOString()
}

export function montarQueryEventos(filtros, page, size) {
  const params = new URLSearchParams()

  if (filtros.q?.trim()) params.set('q', filtros.q.trim())
  if (filtros.cidade?.trim()) params.set('cidade', filtros.cidade.trim())
  if (filtros.tipo) params.set('tipo', filtros.tipo)

  const de = inicioDoDiaISO(filtros.de)
  if (de) params.set('de', de)
  const ate = fimDoDiaISO(filtros.ate)
  if (ate) params.set('ate', ate)

  if (filtros.precoMin !== '' && filtros.precoMin != null) params.set('precoMin', filtros.precoMin)
  if (filtros.precoMax !== '' && filtros.precoMax != null) params.set('precoMax', filtros.precoMax)

  params.set('page', String(page))
  params.set('size', String(size))

  return params.toString()
}

export const FILTROS_VAZIOS = { q: '', cidade: '', de: '', ate: '', precoMin: '', precoMax: '', tipo: '' }
