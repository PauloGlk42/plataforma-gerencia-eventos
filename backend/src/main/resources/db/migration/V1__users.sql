-- A tabela de usuários já existia, criada pelo Hibernate (ddl-auto=update) para a
-- autenticação. Passa a ser declarada aqui, com a mesma estrutura, para que o schema
-- inteiro suba do zero em qualquer ambiente novo. IF NOT EXISTS porque em ambientes onde
-- ela já foi criada pelo Hibernate esta migration não deve tentar recriá-la.
CREATE TABLE IF NOT EXISTS users (
    id       varchar(255) NOT NULL,
    name     varchar(255),
    login    varchar(255),
    password varchar(255),
    role     varchar(255),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);
