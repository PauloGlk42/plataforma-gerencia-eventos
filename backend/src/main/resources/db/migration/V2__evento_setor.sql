CREATE TABLE evento (
    id              bigserial     PRIMARY KEY,
    organizador_id  varchar(255)  NOT NULL REFERENCES users(id),
    tipo            varchar(10)   NOT NULL,
    fonte           varchar(20)   NOT NULL,
    id_externo      varchar(100),
    titulo          varchar(200)  NOT NULL,
    sinopse         text,
    imagem_url      varchar(500),
    metadados       jsonb,
    local_nome      varchar(160)  NOT NULL,
    cidade          varchar(100)  NOT NULL,
    uf              varchar(2),
    inicio          timestamptz   NOT NULL,
    fim             timestamptz,
    status          varchar(20)   NOT NULL,
    criado_em       timestamptz   NOT NULL DEFAULT now(),
    atualizado_em   timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE setor (
    id              bigserial     PRIMARY KEY,
    evento_id       bigint        NOT NULL REFERENCES evento(id) ON DELETE CASCADE,
    slug            varchar(40)   NOT NULL,
    nome            varchar(80)   NOT NULL,
    preco           numeric(10,2) NOT NULL CHECK (preco > 0),
    capacidade      int           NOT NULL CHECK (capacidade > 0),
    ocupados        int           NOT NULL DEFAULT 0,
    CONSTRAINT uq_setor_evento_slug UNIQUE (evento_id, slug),
    -- rede de segurança no banco: mesmo que um bug futuro escreva ocupados por outro
    -- caminho, o Postgres recusa vender além da capacidade ou zerar abaixo de zero
    CONSTRAINT ck_setor_estoque CHECK (ocupados >= 0 AND ocupados <= capacidade)
);
