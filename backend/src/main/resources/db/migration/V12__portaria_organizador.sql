-- Um acesso de portaria passa a pertencer a um organizador específico: só valida
-- ingressos dos eventos daquele organizador, não de todos os eventos publicados.
-- Nula para CLIENTE/ORGANIZADOR (não se aplica), preenchida para PORTARIA.
ALTER TABLE users ADD COLUMN organizador_id varchar(255) REFERENCES users(id);

UPDATE users
   SET organizador_id = (SELECT id FROM users WHERE login = 'organizador@evento.com')
 WHERE login = 'portaria@evento.com';
