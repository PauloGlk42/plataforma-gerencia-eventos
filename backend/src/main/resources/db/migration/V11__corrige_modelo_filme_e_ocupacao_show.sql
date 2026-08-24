-- Corrige o modelo de FILME: uma sessão de cinema acontece em UMA sala. Os eventos
-- semeados na V9 com setores "Sala 1"/"Sala 2" descreviam a mesma sessão em duas salas
-- ao mesmo tempo, o que não existe — viram duas sessões (dois eventos), cada uma na sua
-- sala, com o horário da segunda sessão 3h depois. Dentro da sala, os setores passam a
-- ser faixas de preço (PLATEIA / VIP), não mais o nome da sala.

-- Invocação do Mal 5: mantém o id_externo original com o que era "Sala 1" (vira
-- PLATEIA) e cria uma segunda sessão para o que era "Sala 2".
WITH nova_sessao AS (
    INSERT INTO evento (organizador_id, tipo, fonte, id_externo, titulo, sinopse, imagem_url,
                         local_nome, cidade, uf, inicio, status, metadados)
    SELECT organizador_id, tipo, fonte, id_externo || '-sessao-2', titulo, sinopse, imagem_url,
           local_nome, cidade, uf, inicio + interval '3 hours', status, metadados
    FROM evento WHERE id_externo = 'local-invocacao-do-mal-5'
    RETURNING id
)
INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT nova_sessao.id, 'PLATEIA', 'Plateia', s.preco, s.capacidade, s.ocupados
FROM nova_sessao, setor s
JOIN evento e ON e.id = s.evento_id
WHERE e.id_externo = 'local-invocacao-do-mal-5' AND s.slug = 'SALA_2';

DELETE FROM setor
WHERE slug = 'SALA_2'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-invocacao-do-mal-5');

UPDATE setor SET slug = 'PLATEIA', nome = 'Plateia'
WHERE slug = 'SALA_1'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-invocacao-do-mal-5');

-- Homem-Aranha: Novo Universo — mesmo tratamento.
WITH nova_sessao AS (
    INSERT INTO evento (organizador_id, tipo, fonte, id_externo, titulo, sinopse, imagem_url,
                         local_nome, cidade, uf, inicio, status, metadados)
    SELECT organizador_id, tipo, fonte, id_externo || '-sessao-2', titulo, sinopse, imagem_url,
           local_nome, cidade, uf, inicio + interval '3 hours', status, metadados
    FROM evento WHERE id_externo = 'local-homem-aranha-novo-universo'
    RETURNING id
)
INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT nova_sessao.id, 'PLATEIA', 'Plateia', s.preco, s.capacidade, s.ocupados
FROM nova_sessao, setor s
JOIN evento e ON e.id = s.evento_id
WHERE e.id_externo = 'local-homem-aranha-novo-universo' AND s.slug = 'SALA_2';

DELETE FROM setor
WHERE slug = 'SALA_2'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-homem-aranha-novo-universo');

UPDATE setor SET slug = 'PLATEIA', nome = 'Plateia'
WHERE slug = 'SALA_1'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-homem-aranha-novo-universo');

-- Sessões de sala única (V6/V9) que usavam o nome da sala como slug viram PLATEIA,
-- a faixa de preço padrão.
UPDATE setor SET slug = 'PLATEIA', nome = 'Plateia'
WHERE slug = 'SALA'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-duna-parte-tres');

UPDATE setor SET slug = 'PLATEIA', nome = 'Plateia'
WHERE slug = 'SALA'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-divertida-mente-3');

-- Missão Impossível — Ato Final: sessão só de poltrona premium, vira setor único VIP.
UPDATE setor SET slug = 'VIP', nome = 'VIP'
WHERE slug = 'SALA_VIP'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-missao-impossivel-ato-final');

-- Espalha a ocupação da Turnê Nacional — Banda XPTO (SHOW) pelos quatro degraus do
-- medidor: quase vazio, pela metade, quase cheio e esgotado. Os dois setores originais
-- estavam zerados; ARQUIBANCADA e CAMAROTE_B são novos, respeitando ck_setor_estoque.
UPDATE setor SET ocupados = 70   -- 700 lugares, 10% = quase vazio
WHERE slug = 'PISTA'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-turne-xpto');

UPDATE setor SET ocupados = 50   -- 50 lugares, 100% = esgotado
WHERE slug = 'CAMAROTE'
  AND evento_id = (SELECT id FROM evento WHERE id_externo = 'local-turne-xpto');

INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT id, 'ARQUIBANCADA', 'Arquibancada', 180.00, 300, 150   -- 50% = pela metade
FROM evento WHERE id_externo = 'local-turne-xpto';

INSERT INTO setor (evento_id, slug, nome, preco, capacidade, ocupados)
SELECT id, 'CAMAROTE_B', 'Camarote B', 450.00, 80, 72   -- 90% = quase cheio
FROM evento WHERE id_externo = 'local-turne-xpto';
