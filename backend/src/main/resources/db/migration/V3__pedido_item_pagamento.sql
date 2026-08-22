CREATE TABLE pedido (
    id              bigserial     PRIMARY KEY,
    codigo          uuid          NOT NULL UNIQUE,
    cliente_id      varchar(255)  NOT NULL REFERENCES users(id),
    evento_id       bigint        NOT NULL REFERENCES evento(id),
    status          varchar(20)   NOT NULL,
    valor_total     numeric(10,2) NOT NULL,
    expira_em       timestamptz,
    criado_em       timestamptz   NOT NULL DEFAULT now(),
    atualizado_em   timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE pedido_item (
    id              bigserial     PRIMARY KEY,
    pedido_id       bigint        NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    setor_id        bigint        NOT NULL REFERENCES setor(id),
    quantidade      int           NOT NULL CHECK (quantidade > 0),
    -- preço congelado no momento da reserva: reajuste posterior do setor não pode
    -- alterar o valor de pedidos já feitos
    preco_unitario  numeric(10,2) NOT NULL,
    CONSTRAINT uq_item_pedido_setor UNIQUE (pedido_id, setor_id)
);

CREATE TABLE pagamento (
    id              bigserial     PRIMARY KEY,
    pedido_id       bigint        NOT NULL REFERENCES pedido(id),
    status          varchar(20)   NOT NULL,
    motivo          varchar(200),
    valor           numeric(10,2) NOT NULL,
    criado_em       timestamptz   NOT NULL DEFAULT now()
);
