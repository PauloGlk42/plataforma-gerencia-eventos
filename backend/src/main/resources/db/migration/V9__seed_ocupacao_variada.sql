-- Enriquece o seed com mais eventos publicados e ocupações variadas por setor,
-- pra listagem pública exercitar todas as faixas do medidor de ocupação do
-- card (vazio, ~50%, ~85%, ~97% e esgotado) e não só os dois eventos "zerados"
-- da V6. Snapshot escrito direto, sem chamar API externa, igual à V6.

INSERT INTO evento (organizador_id, tipo, fonte, id_externo, titulo, sinopse, imagem_url,
                     local_nome, cidade, uf, inicio, status, metadados)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'SHOW', 'LOCAL', 'local-turne-xpto',
     'Turnê Nacional — Banda XPTO', 'Show de rock pelo Brasil.',
     'https://picsum.photos/seed/turne-xpto/600/400',
     'Pedreira Paulo Leminski', 'Curitiba', 'PR', '2026-08-27 21:00:00-03', 'PUBLICADO', NULL),

    ('a0000000-0000-0000-0000-000000000001', 'FILME', 'LOCAL', 'local-invocacao-do-mal-5',
     'Invocação do Mal 5', 'Sessão de terror.',
     'https://picsum.photos/seed/invocacao-5/600/400',
     'Cinemark Botafogo', 'Rio de Janeiro', 'RJ', '2026-08-29 20:00:00-03', 'PUBLICADO',
     '{"duracaoMin": 110, "classificacao": "16 anos"}'),

    ('a0000000-0000-0000-0000-000000000001', 'SHOW', 'LOCAL', 'local-standup-especial',
     'Stand-up Comedy Especial', 'Noite de comédia stand-up.',
     'https://picsum.photos/seed/standup-especial/600/400',
     'Teatro Sesiminas', 'Belo Horizonte', 'MG', '2026-09-01 20:30:00-03', 'PUBLICADO', NULL),

    ('a0000000-0000-0000-0000-000000000001', 'FILME', 'LOCAL', 'local-homem-aranha-novo-universo',
     'Homem-Aranha: Novo Universo', 'Sessão de animação e ação.',
     'https://picsum.photos/seed/aranha-novo-universo/600/400',
     'Cinemark Shopping Total', 'Porto Alegre', 'RS', '2026-09-04 19:00:00-03', 'PUBLICADO',
     '{"duracaoMin": 130, "classificacao": "12 anos"}'),

    ('a0000000-0000-0000-0000-000000000001', 'SHOW', 'LOCAL', 'local-festival-de-inverno',
     'Festival de Inverno', 'Festival multi-atrações ao ar livre.',
     'https://picsum.photos/seed/festival-inverno/600/400',
     'Arena Fonte Nova', 'Salvador', 'BA', '2026-09-07 18:00:00-03', 'PUBLICADO', NULL),

    ('a0000000-0000-0000-0000-000000000001', 'FILME', 'LOCAL', 'local-divertida-mente-3',
     'Divertida Mente 3', 'Sessão infantil de animação.',
     'https://picsum.photos/seed/divertida-mente-3/600/400',
     'Cinemark Iguatemi Fortaleza', 'Fortaleza', 'CE', '2026-09-10 16:00:00-03', 'PUBLICADO',
     '{"duracaoMin": 100, "classificacao": "Livre"}'),

    ('a0000000-0000-0000-0000-000000000001', 'SHOW', 'LOCAL', 'local-orquestra-noite-classica',
     'Orquestra Sinfônica — Noite Clássica', 'Concerto de música clássica.',
     'https://picsum.photos/seed/orquestra-classica/600/400',
     'Teatro Guararapes', 'Recife', 'PE', '2026-09-14 20:00:00-03', 'PUBLICADO', NULL),

    ('a0000000-0000-0000-0000-000000000001', 'FILME', 'LOCAL', 'local-missao-impossivel-ato-final',
     'Missão Impossível — Ato Final', 'Sessão de ação.',
     'https://picsum.photos/seed/missao-impossivel/600/400',
     'Cinemark Iguatemi Brasília', 'Brasília', 'DF', '2026-09-22 21:30:00-03', 'PUBLICADO',
     '{"duracaoMin": 150, "classificacao": "14 anos"}');

-- capacidade/ocupados escolhidos pra cobrir vazio (0%), ~50%, ~85%, ~97% e
-- esgotado (100%) na agregação por evento que a listagem pública usa; dentro
-- de cada evento a ocupação também varia entre os setores, não é uniforme.
INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT e.id, v.slug, v.nome, v.preco, v.capacidade, v.ocupados
FROM evento e
JOIN (VALUES
    -- Turnê Nacional — Banda XPTO: agregado 0/750 = vazio
    ('local-turne-xpto',                  'PISTA',      'Pista',      150.00, 700, 0),
    ('local-turne-xpto',                  'CAMAROTE',   'Camarote',   400.00,  50, 0),

    -- Invocação do Mal 5: agregado 50/100 = ~50%
    ('local-invocacao-do-mal-5',          'SALA_1',     'Sala 1',      32.00,  80, 50),
    ('local-invocacao-do-mal-5',          'SALA_2',     'Sala 2',      32.00,  20,  0),

    -- Stand-up Comedy Especial: agregado 170/200 = ~85%
    ('local-standup-especial',            'PLATEIA',    'Plateia',     90.00, 150, 140),
    ('local-standup-especial',            'CAMAROTE',   'Camarote',   140.00,  50,  30),

    -- Homem-Aranha: Novo Universo: agregado 146/150 = ~97% (Sala 1 já esgotada)
    ('local-homem-aranha-novo-universo',  'SALA_1',     'Sala 1',      28.00, 100, 100),
    ('local-homem-aranha-novo-universo',  'SALA_2',     'Sala 2',      38.00,  50,  46),

    -- Festival de Inverno: agregado 340/340 = 100% — evento inteiramente esgotado
    ('local-festival-de-inverno',         'PISTA',      'Pista',      120.00, 300, 300),
    ('local-festival-de-inverno',         'CAMAROTE',   'Camarote',   350.00,  40,  40),

    -- Divertida Mente 3: 54/180 = ~30%
    ('local-divertida-mente-3',           'SALA',       'Sala 5',      25.00, 180,  54),

    -- Orquestra Sinfônica — Noite Clássica: agregado 162/250 = ~65%
    ('local-orquestra-noite-classica',    'PLATEIA_A',  'Plateia A',  150.00, 200, 130),
    ('local-orquestra-noite-classica',    'PLATEIA_B',  'Plateia B',  250.00,  50,  32),

    -- Missão Impossível — Ato Final: 9/60 = 15%
    ('local-missao-impossivel-ato-final', 'SALA_VIP',   'Sala VIP',    55.00,  60,   9)
) AS v(id_externo, slug, nome, preco, capacidade, ocupados) ON v.id_externo = e.id_externo;
