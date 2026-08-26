# Plataforma de Eventos e Ingressos — Contexto e Alinhamento Técnico

> **Como usar este documento:** é o prompt de alinhamento para a sessão de desenvolvimento.
> Ele contém as decisões já tomadas, com a justificativa de cada uma, o modelo de dados,
> a arquitetura de serviços e a ordem de execução. Decisões marcadas com **[DECIDIDO]**
> não devem ser revisitadas sem motivo forte. Itens **[EM ABERTO]** ainda precisam de escolha.
>
> **Este arquivo deve ser versionado no repositório** (ex.: `docs/CONTEXTO-PROJETO.md`).
> O desafio pede explicitamente que artefatos de contexto produzidos no caminho sejam
> commitados, porque mostram como o candidato conduziu a ferramenta.

---

## 1. O desafio

Desafio Elite Dev 2026 (Verzel) — vaga de desenvolvedor full stack júnior.

Construir uma **Plataforma de Eventos e Ingressos**: um organizador publica eventos a partir
de um catálogo externo, um cliente compra ingressos com pagamento simulado e recebe um QR Code,
e a portaria valida o ingresso na entrada.

**Prazo:** 7 dias corridos a partir de 18/08. Entrega até **25/08**.

**Situação atual (22/08):** restam ~3 dias.

Pronto e verificado rodando:
- Autenticação JWT stateless com os 3 papéis (`users` / `UserRoles`)
- Frontend Vite + React inicializado, com tela de registro
- Camada de dados completa: migrations Flyway V1–V6, entidades, enums, 6 repositórios
- Seed com 1 organizador, 2 clientes, 1 portaria e 2 eventos publicados com setores
- Testado em banco vazio: V1–V6 aplicam do zero e o Hibernate valida o schema

Pendências conhecidas (ver §15).

### O que a empresa disse que quer ver

Isto é mais importante que a lista de requisitos, e deve guiar cada escolha:

- *"O escopo aqui é pequeno de propósito. O que nos interessa não é o volume entregue: é **como você pensa**. As decisões que tomou, **o que descartou pelo caminho**, por que a tela é assim e não de outro jeito."*
- *"Fuja do AI slop: aquela interface que sai pronta da ferramenta... O problema não é a IA ter feito, é ninguém ter escolhido nada."*
- *"Faça o básico rodar de ponta a ponta e só depois agregue valor. Preferimos o fluxo inteiro simples e completo a um pedaço sofisticado com telas pela metade."*
- *"Recomendamos usar IA. Conte quais ferramentas usou, em que partes, e o que fez sem IA."*

**Consequência prática:** cada decisão não-óbvia precisa de um parágrafo no README explicando
o porquê — inclusive as coisas que **não** foram feitas. A seção de não-objetivos conta ponto.

### Requisitos funcionais (do PDF)

**Front-end**
- Navegação e busca de eventos publicados, com data, local e preço
- Criação e gerenciamento de eventos pelo organizador
- Fluxo de reserva: mapa de assentos (cinema/teatro) **ou** quantidade por setor (pista) — *implementar um dos dois ou os dois*
- Pagamento simulado, contemplando confirmação **e recusa**
- Área "Meus ingressos", com o ingresso e seu QR Code
- Tela de portaria, com retorno claro: **válido / inválido / já utilizado / evento errado**
- Leitura do QR **pela câmera**, com digitação manual do código como alternativa

**Back-end**
- Gestão das chamadas à API externa (Ticketmaster Discovery ou TMDb)
- Autenticação com três papéis: Organizador, Cliente, Portaria
- Armazenamento de eventos, reservas e ingressos
- Garantia de que o mesmo lugar não seja vendido duas vezes
- QR Code que não possa ser forjado
- Compartilhamento de ingresso via link gerado pela aplicação
- Validação na portaria garantindo que o mesmo ingresso não seja validado duas vezes

**Não funcionais**
- README detalhado com passo a passo de configuração e execução; o que não funcionar deve estar declarado
- Dados semeados: 1 organizador, 2 clientes, 1 portaria, ≥1 evento publicado com ingressos disponíveis
- Repositório público no GitHub, com commits ao longo da semana e mensagens descritivas
- Deploy não é obrigatório, mas **rende +1 ponto na nota final**

**Opcionais valorizados:** busca e filtros, painel do organizador, cancelamento com devolução ao
estoque, mapa de assentos em tempo real, Docker Compose, testes, aplicação publicada.

**Explicitamente fora:** nota fiscal, revenda entre usuários, app nativo, recuperação de senha,
envio de ingresso por e-mail.

---

## 2. Stack **[DECIDIDO]**

| Camada | Escolha | Motivo |
|---|---|---|
| Front | React + Vite (sem framework) | Familiaridade e compatibilidade direta com Vercel |
| Back | Java + Spring Boot, arquitetura em camadas | Familiaridade; ecossistema maduro para JWT, JPA, agendamento |
| Banco | PostgreSQL | Sair do ecossistema Microsoft (SQL Server); precisa de `pg_trgm` |
| Migrations | Flyway | Schema versionado e reprodutível; essencial para o avaliador subir do zero |
| Auth | JWT stateless, 3 roles | Sem sessão em memória, facilita deploy em plataforma efêmera |
| Deploy | Vercel (front) + Railway (back + Postgres) | +1 ponto; Railway já provisiona Postgres gerenciado |

---

## 3. A decisão estruturante: setores por quantidade, não mapa de assentos **[DECIDIDO]**

O PDF permite explicitamente implementar apenas um dos dois modelos. Escolhemos **ocupação por
setor** (Pista, Arquibancada, Camarote), com contador de capacidade.

**Por que:** com 4 dias, o mapa de assentos consome o tempo que deveria ir para o fluxo completo
funcionando ponta a ponta — que é exatamente o que o PDF diz preferir. O modelo por setor cobre
100% dos requisitos de reserva, estoque e concorrência.

**Ganho colateral importante — filmes cabem no mesmo modelo, sem schema novo.** Uma sessão de
cinema é um evento com `tipo = FILME` e um único setor chamado `SALA`, com capacidade N.
A diferença entre show e filme passa a ser apenas a origem do catálogo (Ticketmaster vs TMDb) e a
arte do card. Adicionar filmes vira ~1 tarde de trabalho, não um subsistema novo.


---

## 4. Modelo de dados

### Princípios aplicados

1. **`local` foi dividido em dois conceitos.** O *venue* (Allianz Parque, Cinemark Iguatemi) virou
   campos desnormalizados no `evento` — tabela própria exigiria uma tela de CRUD de locais que
   ninguém pediu. O que importa de verdade é o **setor**, porque é ele que carrega preço e
   capacidade (Pista R$200/500 lugares ≠ Camarote R$500/80 lugares).
2. **`reserva` + `transacao` + `ingresso` foram colapsados.** "Reserva" não é tabela: é o `pedido`
   no estado `PENDENTE` com `expira_em` preenchido. Mesma semântica, um join a menos.
3. **`pagamento` continua tabela separada.** O PDF pede confirmação *e* recusa; registrar tentativas
   transforma a recusa num estado real do sistema em vez de um `alert()` no front. É barato.
4. **Preço congelado no item do pedido.** Se o organizador reajustar o setor, pedidos antigos não
   podem mudar de valor.

### Tabelas

> users
>   id        varchar(255) PK   -- UUID em formato string, gerado pela aplicação
>   name      varchar
>   login     varchar           -- é o e-mail; ver pendência de unique constraint em §15
>   password  varchar           -- hash bcrypt
>   role      varchar           -- enum UserRoles: CLIENTE | ORGANIZADOR | PORTARIA
> ```
>
> Consequências já aplicadas no schema: **não existe enum `Papel`** (usa-se `UserRoles`), não existe
> `criado_em` em usuário, e **todas as FKs para usuário são `varchar(255)`**, não `bigint`.
>
> O `varchar(255)` como FK é menos eficiente que `uuid` nativo. É trade-off consciente: não vale
> refatorar autenticação funcionando dentro do prazo. Documentar no README.

```
evento
  id              bigserial PK
  organizador_id  varchar(255) not null references users(id)
  tipo            varchar(10)  not null   -- SHOW | FILME
  fonte           varchar(20)  not null   -- TICKETMASTER | TMDB | LOCAL
  id_externo      varchar(100)            -- id no catálogo de origem (nullable)
  titulo          varchar(200) not null   -- SNAPSHOT do catálogo
  sinopse         text                    -- SNAPSHOT
  imagem_url      varchar(500)            -- SNAPSHOT
  metadados       jsonb                   -- duração, classificação, gênero (extensível sem migration)
  local_nome      varchar(160) not null
  cidade          varchar(100) not null
  uf              char(2)
  inicio          timestamptz  not null
  fim             timestamptz
  status          varchar(20)  not null   -- RASCUNHO | PUBLICADO | CANCELADO
  criado_em       timestamptz  not null default now()
  atualizado_em   timestamptz  not null default now()

setor
  id              bigserial PK
  evento_id       bigint       not null references evento(id) on delete cascade
  slug            varchar(40)  not null   -- PISTA | ARQUIBANCADA | CAMAROTE_A | CAMAROTE_B | SALA
  nome            varchar(80)  not null   -- rótulo exibido: "Pista Premium"
  preco           numeric(10,2) not null check (preco > 0)
  capacidade      int          not null check (capacidade > 0)
  ocupados        int          not null default 0
  constraint uq_setor_evento_slug unique (evento_id, slug)
  constraint ck_setor_estoque check (ocupados >= 0 and ocupados <= capacidade)

pedido
  id              bigserial PK
  codigo          uuid         not null unique   -- referência pública, não expõe id sequencial
  cliente_id      varchar(255) not null references users(id)
  evento_id       bigint       not null references evento(id)
  status          varchar(20)  not null   -- PENDENTE | PAGO | EXPIRADO | CANCELADO | RECUSADO
  valor_total     numeric(10,2) not null
  expira_em       timestamptz             -- preenchido em PENDENTE, nulo após PAGO
  criado_em       timestamptz  not null default now()
  atualizado_em   timestamptz  not null default now()

pedido_item
  id              bigserial PK
  pedido_id       bigint       not null references pedido(id) on delete cascade
  setor_id        bigint       not null references setor(id)
  quantidade      int          not null check (quantidade > 0)
  preco_unitario  numeric(10,2) not null    -- CONGELADO no momento da reserva
  constraint uq_item_pedido_setor unique (pedido_id, setor_id)

pagamento
  id              bigserial PK
  pedido_id       bigint       not null references pedido(id)
  status          varchar(20)  not null   -- APROVADO | RECUSADO
  motivo          varchar(200)            -- "Cartão recusado pelo emissor (simulado)"
  valor           numeric(10,2) not null
  criado_em       timestamptz  not null default now()

ingresso
  id              bigserial PK
  pedido_id       bigint       not null references pedido(id)
  setor_id        bigint       not null references setor(id)
  codigo          varchar(60)  not null unique   -- conteúdo do QR (ver §7)
  status          varchar(20)  not null   -- VALIDO | UTILIZADO | CANCELADO
  validado_em     timestamptz
  validado_por    varchar(255) references users(id)
  token_publico   uuid         not null unique   -- URL de compartilhamento
  criado_em       timestamptz  not null default now()
```

### Índices

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_evento_titulo_trgm  ON evento USING gin (titulo gin_trgm_ops);
CREATE INDEX idx_evento_status_data  ON evento (status, inicio);
CREATE INDEX idx_evento_cidade       ON evento (cidade);
CREATE INDEX idx_pedido_expiracao    ON pedido (status, expira_em);   -- varredura de expiradas
CREATE INDEX idx_pedido_cliente      ON pedido (cliente_id);
CREATE INDEX idx_ingresso_pedido     ON ingresso (pedido_id);
```

### Migrations (Flyway)

Já criadas e verificadas rodando em banco vazio:

```
V1__users.sql
V2__evento_setor.sql
V3__pedido_item_pagamento.sql
V4__ingresso.sql
V5__indices_e_pg_trgm.sql
V6__seed.sql
```

Migrations novas continuam a partir da **V7**. Nunca editar uma migration já aplicada — o Flyway
valida o checksum e recusa subir.

O seed (`V6`) **não chama API externa nenhuma** — os campos de snapshot vão escritos direto. Seed
que depende de rede é seed que falha na hora errada.

Configuração relevante em `application.properties`:
- `spring.jpa.hibernate.ddl-auto=validate` — o Hibernate nunca altera schema, só confere
- `spring.flyway.baseline-on-migrate=true` — necessário porque a tabela `users` já existia no banco
  local antes do Flyway entrar no projeto. Em banco vazio o Flyway roda normalmente desde a V1
  (verificado em 22/08).

---

## 5. Concorrência: a garantia de não vender duas vezes **[DECIDIDO]**

Este é o requisito de maior peso técnico do desafio. A solução:

### Contador único

`setor.ocupados` é **incrementado já na reserva**, não no pagamento. Reserva pendente segura
estoque; o pagamento apenas muda o estado do pedido e emite os ingressos; expiração e cancelamento
devolvem.

> Rejeitada a alternativa de dois contadores (`reservados` + `vendidos`): parece mais correta e na
> prática só multiplica as transições de estado onde dá para errar.

### Update condicional atômico

```sql
UPDATE setor
   SET ocupados = ocupados + :qtd
 WHERE id = :setorId
   AND ocupados + :qtd <= capacidade
```

Se retornar **0 linhas afetadas**, não havia estoque → aborta a transação e devolve erro de negócio.
Sem `SELECT` antes, sem lock explícito, sem race window.

### Rede de segurança no banco

```sql
constraint ck_setor_estoque check (ocupados >= 0 and ocupados <= capacidade)
```

Mesmo que um bug futuro escreva por outro caminho, o Postgres recusa. É uma linha de DDL que
responde literalmente ao requisito e rende um parágrafo forte de README.

### Ponto único de mutação

**Somente o `BookingService` altera `setor.ocupados`.** Reservar, pagar, cancelar e expirar entram
todos por ele, com `@Transactional`. Um lugar para auditar quando der problema.

### Teste de concorrência (alta prioridade, ~40 linhas)

50 threads comprando os últimos 10 lugares → assert de que **exatamente 10** têm sucesso e
`ocupados == capacidade`. Roda em segundos e é a diferença entre *dizer* que tratou race condition
e *demonstrar*. Poucos candidatos júnior fazem isso.

### Expiração da reserva

- Job agendado (`@Scheduled`, a cada 30s) varrendo `status = 'PENDENTE' AND expira_em < now()`
- **Além disso**, checagem preguiçosa na leitura: pedido pendente vencido é tratado como expirado
  no momento em que alguém o consulta. Assim a demo não depende do scheduler ter rodado no
  minuto certo.
- O job chama **o mesmo método** do cancelamento manual. Uma via só para devolver estoque.
- Com duas instâncias, o `UPDATE ... WHERE status='PENDENTE' AND expira_em < now()` é idempotente:
  quem perder a corrida atualiza 0 linhas.

---

## 6. API externa: catálogo de consulta, não fonte dos eventos **[DECIDIDO]**

**A API externa NÃO é a fonte dos seus eventos.** O enunciado diz: o organizador monta o evento a
partir do catálogo, *definindo data, local, capacidade e preço*. Quem define isso é o organizador.
A API só fornece a **identidade** do show — nome, imagem, gênero.

### Onde a chamada acontece: uma tela, um endpoint

```
Organizador em "Criar evento"
   ↓ digita "metallica"
Front  →  BACKEND PRÓPRIO:  GET /api/catalogo?q=metallica&tipo=SHOW
              ↓
          CatalogoService  →  Ticketmaster Discovery API
              ↓ resposta cacheada por termo (Caffeine, TTL ~1h)
          List<ItemCatalogo> { idExterno, titulo, sinopse, imagemUrl, tipo }
   ↓ organizador escolhe o resultado
   ↓ preenche: 15/09, Allianz Parque, setores (Pista 500 · R$200 / Camarote 80 · R$500)
Front  →  POST /api/eventos { fonte, idExterno, titulo, sinopse, imagemUrl, ...dados próprios }
              ↓
          grava tudo no Postgres
```

**Depois disso, nunca mais.** Home do cliente, filtros, reserva, pagamento, QR, portaria — zero
chamadas externas. Tudo lê do banco próprio.

### Snapshot, não cache

Título, sinopse e imagem viram **coluna própria**, copiadas no momento da criação. Se a Ticketmaster
cair, se a chave estourar cota, se o show sumir do catálogo — a aplicação continua inteira. O único
ponto afetado é a busca do organizador, numa tela visitada duas vezes.

> Contra-exemplo que justifica a escolha: chamar a API na home do cliente transformaria o projeto num
> proxy da Ticketmaster — sem preço próprio, sem capacidade própria, sem estoque para controlar. O
> requisito central (não vender o mesmo lugar duas vezes) simplesmente deixaria de existir.

### Endpoint da Discovery API

Provavelmente `/attractions` serve melhor que `/events`: *attraction* é a atração em si (nome +
imagem + gênero), enquanto *event* é um show específico com data e venue próprios — que seriam
descartados, já que o organizador define os seus. **Confirmar na documentação antes de fixar.**

### Provider local: funciona sem chave de API

`CatalogoProvider` é interface, com duas implementações:

| Condição | Implementação injetada |
|---|---|
| `TICKETMASTER_API_KEY` ausente/vazia | `CatalogoProviderLocal` — ~15 itens fixos com poster |
| `TICKETMASTER_API_KEY` presente | `TicketmasterCatalogoProvider` |

**Por que isso importa muito:** se o avaliador clonar o repo e a tela de criar evento explodir com
401 porque ele não fez cadastro na Ticketmaster, a nota sofre por um motivo que não é do código.
Com o provider local, ele roda o fluxo inteiro em dois minutos — e a abstração fica demonstrada
como real, não decorativa.

README: *"funciona sem chave nenhuma; configure `TICKETMASTER_API_KEY` para usar o catálogo real."*

O TMDb, quando entrar, é a **terceira implementação da mesma interface** — nada mais muda.

### Preparação para filmes (custo quase zero agora)

1. `evento.tipo` = `SHOW | FILME`
2. `evento.fonte` + `evento.id_externo`
3. Campos de snapshot (é o snapshot que desacopla)
4. Interface `CatalogoProvider` com DTO comum `ItemCatalogo`

Campos exclusivos de filme (duração, classificação indicativa) vão em `evento.metadados jsonb`,
sem migration nova. O fluxo de reserva não muda: a sala é um setor com capacidade.

---

## 7. Ingresso, QR e portaria

### Anti-forja **[PROPOSTA — validar na implementação]**

O `ingresso.codigo` é composto de duas partes: um identificador aleatório (UUID/ULID) e uma
assinatura **HMAC-SHA256** desse identificador, com chave secreta da aplicação.

```
codigo = <id-aleatorio> + "." + base64url(HMAC_SHA256(segredo, <id-aleatorio>))
```

O QR carrega essa string. Na portaria, o backend **primeiro confere a assinatura** (rejeita na hora,
sem tocar no banco) e só então busca o ingresso. Sem a chave, forjar um código válido é inviável;
e um ingresso adulterado nem chega a consultar o banco.

### Validação sem duplo uso

Update condicional, mesmo princípio do estoque:

```sql
UPDATE ingresso
   SET status = 'UTILIZADO', validado_em = now(), validado_por = :usuarioId
 WHERE codigo = :codigo AND status = 'VALIDO'
```

0 linhas afetadas → já utilizado (ou cancelado). Nada de `SELECT` seguido de `UPDATE`.

### Os quatro retornos exigidos pelo PDF

| Retorno | Condição |
|---|---|
| `VALIDO` | assinatura ok, ingresso existe, `status = VALIDO`, evento confere |
| `INVALIDO` | assinatura inválida ou código inexistente |
| `JA_UTILIZADO` | ingresso existe mas `status = UTILIZADO` (retornar `validado_em`) |
| `EVENTO_ERRADO` | ingresso válido, mas de outro evento que não o selecionado na portaria |

A tela de portaria deve deixar o operador **selecionar o evento** antes de escanear — é isso que
torna `EVENTO_ERRADO` possível de detectar.

### Link público de compartilhamento

Rota aberta `GET /p/{token_publico}` (UUID, não o `codigo`), sem autenticação, exibindo:
título do evento, data, local, setor e o QR. **Sem nome, e-mail ou qualquer dado pessoal do
comprador.** Manter `token_publico` separado do `codigo` significa que a URL compartilhada não é,
ela mesma, o payload de validação.

### Leitura por câmera

Biblioteca JS de QR no front (`html5-qrcode` ou `jsQR`), com **campo de digitação manual sempre
visível** como alternativa — exigência explícita do PDF, e salva a demo quando a câmera não tem
permissão no navegador do avaliador.

---

## 8. Busca e filtros: `pg_trgm`, não full-text search **[DECIDIDO]**

O problema real é o `LIKE '%texto%'`, que não usa índice B-tree e degrada em tabela grande.

`tsvector`/full-text search resolve **outro** problema (busca por palavras em textos longos) e
trairia justamente a tela principal: o usuário digita `metal` e não acha "Metallica", porque FTS
casa termos inteiros e lematizados.

A solução correta para este caso é `pg_trgm` com índice GIN de trigramas, que:

- **acelera `ILIKE '%texto%'`** — exatamente o padrão que normalmente é catastrófico
- habilita `similarity()`, dando busca tolerante a erro de digitação ("metalica" → "Metallica")

São duas linhas de migration. Filtros da home: termo, cidade, faixa de data, faixa de preço, tipo.

---

## 9. Arquitetura de serviços

Camadas: `controller` (sem regra de negócio) → `service` (`@Transactional`, regra) → `repository`
(só query). DTOs de entrada e saída; entidades JPA não vazam para o controller.

| Serviço | Responsabilidade |
|---|---|
| `AuthService` | registro, login, emissão de JWT, resolução de papel |
| `CatalogoService` | fachada sobre `CatalogoProvider`; cache por termo |
| `EventoService` | CRUD do organizador, publicação, cancelamento, busca e filtros da home |
| `BookingService` | **único ponto que muta `setor.ocupados`**: reservar, confirmar, cancelar, expirar |
| `PagamentoService` | simulação de aprovação/recusa; registra tentativas |
| `IngressoService` | emissão, geração de código assinado, token público, validação da portaria |

Job: `ReservaExpiradaJob` (`@Scheduled`) → delega ao `BookingService`.

**Não criar `SearchService`.** Seria uma classe com um método delegando ao repository. Camada vazia
lê como insegurança, não como organização — a busca é um método do `EventoService` com Specification
ou query única.

### Endpoints (esboço)

```
POST   /api/auth/registrar                 público (cria CLIENTE)
POST   /api/auth/login                     público

GET    /api/eventos                        público  — filtros: q, cidade, de, ate, precoMin, precoMax, tipo; paginado
GET    /api/eventos/{id}                   público  — inclui setores com ocupação
GET    /p/{token}                          público  — página do ingresso compartilhado

GET    /api/catalogo?q=&tipo=              ORGANIZADOR
POST   /api/eventos                        ORGANIZADOR
PUT    /api/eventos/{id}                   ORGANIZADOR
POST   /api/eventos/{id}/publicar          ORGANIZADOR
POST   /api/eventos/{id}/cancelar          ORGANIZADOR
GET    /api/eventos/meus                   ORGANIZADOR
GET    /api/eventos/{id}/ocupacao          ORGANIZADOR

POST   /api/pedidos                        CLIENTE  { eventoId, itens:[{setorId, quantidade}] }
POST   /api/pedidos/{id}/pagamento         CLIENTE  → APROVADO | RECUSADO
DELETE /api/pedidos/{id}                   CLIENTE  (cancela e devolve estoque)
GET    /api/meus-ingressos                 CLIENTE  (agrupados por evento)
POST   /api/ingressos/{id}/compartilhar    CLIENTE  → token público

POST   /api/validacao                      PORTARIA { codigo, eventoId }
```

### Regra de edição de evento

Dados não críticos (início, término, descrição) editáveis **até 7 dias antes** do evento. Depois
disso, a única ação disponível é **cancelar**, devolvendo o valor aos clientes (simulado).

### Pagamento simulado

Precisa ser **determinístico e testável**, para o avaliador conseguir exercitar os dois caminhos.
Sugestão: campo de cartão onde um número específico sempre recusa (ex.: final `0000`), ou um seletor
explícito "aprovar / recusar" na tela de checkout. Documentar no README como forçar cada resultado.

---

## 10. Frontend

React + Vite, sem framework. Telas:

**Cliente:** home com busca e filtros → detalhe do evento (com o diagrama de setores) → checkout →
meus ingressos (agrupados por evento, com QR) → página pública do ingresso compartilhado.
**Organizador:** meus eventos → criar/editar evento (com busca no catálogo) → painel de ocupação.
**Portaria:** seleção do evento → scanner de câmera + campo manual → resultado com os 4 estados.

### O diferencial visual: diagrama de setores em SVG

O risco do modelo por setor é a tela virar grid de card genérico + `<input type="number">` — que é
exatamente o "AI slop" que o PDF manda evitar. O diagrama resolve isso, resolve o painel de ocupação
do organizador e a seleção do cliente, tudo com o mesmo componente.

Não é planta realista: é **planta estilizada** — palco em cima, pista, arquibancada em volta,
camarotes nas laterais. Quatro ou cinco formas.

```jsx
<svg viewBox="0 0 400 300" role="img" aria-label="Mapa de setores">
  {setores.map(s => (
    <path
      key={s.id}
      d={FORMAS[s.slug]}
      fill={corPorOcupacao(s.ocupados / s.capacidade)}
      onClick={() => selecionar(s)}
    />
  ))}
</svg>
```

O que mantém isso genérico: o `setor.slug` no banco (`PISTA`, `ARQUIBANCADA`, `CAMAROTE_A`, `SALA`)
mapeia para uma geometria fixa no front. **Sem editor de plantas, sem coordenadas no banco.**
Setor com slug desconhecido cai num fallback de lista — a tela nunca trava.

Dois cuidados obrigatórios:
- **Cor nunca sozinha.** Sempre com rótulo ao lado (`Pista · restam 160`), senão quem não distingue
  verde de vermelho fica sem informação.
- **Esgotado com hachura cinza**, não vermelho escuro.

---

## 11. Não-objetivos (documentar no README)

O PDF diz querer saber o que foi descartado. Esta lista **conta ponto**:

| Descartado | Por quê |
|---|---|
| Mapa de assentos individuais | O PDF permite um dos dois modelos; o setor cobre os requisitos e o tempo vai para o fluxo completo |
| Redis para holds de reserva | Divide a fonte da verdade; nesta escala `expira_em` + varredura é a decisão correta, não a possível |
| Rate limiting | Invisível na avaliação; o requisito de "otimizar chamadas" é sobre chamadas de **saída**, resolvido por cache/snapshot |
| Cache da lista própria de eventos | Query de centenas de linhas é microssegundos; cachear ocupação desatualizada é dano puro |
| Full-text search (`tsvector`) | Resolve outro problema e piora a busca por prefixo da home; `pg_trgm` é o ajuste certo |
| Meia-entrada | Dois preços dividindo o mesmo estoque quebram o contador único por setor |
| Refresh token | Sessão curta basta para o escopo; complexidade sem retorno visível |
| Nota fiscal, revenda, app nativo, recuperação de senha, e-mail | Explicitamente fora de escopo no PDF |

---

## 12. Ordem de execução (21/08 → 25/08)

**Inegociável primeiro — o fluxo de ponta a ponta:**

1. Schema + migrations Flyway + seed (1 organizador, 2 clientes, 1 portaria, ≥1 evento publicado)
2. Auth com os 3 papéis (já iniciado) + CORS
3. CRUD de evento e setores + `CatalogoProviderLocal`
4. Home do cliente listando eventos + detalhe
5. `BookingService`: reserva com update condicional + expiração
6. Pagamento simulado (aprovar e recusar) + emissão de ingresso
7. Meus ingressos com QR
8. Portaria: validação com os 4 retornos
9. **Deploy** (Vercel + Railway) — fazer cedo, não no último dia
10. README completo + seção de uso de IA

**Depois, nesta ordem de prioridade:**

11. Teste de concorrência (barato, prova o requisito mais crítico)
12. Diagrama SVG de setores
13. Filtros com `pg_trgm`
14. `TicketmasterCatalogoProvider` real
15. Câmera na portaria (se apertar, o campo manual sozinho já atende parcialmente)
16. Docker Compose
17. Filmes / TMDb

## 13. Estrutura do README (obrigatório)

1. O que é o projeto e como rodar (passo a passo, do zero, incluindo o banco)
2. Variáveis de ambiente — e o aviso de que **funciona sem chave de API**
3. Credenciais dos usuários semeados
4. Decisões de arquitetura, com o porquê de cada uma (§3, §5, §6, §8)
5. **Não-objetivos** (§11) — o que foi descartado e por quê
6. O que não está funcionando ou ficou parcial (o PDF diz que a ausência disso *impacta a nota*)
7. **Uso de IA:** quais ferramentas, em que partes, e o que foi feito sem IA
8. Link da aplicação publicada


