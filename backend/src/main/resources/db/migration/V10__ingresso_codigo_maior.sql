-- codigo = <identificador aleatório>.<HMAC-SHA256 do identificador, em base64url>.
-- Com identificador de 32 hex chars (UUID sem hífen) + assinatura de 43 chars em
-- base64url, o total passa de 60 (limite definido na V4, antes da assinatura existir).
ALTER TABLE ingresso ALTER COLUMN codigo TYPE varchar(120);
