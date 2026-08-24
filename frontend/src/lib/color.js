// O protótipo escolhe a cor de cada poster à mão (--p: "62 90 190"). Sem
// designer escolhendo por evento real, deriva um triplo RGB determinístico do
// título — mesmo evento sempre cai na mesma cor, sem inventar paleta nova (o
// gradiente/opacidade em si é a mesma mecânica --tint-a/--tint-b do protótipo).
export function corDoTexto(texto) {
  let hash = 0
  for (let i = 0; i < texto.length; i++) {
    hash = (hash * 31 + texto.charCodeAt(i)) >>> 0
  }
  const r = 60 + (hash % 160)
  const g = 60 + ((hash >> 8) % 160)
  const b = 60 + ((hash >> 16) % 160)
  return `${r} ${g} ${b}`
}

// Rampa sequencial de ocupação do protótipo: um matiz só, claro -> escuro.
export function passoOcupacao(razao) {
  if (razao >= 1) return 'out'
  if (razao >= 0.95) return 4
  if (razao >= 0.8) return 3
  if (razao >= 0.5) return 2
  return 1
}

export function corBarraOcupacao(razao) {
  const passo = passoOcupacao(razao)
  return passo === 'out' ? 'var(--occ-out)' : `var(--occ-${passo})`
}

// Mesma rampa, mas esgotado vira hachura (ver #hatch em SectorMap) em vez de cor sólida —
// cor nunca é o único sinal de "esgotado".
export function corMapaOcupacao(razao) {
  const passo = passoOcupacao(razao)
  return passo === 'out' ? 'url(#hatch)' : `var(--occ-${passo})`
}
