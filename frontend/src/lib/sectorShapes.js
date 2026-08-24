// Geometria fixa por slug, extraída da planta estilizada de docs/prototipo-home.html.
// Slugs reais em uso vêm das migrations V6/V9/V11: SHOW usa PISTA, PLATEIA(_A/_B),
// CAMAROTE(_A/_B), ARQUIBANCADA; FILME usa PLATEIA e, quando a sessão reserva uma
// faixa de poltrona premium, VIP. Setor com slug fora deste mapa simplesmente não
// aparece no SVG — segue só na lista ao lado.

const SHOW_VIEWBOX = '0 0 400 320'

const SHOW_DECORATIVE = [
  { key: 'palco', shape: 'rect', x: 120, y: 16, width: 160, height: 30, rx: 3, className: 'stage-shape' },
]
const SHOW_DECORATIVE_LABELS = [{ x: 200, y: 35, texto: 'PALCO' }]

const SHOW_SHAPES = {
  PISTA: { shape: 'rect', x: 120, y: 62, width: 160, height: 120, rx: 4, labelX: 200, labelY: 118 },
  PLATEIA: { shape: 'rect', x: 120, y: 62, width: 160, height: 120, rx: 4, labelX: 200, labelY: 118 },
  PLATEIA_A: { shape: 'rect', x: 120, y: 62, width: 160, height: 64, rx: 4, labelX: 200, labelY: 96 },
  PLATEIA_B: { shape: 'rect', x: 120, y: 134, width: 160, height: 48, rx: 4, labelX: 200, labelY: 160 },
  CAMAROTE: { shape: 'rect', x: 302, y: 62, width: 72, height: 120, rx: 4, labelX: 338, labelY: 118 },
  CAMAROTE_A: { shape: 'rect', x: 302, y: 62, width: 72, height: 120, rx: 4, labelX: 338, labelY: 118 },
  CAMAROTE_B: { shape: 'rect', x: 26, y: 62, width: 72, height: 120, rx: 4, labelX: 62, labelY: 118 },
  ARQUIBANCADA: {
    shape: 'path',
    d: 'M 26 202 Q 200 244 374 202 L 374 276 Q 200 318 26 276 Z',
    labelX: 200,
    labelY: 261,
  },
}

// Planta de sala de cinema: tela em destaque no topo, plateia como auditório em leque
// (mais larga no fundo da sala, cantos suavizados, curvando levemente em direção à
// tela). Quando a sessão reserva uma faixa de poltrona premium, ela ocupa a metade de
// trás da mesma sala (FILME_SHAPES_DIVIDIDA); sessão com um setor só usa a sala
// inteira, seja PLATEIA ou VIP (FILME_SHAPES_UNICA) — nunca uma tira sozinha.

const FILME_VIEWBOX = '0 0 400 240'

const FILME_DECORATIVE = [
  { key: 'tela-glow', shape: 'rect', x: 60, y: 10, width: 280, height: 30, rx: 15, className: 'screen-glow' },
  { key: 'tela', shape: 'rect', x: 100, y: 18, width: 200, height: 10, rx: 5, className: 'stage-shape' },
]
const FILME_DECORATIVE_LABELS = [{ x: 200, y: 46, texto: 'TELA' }]

const SALA_INTEIRA = {
  shape: 'path',
  d: 'M 160 60 Q 200 48 240 60 C 268 68 300 95 320 135 Q 336 168 322 192 Q 300 210 250 206 L 150 206 Q 100 210 78 192 Q 64 168 80 135 C 100 95 132 68 160 60 Z',
  labelX: 200,
  labelY: 136,
}

const FILME_SHAPES_UNICA = { PLATEIA: SALA_INTEIRA, VIP: SALA_INTEIRA }

const FILME_SHAPES_DIVIDIDA = {
  PLATEIA: {
    shape: 'path',
    d: 'M 160 60 Q 200 48 240 60 C 268 68 300 95 315 150 Q 200 158 85 150 C 100 95 132 68 160 60 Z',
    labelX: 200,
    labelY: 108,
  },
  VIP: {
    shape: 'path',
    d: 'M 85 150 Q 200 158 315 150 Q 336 178 322 195 Q 300 212 250 208 L 150 208 Q 100 212 78 195 Q 64 178 85 150 Z',
    labelX: 200,
    labelY: 176,
  },
}

export function geometriaPorTipo(tipo, setores = []) {
  if (tipo === 'FILME') {
    const slugs = new Set(setores.map(s => s.slug))
    const dividida = slugs.has('PLATEIA') && slugs.has('VIP')
    return {
      viewBox: FILME_VIEWBOX,
      decorativos: FILME_DECORATIVE,
      rotulosDecorativos: FILME_DECORATIVE_LABELS,
      shapes: dividida ? FILME_SHAPES_DIVIDIDA : FILME_SHAPES_UNICA,
    }
  }
  return { viewBox: SHOW_VIEWBOX, decorativos: SHOW_DECORATIVE, rotulosDecorativos: SHOW_DECORATIVE_LABELS, shapes: SHOW_SHAPES }
}
