import { geometriaPorTipo } from '../../lib/sectorShapes'
import { corMapaOcupacao, passoOcupacao } from '../../lib/color'
import './SectorMap.css'

export default function SectorMap({ tipo, setores, selecionado, onSelecionar }) {
  const { viewBox, decorativos, rotulosDecorativos, shapes } = geometriaPorTipo(tipo)
  const setoresComForma = setores.filter(s => shapes[s.slug])

  if (setoresComForma.length === 0) return null

  return (
    <svg className="venue" viewBox={viewBox} role="img" aria-label="Mapa de setores">
      <defs>
        <pattern id="hatch" width="7" height="7" patternTransform="rotate(45)" patternUnits="userSpaceOnUse">
          <rect width="7" height="7" fill="var(--occ-out)" />
          <line x1="0" y1="0" x2="0" y2="7" stroke="var(--surface)" strokeWidth="3" />
        </pattern>
      </defs>

      {decorativos.map(d => (
        d.shape === 'rect'
          ? <rect key={d.key} className={d.className} x={d.x} y={d.y} width={d.width} height={d.height} rx={d.rx} />
          : null
      ))}
      {rotulosDecorativos.map(r => <text key={r.texto} x={r.x} y={r.y}>{r.texto}</text>)}

      {setoresComForma.map(setor => {
        const forma = shapes[setor.slug]
        const razao = setor.ocupados / setor.capacidade
        const livres = setor.capacidade - setor.ocupados
        const esgotado = livres <= 0
        const rotuloClaro = passoOcupacao(razao) === 4
        const pressionado = selecionado === setor.slug
        const props = {
          'data-sector': setor.slug,
          fill: corMapaOcupacao(razao),
          stroke: 'var(--rule-2)',
          strokeWidth: 1,
          role: 'button',
          tabIndex: esgotado ? -1 : 0,
          'aria-pressed': pressionado,
          'aria-disabled': esgotado,
          'aria-label': `${setor.nome}, ${esgotado ? 'esgotado' : `${livres} lugares livres`}`,
          onClick: () => !esgotado && onSelecionar(setor),
          onKeyDown: ev => {
            if (!esgotado && (ev.key === 'Enter' || ev.key === ' ')) {
              ev.preventDefault()
              onSelecionar(setor)
            }
          },
        }

        return (
          <g key={setor.id}>
            {forma.shape === 'rect'
              ? <rect {...props} x={forma.x} y={forma.y} width={forma.width} height={forma.height} rx={forma.rx} />
              : <path {...props} d={forma.d} />}
            <text x={forma.labelX} y={forma.labelY - 8} className={rotuloClaro ? 'lbl-light' : undefined}>
              {setor.nome}
            </text>
            <text x={forma.labelX} y={forma.labelY + 8} className={`lbl-sub ${rotuloClaro ? 'lbl-light' : ''}`}>
              {esgotado ? 'esgotado' : `${livres.toLocaleString('pt-BR')} livres`}
            </text>
          </g>
        )
      })}
    </svg>
  )
}
