const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']

export function formatarDataHora(isoString) {
  const d = new Date(isoString)
  const dia = String(d.getDate()).padStart(2, '0')
  const mes = MESES[d.getMonth()]
  const hora = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${dia} ${mes} · ${hora}h${min}`
}

export function formatarPreco(valor) {
  return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}
