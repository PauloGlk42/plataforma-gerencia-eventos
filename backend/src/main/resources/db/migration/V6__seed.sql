-- Seed de demonstração: 1 organizador, 2 clientes, 1 portaria e 2 eventos publicados com
-- setores disponíveis. Sem chamada a API externa — título, sinopse e imagem já vão como
-- snapshot direto, como qualquer evento criado pelo organizador.

INSERT INTO users (id, name, login, password, role) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Ana Organizadora', 'organizador@evento.com', '$2a$10$IP/Mm/hF/jGXQIBRcBIylOgNxXSw.eammrrlS40yH8WbCVNdp2cvG', 'ORGANIZADOR'),
    ('a0000000-0000-0000-0000-000000000002', 'Bruno Cliente',     'cliente1@evento.com',    '$2a$10$woeAlxzkxhxrVRSlkug1eexJ1FqsSY6WZ/R6C8TuqKkZNLAcC7536', 'CLIENTE'),
    ('a0000000-0000-0000-0000-000000000003', 'Carla Cliente',     'cliente2@evento.com',    '$2a$10$woeAlxzkxhxrVRSlkug1eexJ1FqsSY6WZ/R6C8TuqKkZNLAcC7536', 'CLIENTE'),
    ('a0000000-0000-0000-0000-000000000004', 'Diego Portaria',    'portaria@evento.com',    '$2a$10$cY622duBBFz7n4E5FohrTufy4dGtc54AAVj.hkR0DOzbwf7bFMt4e', 'PORTARIA');

WITH evento_show AS (
    INSERT INTO evento (organizador_id, tipo, fonte, id_externo, titulo, sinopse, imagem_url,
                         local_nome, cidade, uf, inicio, status)
    VALUES ('a0000000-0000-0000-0000-000000000001', 'SHOW', 'LOCAL', 'local-metallica-m72',
            'Metallica — M72 World Tour', 'Show de metal em São Paulo.',
            'https://picsum.photos/seed/metallica/600/400',
            'Allianz Parque', 'São Paulo', 'SP', '2026-11-15 21:00:00-03', 'PUBLICADO')
    RETURNING id
),
evento_filme AS (
    INSERT INTO evento (organizador_id, tipo, fonte, id_externo, titulo, sinopse, imagem_url,
                         local_nome, cidade, uf, inicio, status, metadados)
    VALUES ('a0000000-0000-0000-0000-000000000001', 'FILME', 'LOCAL', 'local-duna-parte-tres',
            'Duna: Parte Três', 'Sessão de cinema — ficção científica.',
            'https://picsum.photos/seed/duna/600/400',
            'Cinemark Iguatemi', 'São Paulo', 'SP', '2026-09-20 19:30:00-03', 'PUBLICADO',
            '{"duracaoMin": 155, "classificacao": "14 anos"}')
    RETURNING id
),
setores_show AS (
    INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
    SELECT evento_show.id, s.slug, s.nome, s.preco, s.capacidade, 0
    FROM evento_show, (VALUES
        ('PISTA', 'Pista', 250.00, 500),
        ('CAMAROTE_A', 'Camarote A', 600.00, 80)
    ) AS s(slug, nome, preco, capacidade)
)
INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT evento_filme.id, 'SALA', 'Sala 3', 35.00, 120, 0
FROM evento_filme;
