-- Duplicidade de login hoje só é barrada na aplicação, o que não segura concorrência.
ALTER TABLE users
    ADD CONSTRAINT uq_users_login UNIQUE (login);
