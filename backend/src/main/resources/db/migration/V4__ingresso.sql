CREATE TABLE ingresso (
    id              bigserial     PRIMARY KEY,
    pedido_id       bigint        NOT NULL REFERENCES pedido(id),
    setor_id        bigint        NOT NULL REFERENCES setor(id),
    codigo          varchar(60)   NOT NULL UNIQUE,
    status          varchar(20)   NOT NULL,
    validado_em     timestamptz,
    validado_por    varchar(255)  REFERENCES users(id),
    token_publico   uuid          NOT NULL UNIQUE,
    criado_em       timestamptz   NOT NULL DEFAULT now()
);
