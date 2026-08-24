// Geometria fixa por slug, extraída da planta estilizada de docs/prototipo-home.html.
// Slugs reais em uso vêm das migrations V6/V9: SHOW usa PISTA, PLATEIA(_A/_B),
// CAMAROTE(_A/_B); FILME usa SALA, SALA_1/SALA_2, SALA_VIP. Setor com slug fora
// deste mapa simplesmente não aparece no SVG — segue só na lista ao lado.

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

const FILME_VIEWBOX = '0 0 400 240'

const FILME_DECORATIVE = [
  { key: 'tela', shape: 'rect', x: 80, y: 16, width: 240, height: 16, rx: 2, className: 'stage-shape' },
]
const FILME_DECORATIVE_LABELS = [{ x: 200, y: 29, texto: 'TELA' }]

const FILME_SHAPES = {
  SALA: { shape: 'rect', x: 60, y: 56, width: 280, height: 150, rx: 4, labelX: 200, labelY: 128 },
  SALA_VIP: { shape: 'rect', x: 60, y: 56, width: 280, height: 150, rx: 4, labelX: 200, labelY: 128 },
  PLATEIA: { shape: 'rect', x: 60, y: 56, width: 280, height: 150, rx: 4, labelX: 200, labelY: 128 },
  SALA_1: { shape: 'rect', x: 40, y: 56, width: 155, height: 150, rx: 4, labelX: 117, labelY: 128 },
  SALA_2: { shape: 'rect', x: 205, y: 56, width: 155, height: 150, rx: 4, labelX: 282, labelY: 128 },
}

export function geometriaPorTipo(tipo) {
  if (tipo === 'FILME') {
    return { viewBox: FILME_VIEWBOX, decorativos: FILME_DECORATIVE, rotulosDecorativos: FILME_DECORATIVE_LABELS, shapes: FILME_SHAPES }
  }
  return { viewBox: SHOW_VIEWBOX, decorativos: SHOW_DECORATIVE, rotulosDecorativos: SHOW_DECORATIVE_LABELS, shapes: SHOW_SHAPES }
}
