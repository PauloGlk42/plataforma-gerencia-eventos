package com.example.plataforma_eventos_backend.services;

import com.example.plataforma_eventos_backend.domain.evento.Evento;
import com.example.plataforma_eventos_backend.domain.evento.EventoPublicoSpecification;
import com.example.plataforma_eventos_backend.domain.evento.OcupacaoPorEvento;
import com.example.plataforma_eventos_backend.domain.evento.PrecoMinimoPorEvento;
import com.example.plataforma_eventos_backend.domain.evento.Setor;
import com.example.plataforma_eventos_backend.domain.evento.StatusEvento;
import com.example.plataforma_eventos_backend.domain.evento.TipoEvento;
import com.example.plataforma_eventos_backend.domain.evento.dtos.CriarEventoDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.CriarSetorDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoDetalheDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoPortariaDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.EventoResumoDTO;
import com.example.plataforma_eventos_backend.domain.evento.dtos.SetorDTO;
import com.example.plataforma_eventos_backend.domain.user.User;
import com.example.plataforma_eventos_backend.infra.exception.RecursoNaoEncontradoException;
import com.example.plataforma_eventos_backend.infra.exception.RegraNegocioException;
import com.example.plataforma_eventos_backend.repositories.EventoRepository;
import com.example.plataforma_eventos_backend.repositories.SetorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final SetorRepository setorRepository;

    public EventoService(EventoRepository eventoRepository, SetorRepository setorRepository) {
        this.eventoRepository = eventoRepository;
        this.setorRepository = setorRepository;
    }

    @Transactional
    public EventoDetalheDTO criar(CriarEventoDTO dto, User organizador) {
        Evento evento = new Evento();
        evento.setOrganizador(organizador);
        evento.setTipo(dto.tipo());
        evento.setFonte(dto.fonte());
        evento.setIdExterno(dto.idExterno());
        evento.setTitulo(dto.titulo());
        evento.setSinopse(dto.sinopse());
        evento.setImagemUrl(dto.imagemUrl());
        evento.setMetadados(dto.metadados());
        evento.setLocalNome(dto.localNome());
        evento.setCidade(dto.cidade());
        evento.setUf(dto.uf());
        evento.setInicio(dto.inicio());
        evento.setFim(dto.fim());
        evento.setStatus(StatusEvento.RASCUNHO);
        OffsetDateTime agora = OffsetDateTime.now();
        evento.setCriadoEm(agora);
        evento.setAtualizadoEm(agora);
        eventoRepository.save(evento);

        List<Setor> setores = dto.setores().stream()
                .map(setorDTO -> criarSetor(evento, setorDTO))
                .toList();
        setorRepository.saveAll(setores);

        return new EventoDetalheDTO(evento.getId(), evento.getTipo(), evento.getFonte(), evento.getIdExterno(),
                evento.getTitulo(), evento.getSinopse(), evento.getImagemUrl(), evento.getMetadados(),
                evento.getLocalNome(), evento.getCidade(), evento.getUf(), evento.getInicio(), evento.getFim(),
                evento.getStatus(), setores.stream().map(SetorDTO::de).toList());
    }

    private Setor criarSetor(Evento evento, CriarSetorDTO dto) {
        Setor setor = new Setor();
        setor.setEvento(evento);
        setor.setSlug(dto.slug());
        setor.setNome(dto.nome());
        setor.setPreco(dto.preco());
        setor.setCapacidade(dto.capacidade());
        setor.setOcupados(0);
        return setor;
    }

    public List<EventoResumoDTO> buscarPorOrganizador(User organizador) {
        return paraResumos(eventoRepository.findByOrganizador(organizador));
    }

    public List<EventoPortariaDTO> buscarPublicadosParaPortaria() {
        return eventoRepository.findByStatusOrderByInicio(StatusEvento.PUBLICADO).stream()
                .map(EventoPortariaDTO::de)
                .toList();
    }

    @Transactional
    public void publicar(Long eventoId, User organizador) {
        Evento evento = buscarDoOrganizador(eventoId, organizador);
        if (evento.getStatus() != StatusEvento.RASCUNHO) {
            throw new RegraNegocioException("Somente eventos em rascunho podem ser publicados");
        }
        evento.setStatus(StatusEvento.PUBLICADO);
        evento.setAtualizadoEm(OffsetDateTime.now());
    }

    private Evento buscarDoOrganizador(Long eventoId, User organizador) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        if (!evento.getOrganizador().getId().equals(organizador.getId())) {
            throw new AccessDeniedException("Evento pertence a outro organizador");
        }
        return evento;
    }

    public Page<EventoResumoDTO> buscarPublicados(String q, String cidade, TipoEvento tipo, OffsetDateTime de,
                                                   OffsetDateTime ate, BigDecimal precoMin, BigDecimal precoMax,
                                                   Pageable pageable) {
        var especificacao = EventoPublicoSpecification.filtros(vazioParaNull(q), vazioParaNull(cidade), tipo,
                de, ate, precoMin, precoMax);
        Page<Evento> pagina = eventoRepository.findAll(especificacao, pageable);
        Map<Long, BigDecimal> precosMinimos = precoMinimoPorEvento(pagina.getContent());
        Map<Long, OcupacaoPorEvento> ocupacoes = ocupacaoPorEvento(pagina.getContent());
        return pagina.map(evento -> paraResumo(evento, precosMinimos.get(evento.getId()), ocupacoes.get(evento.getId())));
    }

    public EventoDetalheDTO buscarDetalhePublico(Long eventoId, User usuarioAtual) {
        Evento evento = eventoRepository.findById(eventoId)
                .filter(e -> podeVisualizar(e, usuarioAtual))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        List<Setor> setores = setorRepository.findByEvento(evento);
        return new EventoDetalheDTO(evento.getId(), evento.getTipo(), evento.getFonte(), evento.getIdExterno(),
                evento.getTitulo(), evento.getSinopse(), evento.getImagemUrl(), evento.getMetadados(),
                evento.getLocalNome(), evento.getCidade(), evento.getUf(), evento.getInicio(), evento.getFim(),
                evento.getStatus(), setores.stream().map(SetorDTO::de).toList());
    }

    /**
     * Rascunho/cancelado só é visível pro organizador dono. Qualquer outro caso vira
     * "não encontrado" (404), nunca "acesso negado" (403) — 403 confirmaria pra quem não
     * é dono que o evento existe.
     */
    private boolean podeVisualizar(Evento evento, User usuarioAtual) {
        if (evento.getStatus() == StatusEvento.PUBLICADO) {
            return true;
        }
        return usuarioAtual != null && evento.getOrganizador().getId().equals(usuarioAtual.getId());
    }

    private List<EventoResumoDTO> paraResumos(List<Evento> eventos) {
        Map<Long, BigDecimal> precosMinimos = precoMinimoPorEvento(eventos);
        Map<Long, OcupacaoPorEvento> ocupacoes = ocupacaoPorEvento(eventos);
        return eventos.stream()
                .map(evento -> paraResumo(evento, precosMinimos.get(evento.getId()), ocupacoes.get(evento.getId())))
                .toList();
    }

    private Map<Long, BigDecimal> precoMinimoPorEvento(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            return Map.of();
        }
        return setorRepository.buscarPrecoMinimoPorEvento(eventos).stream()
                .collect(Collectors.toMap(PrecoMinimoPorEvento::getEventoId, PrecoMinimoPorEvento::getPrecoMinimo));
    }

    private Map<Long, OcupacaoPorEvento> ocupacaoPorEvento(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            return Map.of();
        }
        return setorRepository.buscarOcupacaoPorEvento(eventos).stream()
                .collect(Collectors.toMap(OcupacaoPorEvento::getEventoId, ocupacao -> ocupacao));
    }

    private EventoResumoDTO paraResumo(Evento evento, BigDecimal precoMinimo, OcupacaoPorEvento ocupacao) {
        Integer capacidade = ocupacao != null ? ocupacao.getCapacidadeTotal().intValue() : null;
        Integer ocupados = ocupacao != null ? ocupacao.getOcupadosTotal().intValue() : null;
        return new EventoResumoDTO(evento.getId(), evento.getTipo(), evento.getTitulo(), evento.getImagemUrl(),
                evento.getLocalNome(), evento.getCidade(), evento.getUf(), evento.getInicio(), evento.getStatus(),
                precoMinimo, capacidade, ocupados);
    }

    private static String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
