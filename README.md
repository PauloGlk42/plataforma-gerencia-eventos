# Plataforma de Eventos e Ingressos

> README ainda em construção — a versão completa (passo a passo, decisões de
> arquitetura, não-objetivos, uso de IA) entra depois que o backend e o
> frontend estiverem fechados. Ver `docs/CONTEXTO-PROJETO.md` §13.

## Credenciais de demonstração (seed)

Os quatro usuários semeados (migrations V6 + V8) usam a mesma senha, por
serem dados de demonstração:

| Papel       | Login                    | Senha      |
|-------------|--------------------------|------------|
| Organizador | organizador@evento.com   | senha123   |
| Cliente     | cliente1@evento.com      | senha123   |
| Cliente     | cliente2@evento.com      | senha123   |
| Portaria    | portaria@evento.com      | senha123   |

Login: `POST /auth/login` com `{ "login": "...", "password": "senha123" }`,
retorna `{ "token": "..." }` (JWT, usar como `Authorization: Bearer <token>`).
