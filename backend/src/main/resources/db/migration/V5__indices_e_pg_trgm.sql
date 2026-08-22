CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- acelera ILIKE '%texto%' na busca da home, que degrada em B-tree; com os ~20 eventos
-- semeados nenhum índice é estritamente necessário, mas é o primeiro ponto a piorar em
-- produção
CREATE INDEX idx_evento_titulo_trgm  ON evento USING gin (titulo gin_trgm_ops);
CREATE INDEX idx_evento_status_data  ON evento (status, inicio);
CREATE INDEX idx_evento_cidade       ON evento (cidade);
CREATE INDEX idx_pedido_expiracao    ON pedido (status, expira_em);
CREATE INDEX idx_pedido_cliente      ON pedido (cliente_id);
CREATE INDEX idx_ingresso_pedido     ON ingresso (pedido_id);
