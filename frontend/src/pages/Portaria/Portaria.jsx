import { useEffect, useRef, useState } from 'react'
// jsQR: decodifica um ImageData puro, sem UI própria — ~30kb, contra as
// centenas de kb do html5-qrcode (que também empacota a própria interface de
// câmera). Aqui o loop de captura é nosso, então o resultado respeita o mesmo
// visual do resto do app em vez do widget de outra lib.
import jsQR from 'jsqr'
import { usePortariaEventos, useValidarIngresso } from '../../hooks/usePortaria'
import { formatarDataHora } from '../../lib/format'
import { Carregando, ErroCarregamento, Vazio } from '../../components/QueryState/QueryState'
import './Portaria.css'

const RESULTADO_INFO = {
  VALIDO: { rotulo: 'Válido', classe: 'res-valido', Icone: IconeCheck },
  INVALIDO: { rotulo: 'Inválido', classe: 'res-invalido', Icone: IconeX },
  JA_UTILIZADO: { rotulo: 'Já utilizado', classe: 'res-utilizado', Icone: IconeRelogio },
  EVENTO_ERRADO: { rotulo: 'Evento errado', classe: 'res-errado', Icone: IconeAlerta },
}

function IconeCheck() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M8 12.5l2.5 2.5L16 9.5" />
    </svg>
  )
}

function IconeX() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M9 9l6 6M15 9l-6 6" />
    </svg>
  )
}

function IconeRelogio() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3.5 2" />
    </svg>
  )
}

function IconeAlerta() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 3.5l9.5 16.5H2.5L12 3.5z" />
      <path d="M12 10v4" />
      <circle cx="12" cy="17" r=".6" fill="currentColor" stroke="none" />
    </svg>
  )
}

function SeletorEvento({ onSelecionar }) {
  const { data: eventos, isLoading, isError, error, refetch } = usePortariaEventos()

  if (isLoading) return <Carregando texto="Carregando eventos…" />
  if (isError) {
    return (
      <ErroCarregamento
        mensagem={error?.mensagem ?? 'Não foi possível carregar os eventos.'}
        onTentarNovamente={refetch}
      />
    )
  }
  if (!eventos || eventos.length === 0) {
    return <Vazio titulo="Nenhum evento publicado" descricao="Não há eventos disponíveis para validar ingressos." />
  }

  return (
    <div className="portaria-eventos">
      {eventos.map(evento => (
        <button key={evento.id} type="button" className="portaria-evento-card" onClick={() => onSelecionar(evento)}>
          <span className="pe-titulo">{evento.titulo}</span>
          <span className="pe-meta">{formatarDataHora(evento.inicio)} · {evento.localNome}, {evento.cidade}</span>
        </button>
      ))}
    </div>
  )
}

function Scanner({ ativo, onLido }) {
  const videoRef = useRef(null)
  const canvasRef = useRef(null)
  const onLidoRef = useRef(onLido)
  // navigator.mediaDevices não muda durante a vida do componente: checado uma
  // vez no estado inicial em vez de setState dentro do efeito de captura.
  const [erroCamera, setErroCamera] = useState(() => (
    navigator.mediaDevices?.getUserMedia ? null : 'Este navegador não permite acesso à câmera. Use o código manual abaixo.'
  ))
  const semSuporteCamera = !navigator.mediaDevices?.getUserMedia

  useEffect(() => {
    onLidoRef.current = onLido
  })

  useEffect(() => {
    if (!ativo || semSuporteCamera) return undefined

    let streamAtual = null
    let quadro = null
    let cancelado = false

    function ler() {
      const video = videoRef.current
      const canvas = canvasRef.current
      if (cancelado) return
      if (video && canvas && video.readyState === video.HAVE_ENOUGH_DATA) {
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        const contexto = canvas.getContext('2d', { willReadFrequently: true })
        contexto.drawImage(video, 0, 0, canvas.width, canvas.height)
        const imagem = contexto.getImageData(0, 0, canvas.width, canvas.height)
        const lido = jsQR(imagem.data, imagem.width, imagem.height)
        if (lido?.data) {
          onLidoRef.current(lido.data)
          return
        }
      }
      quadro = requestAnimationFrame(ler)
    }

    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
      .then(stream => {
        if (cancelado) {
          stream.getTracks().forEach(faixa => faixa.stop())
          return
        }
        streamAtual = stream
        setErroCamera(null)
        videoRef.current.srcObject = stream
        return videoRef.current.play()
      })
      .then(() => {
        if (!cancelado) ler()
      })
      .catch(() => {
        if (!cancelado) {
          setErroCamera('Câmera indisponível ou permissão negada. Use o código manual abaixo.')
        }
      })

    return () => {
      cancelado = true
      if (quadro) cancelAnimationFrame(quadro)
      if (streamAtual) streamAtual.getTracks().forEach(faixa => faixa.stop())
    }
  }, [ativo, semSuporteCamera])

  return (
    <div className="scanner">
      <video ref={videoRef} className="scanner-video" muted playsInline />
      <canvas ref={canvasRef} hidden />
      {erroCamera && <p className="scanner-aviso" role="alert">{erroCamera}</p>}
    </div>
  )
}

function ResultadoPainel({ resultado, onProximo }) {
  // fallback pra INVALIDO se o resultado vier num formato inesperado — melhor mostrar
  // "código inválido" do que quebrar a tela da portaria no meio de uma fila.
  const info = RESULTADO_INFO[resultado.resultado] ?? RESULTADO_INFO.INVALIDO
  const Icone = info.Icone

  return (
    <div className={`resultado ${info.classe}`} role="status">
      <Icone />
      <p className="resultado-rotulo">{info.rotulo}</p>

      {resultado.resultado === 'VALIDO' && (
        <div className="resultado-detalhe">
          <span>{resultado.eventoTitulo}</span>
          <span>{resultado.setorNome} · {formatarDataHora(resultado.eventoInicio)}</span>
        </div>
      )}
      {resultado.resultado === 'JA_UTILIZADO' && (
        <div className="resultado-detalhe">
          <span>Validado em {formatarDataHora(resultado.validadoEm)}</span>
        </div>
      )}

      <button type="button" className="cta" onClick={onProximo}>Validar próximo</button>
    </div>
  )
}

export default function Portaria() {
  const [evento, setEvento] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [codigoManual, setCodigoManual] = useState('')
  const [erroValidacao, setErroValidacao] = useState(null)
  const validar = useValidarIngresso()

  const aguardandoLeitura = !!evento && !resultado
  const scannerAtivo = aguardandoLeitura && !validar.isPending

  async function processarCodigo(codigo) {
    const valor = codigo.trim()
    if (!valor || validar.isPending) return
    setErroValidacao(null)
    try {
      const resposta = await validar.mutateAsync({ codigo: valor, eventoId: evento.id })
      setResultado(resposta)
    } catch (err) {
      setErroValidacao(err.mensagem ?? 'Não foi possível validar. Tente novamente.')
    }
  }

  function aoSubmeterManual(ev) {
    ev.preventDefault()
    processarCodigo(codigoManual)
    setCodigoManual('')
  }

  function proximaLeitura() {
    setResultado(null)
    setErroValidacao(null)
    setCodigoManual('')
  }

  function trocarEvento() {
    setEvento(null)
    setResultado(null)
    setErroValidacao(null)
    setCodigoManual('')
  }

  if (!evento) {
    return (
      <section className="portaria">
        <div className="page-head">
          <h1>Portaria</h1>
          <p>Escolha o evento que este portão está operando.</p>
        </div>
        <SeletorEvento onSelecionar={setEvento} />
      </section>
    )
  }

  return (
    <section className="portaria">
      <div className="page-head">
        <h1>Portaria</h1>
        <p className="portaria-evento-atual">
          {evento.titulo} · {formatarDataHora(evento.inicio)}
          {' '}
          <button type="button" className="link-trocar" onClick={trocarEvento}>Trocar evento</button>
        </p>
      </div>

      {resultado ? (
        <ResultadoPainel resultado={resultado} onProximo={proximaLeitura} />
      ) : (
        <>
          <Scanner ativo={scannerAtivo} onLido={processarCodigo} />

          <form className="manual-form" onSubmit={aoSubmeterManual}>
            <div className="field">
              <label htmlFor="codigo-manual">Código do ingresso</label>
              <input
                id="codigo-manual"
                type="text"
                autoComplete="off"
                autoCapitalize="off"
                autoCorrect="off"
                placeholder="cole ou digite o código do QR"
                value={codigoManual}
                onChange={e => setCodigoManual(e.target.value)}
                disabled={validar.isPending}
              />
            </div>
            <button className="cta" type="submit" disabled={validar.isPending || !codigoManual.trim()}>
              {validar.isPending ? 'Validando…' : 'Validar código'}
            </button>
          </form>

          {erroValidacao && <p className="auth-error" role="alert">{erroValidacao}</p>}
        </>
      )}
    </section>
  )
}
