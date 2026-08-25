import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCatalogo } from '../../hooks/useCatalogo'
import { useCriarEvento } from '../../hooks/useOrganizador'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import './NovoEvento.css'

// O slug do setor não é texto livre: o mapa SVG (SectorMap/sectorShapes) casa
// por slug fixo, então as opções aqui são exatamente as que têm geometria
// desenhada — um slug inventado cairia no fallback de lista, sem planta.
const SLUGS_POR_TIPO = {
  SHOW: [
    { slug: 'PISTA', nome: 'Pista' },
    { slug: 'ARQUIBANCADA', nome: 'Arquibancada' },
    { slug: 'CAMAROTE_A', nome: 'Camarote A' },
    { slug: 'CAMAROTE_B', nome: 'Camarote B' },
  ],
  FILME: [
    { slug: 'PLATEIA', nome: 'Plateia' },
    { slug: 'VIP', nome: 'VIP' },
  ],
}

function setorVazio() {
  return { chave: crypto.randomUUID(), slug: '', nome: '', preco: '', capacidade: '' }
}

export default function NovoEvento() {
  const navigate = useNavigate()
  const criar = useCriarEvento()

  const [tipoBusca, setTipoBusca] = useState('SHOW')
  const [termoBusca, setTermoBusca] = useState('')
  const termoDebounced = useDebouncedValue(termoBusca, 300)
  const { data: itensCatalogo, isLoading: carregandoCatalogo } = useCatalogo(termoDebounced, tipoBusca)

  const [itemSelecionado, setItemSelecionado] = useState(null)

  const [localNome, setLocalNome] = useState('')
  const [cidade, setCidade] = useState('')
  const [uf, setUf] = useState('')
  const [inicio, setInicio] = useState('')
  const [fim, setFim] = useState('')

  const [setores, setSetores] = useState([setorVazio()])
  const [erro, setErro] = useState(null)

  const opcoesSlug = itemSelecionado ? SLUGS_POR_TIPO[itemSelecionado.tipo] : []
  const slugsUsados = new Set(setores.map(s => s.slug).filter(Boolean))
  const podeAdicionarSetor = !!itemSelecionado && setores.length < opcoesSlug.length

  function selecionarItem(item) {
    setItemSelecionado(item)
    setSetores([setorVazio()])
  }

  function atualizarSetor(chave, campo, valor) {
    setSetores(atual => atual.map(s => {
      if (s.chave !== chave) return s
      if (campo === 'slug') {
        const opcao = opcoesSlug.find(o => o.slug === valor)
        return { ...s, slug: valor, nome: opcao?.nome ?? '' }
      }
      return { ...s, [campo]: valor }
    }))
  }

  function removerSetor(chave) {
    setSetores(atual => (atual.length > 1 ? atual.filter(s => s.chave !== chave) : atual))
  }

  const formValido = !!itemSelecionado
    && localNome.trim() && cidade.trim() && inicio
    && setores.every(s => s.slug && s.nome.trim() && Number(s.preco) > 0 && Number(s.capacidade) >= 1)

  async function aoSubmeter(ev) {
    ev.preventDefault()
    if (!formValido || criar.isPending) return
    setErro(null)
    try {
      await criar.mutateAsync({
        tipo: itemSelecionado.tipo,
        fonte: 'LOCAL',
        idExterno: itemSelecionado.idExterno,
        titulo: itemSelecionado.titulo,
        sinopse: itemSelecionado.sinopse,
        imagemUrl: itemSelecionado.imagemUrl,
        localNome: localNome.trim(),
        cidade: cidade.trim(),
        uf: uf.trim() || null,
        inicio: new Date(inicio).toISOString(),
        fim: fim ? new Date(fim).toISOString() : null,
        setores: setores.map(s => ({
          slug: s.slug, nome: s.nome.trim(), preco: Number(s.preco), capacidade: Number(s.capacidade),
        })),
      })
      navigate('/organizador/eventos')
    } catch (err) {
      setErro(err.mensagem ?? 'Não foi possível criar o evento. Tente novamente.')
    }
  }

  return (
    <section className="novo-evento">
      <div className="page-head">
        <h1>Criar evento</h1>
        <p>Escolha um item do catálogo, preencha os dados do evento e os setores.</p>
      </div>

      <form onSubmit={aoSubmeter} className="novo-evento-form">
        <fieldset className="ne-secao">
          <legend>1. Catálogo</legend>
          {!itemSelecionado ? (
            <>
              <div className="ne-busca">
                <div className="seg" role="group" aria-label="Tipo de evento">
                  <button type="button" aria-pressed={tipoBusca === 'SHOW'} onClick={() => setTipoBusca('SHOW')}>Show</button>
                  <button type="button" aria-pressed={tipoBusca === 'FILME'} onClick={() => setTipoBusca('FILME')}>Filme</button>
                </div>
                <div className="field grow">
                  <label htmlFor="busca-catalogo">Buscar</label>
                  <input
                    id="busca-catalogo" type="search" placeholder="nome do show ou filme…"
                    value={termoBusca} onChange={e => setTermoBusca(e.target.value)}
                  />
                </div>
              </div>

              {carregandoCatalogo ? (
                <p className="ne-aviso">Buscando…</p>
              ) : (
                <div className="ne-catalogo-grid">
                  {(itensCatalogo ?? []).map(item => (
                    <button key={item.idExterno} type="button" className="ne-catalogo-item" onClick={() => selecionarItem(item)}>
                      <img src={item.imagemUrl} alt="" loading="lazy" />
                      <span className="ne-catalogo-titulo">{item.titulo}</span>
                    </button>
                  ))}
                  {(itensCatalogo ?? []).length === 0 && <p className="ne-aviso">Nenhum item encontrado.</p>}
                </div>
              )}
            </>
          ) : (
            <div className="ne-selecionado">
              <img src={itemSelecionado.imagemUrl} alt="" />
              <div className="ne-selecionado-info">
                <span className="ne-selecionado-titulo">{itemSelecionado.titulo}</span>
                <span className="ne-selecionado-sinopse">{itemSelecionado.sinopse}</span>
              </div>
              <button type="button" className="link-trocar" onClick={() => setItemSelecionado(null)}>Trocar</button>
            </div>
          )}
        </fieldset>

        <fieldset className="ne-secao" disabled={!itemSelecionado}>
          <legend>2. Data e local</legend>
          <div className="ne-grid">
            <div className="field">
              <label htmlFor="inicio">Data e hora</label>
              <input id="inicio" type="datetime-local" required value={inicio} onChange={e => setInicio(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="fim">Término (opcional)</label>
              <input id="fim" type="datetime-local" value={fim} onChange={e => setFim(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="local-nome">Local</label>
              <input id="local-nome" type="text" required value={localNome} onChange={e => setLocalNome(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="cidade">Cidade</label>
              <input id="cidade" type="text" required value={cidade} onChange={e => setCidade(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="uf">UF</label>
              <input id="uf" type="text" maxLength={2} value={uf} onChange={e => setUf(e.target.value.toUpperCase())} />
            </div>
          </div>
        </fieldset>

        <fieldset className="ne-secao" disabled={!itemSelecionado}>
          <legend>3. Setores</legend>
          <div className="ne-setores">
            {setores.map(setor => (
              <div key={setor.chave} className="ne-setor-row">
                <div className="field">
                  <label>Setor</label>
                  <select value={setor.slug} onChange={e => atualizarSetor(setor.chave, 'slug', e.target.value)}>
                    <option value="">selecione…</option>
                    {opcoesSlug.map(o => (
                      <option key={o.slug} value={o.slug} disabled={slugsUsados.has(o.slug) && setor.slug !== o.slug}>
                        {o.nome}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label>Nome exibido</label>
                  <input type="text" value={setor.nome} onChange={e => atualizarSetor(setor.chave, 'nome', e.target.value)} />
                </div>
                <div className="field">
                  <label>Preço</label>
                  <input type="number" min="0.01" step="0.01" value={setor.preco} onChange={e => atualizarSetor(setor.chave, 'preco', e.target.value)} />
                </div>
                <div className="field">
                  <label>Capacidade</label>
                  <input type="number" min="1" step="1" value={setor.capacidade} onChange={e => atualizarSetor(setor.chave, 'capacidade', e.target.value)} />
                </div>
                <button
                  type="button" className="ne-remover" onClick={() => removerSetor(setor.chave)}
                  disabled={setores.length === 1} aria-label="Remover setor"
                >×</button>
              </div>
            ))}
          </div>
          <button type="button" className="ne-add-setor" onClick={() => setSetores(atual => [...atual, setorVazio()])} disabled={!podeAdicionarSetor}>
            + Adicionar setor
          </button>
        </fieldset>

        {erro && <p className="auth-error" role="alert">{erro}</p>}

        <button className="cta" type="submit" disabled={!formValido || criar.isPending}>
          {criar.isPending ? 'Criando…' : 'Criar evento'}
        </button>
      </form>
    </section>
  )
}
