-- Normaliza as credenciais do seed de demonstração (V6) para uma senha conhecida,
-- permitindo login em cada papel sem precisar tocar no banco na mão. Migration
-- separada porque a V6 já foi aplicada e o Flyway valida checksum de migration
-- já rodada.
--
-- Senha para os quatro usuários: senha123
-- Hash gerado com o mesmo BCryptPasswordEncoder (custo 10) usado pela aplicação.

UPDATE users
   SET password = '$2a$10$ULjhIYFrHACc4BIO2MSTG.7GelwmGhlc6z6EOq51ZyzrRg77tG1YO'
 WHERE login IN ('organizador@evento.com', 'cliente1@evento.com', 'cliente2@evento.com', 'portaria@evento.com');

UPDATE users SET role = 'ORGANIZADOR' WHERE login = 'organizador@evento.com';
UPDATE users SET role = 'CLIENTE'     WHERE login IN ('cliente1@evento.com', 'cliente2@evento.com');
UPDATE users SET role = 'PORTARIA'    WHERE login = 'portaria@evento.com';
