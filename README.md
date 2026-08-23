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

## Pagamento simulado

`POST /api/pedidos/{id}/pagamento`, com o cliente dono do pedido autenticado. Não existe
gateway real: o resultado é determinístico pelo final do número do cartão, para o
avaliador conseguir exercitar os dois caminhos sem depender de nenhum serviço externo.

| Final do cartão | Resultado |
|---|---|
| `0000` | Recusado — saldo insuficiente |
| `1111` | Recusado — suspeita de fraude |
| Qualquer outro número válido (13 a 19 dígitos) | Aprovado |

Toda tentativa gera uma linha em `pagamento` (aprovado ou recusado, com o motivo). Recusa
**não cancela** o pedido: ele continua `PENDENTE` até o prazo da reserva vencer, e o
cliente pode tentar outro cartão. Aprovação muda o pedido para `PAGO` e emite os
ingressos na mesma transação; pagar de novo um pedido já `PAGO` é rejeitado (400), sem
emitir ingresso duplicado.

## Ingresso e QR

Cada unidade comprada vira um ingresso próprio, com `codigo` no formato
`<identificador>.<assinatura>` — uma assinatura HMAC-SHA256 do identificador, em
base64url, calculada com a chave `INGRESSO_SECRET` (própria, separada do `JWT_SECRET`).
A imagem do QR é responsabilidade do frontend; a API só devolve essa string.

- `GET /api/meus-ingressos` — ingressos do cliente autenticado, agrupados por evento.
- `POST /api/ingressos/{id}/compartilhar` — devolve o `token_publico` do ingresso (dono).
- `GET /api/publico/ingressos/{token}` — sem autenticação; devolve evento, data, local,
  setor e o código do QR, e **nenhum dado do comprador**. `token_publico` é um UUID
  separado do `codigo`: a URL compartilhada não é, ela mesma, o payload de validação.

A verificação de assinatura acontece antes de qualquer consulta ao banco — um código
forjado é rejeitado sem custo de I/O.
