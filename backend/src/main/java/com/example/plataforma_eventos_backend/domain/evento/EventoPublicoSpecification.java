package com.example.plataforma_eventos_backend.domain.evento;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Predicados construídos só para os filtros informados: parâmetro ausente nem chega a
 * virar bind na query, evitando o problema do Postgres não conseguir inferir o tipo de
 * um parâmetro usado somente em "IS NULL".
 */
public final class EventoPublicoSpecification {

    private EventoPublicoSpecification() {
    }

    public static Specification<Evento> filtros(String q, String cidade, TipoEvento tipo, OffsetDateTime de,
                                                  OffsetDateTime ate, BigDecimal precoMin, BigDecimal precoMax) {
        return (root, query, criteriaBuilder) -> {
            HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) criteriaBuilder;
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), StatusEvento.PUBLICADO));

            if (q != null) {
                predicates.add(cb.ilike(root.get("titulo"), "%" + q + "%"));
            }
            if (cidade != null) {
                predicates.add(cb.ilike(root.get("cidade"), "%" + cidade + "%"));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (de != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("inicio"), de));
            }
            if (ate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("inicio"), ate));
            }
            if (precoMin != null || precoMax != null) {
                predicates.add(cb.exists(subQuerySetorNaFaixa(root, query, cb, precoMin, precoMax)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<Long> subQuerySetorNaFaixa(Root<Evento> eventoRoot, jakarta.persistence.criteria.CommonAbstractCriteria query,
                                                         HibernateCriteriaBuilder cb, BigDecimal precoMin, BigDecimal precoMax) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Setor> setorRoot = subquery.from(Setor.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(setorRoot.get("evento"), eventoRoot));
        if (precoMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(setorRoot.get("preco"), precoMin));
        }
        if (precoMax != null) {
            predicates.add(cb.lessThanOrEqualTo(setorRoot.get("preco"), precoMax));
        }
        subquery.select(setorRoot.get("id")).where(predicates.toArray(new Predicate[0]));
        return subquery;
    }
}
