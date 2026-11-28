# Auto Center Silva — Deploy automatizado na AWS EC2

E-commerce da oficina **Auto Center Silva**: front-end Angular 19, API Spring Boot 3.2 (Java 17, JWT) e PostgreSQL,
publicados em **uma única instância EC2 gratuita** (Ubuntu 22.04/24.04, `t2.micro`/`t3.micro`) por meio de um
script de Infraestrutura como Código: [`deploy.sh`](./deploy.sh).

## Sobre a aplicação

O **Auto Center Silva** é uma plataforma web completa de e-commerce e gestão voltada para uma oficina automotiva especializada na venda de pneus. A aplicação atende tanto aos clientes finais — permitindo pesquisa de produtos por medidas, gerenciamento de carrinho, consulta de cotação de moedas em tempo real, realização de pedidos (com reserva temporária de itens) e acompanhamento do histórico de compras — quanto aos administradores da loja, oferecendo um painel restrito para controle de pedidos, cancelamentos e movimentação de estoque.

O sistema segue uma arquitetura full-stack moderna dividida em três camadas:
* **Front-end:** Single Page Application (SPA) desenvolvida em **Angular 19** (TypeScript, componentes standalone, MDB UI Kit e SweetAlert2), com rotas protegidas por perfil de acesso (`CLIENTE` e `ADMIN`) e interceptadores JWT.
* **Back-end:** API RESTful construída com **Java 17** e **Spring Boot 3.2.5**, utilizando **Spring Security** com autenticação stateless via **JWT**, integração externa via **OpenFeign** (para cotação de moedas) e agendamento de tarefas para expiração automática de reservas de estoque.
* **Banco de Dados:** **PostgreSQL** gerenciado com versionamento de schema via **Flyway** (14 migrations), utilizando extensões como `pg_trgm` e `unaccent` para otimizar as buscas no catálogo.

## Arquitetura

```
Internet ──► :80 ──► Nginx ──┬─ /            → Angular (build de produção, fallback SPA: try_files … /index.html)
                             ├─ /api/        → proxy → Spring Boot 127.0.0.1:8080  (serviço systemd)
                             └─ /cotacao/    → proxy → Spring Boot 127.0.0.1:8080
                                                         │
                                                         └─► PostgreSQL 127.0.0.1:5432  (inacessível externamente)
```

| Camada | Tecnologia | Como roda |
|---|---|---|
| Front-end | Angular 19 (Node 22) | arquivos estáticos em `/var/www/autocenter`, servidos pelo Nginx |
| Proxy / web | Nginx | porta 80; mesma origem para front e API (sem CORS em produção) |
| Back-end | Spring Boot 3.2.5, Java 17 | serviço `systemd` `autocenter-backend`, reinício automático |
| Banco | PostgreSQL (22.04→14, 24.04→16) | somente `localhost`; Flyway cria as tabelas no 1º start |

## Pré-requisitos

- Instância EC2 Ubuntu 22.04 ou 24.04 (`t2.micro`/`t3.micro`), disco de **15–20 GB**.
- Security Group com **apenas** `22/tcp` (SSH) e `80/tcp` (HTTP). **Não** abra 5432 nem 8080.
- O código-fonte, por uma destas formas (o script detecta sozinho):

| Opção | O que fazer |
|---|---|
| **A. Git** | `export BACKEND_REPO=https://github.com/USUARIO/backend-ecommerce-v02.git FRONTEND_REPO=https://github.com/USUARIO/frontend-ecommerce-v02.git` |
| **B. ZIP** (padrão do projeto) | enviar `backend-ecommerce-v02-main.zip` e `frontend-ecommerce-v02-main.zip` para `/home/ubuntu` |
| **C. Pastas** | já deixar os projetos em `/opt/autocenter/src/backend` e `/opt/autocenter/src/frontend` |

## Como executar

```bash
# na EC2, na pasta onde estão deploy.sh (e os .zip, se usar a opção B)
chmod +x deploy.sh
./deploy.sh                  # opção B (zips)
# ou, com Git:
BACKEND_REPO=<url> FRONTEND_REPO=<url> ./deploy.sh
```

Tempo típico: 10–25 min na `t2.micro` (o build do Maven e do Angular é a parte lenta).
Ao final o script executa testes de saúde e imprime a URL: `http://<IP-PÚBLICO>/`.

O script é **idempotente**: pode ser executado de novo (atualiza o código, refaz o build e reinicia o serviço)
sem recriar o banco nem trocar senhas.

### Subcomandos

```bash
./deploy.sh status                    # estado dos serviços + testes de saúde
./deploy.sh promover-admin 45999990000  # torna um cliente já cadastrado ADM (telefone só com dígitos)
```

> O sistema não cria ADM automaticamente. Cadastre-se pelo site e depois rode `promover-admin`;
> faça logout/login para o novo perfil valer.

### Variáveis opcionais

| Variável | Efeito |
|---|---|
| `DOMINIO` | inclui o domínio no CORS e no `server_name` do Nginx |
| `WHATSAPP_NUMERO` | número usado no link de confirmação do pedido |
| `SWAP_GB` | tamanho do swap (padrão `2`) |
| `SKIP_UPGRADE=1` | pula o `apt upgrade` |
| `DB_PASSWORD`, `JWT_SECRET` | usar valores próprios na 1ª execução (senão são gerados) |

## O que o `deploy.sh` faz

1. Verifica Ubuntu, RAM e disco.
2. `apt update && apt upgrade` e instala utilitários.
3. Cria **swap de 2 GB** (evita *out of memory* nos builds com 1 GB de RAM).
4. Instala **OpenJDK 17**, **Maven**, **Node.js 22**, **Angular CLI 19**, **Nginx** e **PostgreSQL**.
5. Gera `DB_PASSWORD` e `JWT_SECRET` aleatórios e grava em `/etc/autocenter/autocenter.env` (`root:root`, modo `600`).
6. Cria o usuário `autocenter` e o banco `bdAutoCenterSilva` no PostgreSQL (+ extensões `pg_trgm` e `unaccent` exigidas pela migration V1).
7. Compila o back-end (`mvn package`) e instala o `.jar` em `/opt/autocenter/backend`.
8. Cria o serviço **systemd** `autocenter-backend` (`Restart=always`, inicia no boot, sandbox de segurança).
9. Compila o Angular em modo produção e publica em `/var/www/autocenter`.
10. Configura o **Nginx** (SPA + proxy `/api` e `/cotacao`) e o firewall UFW (só 22 e 80).
11. Valida a aplicação (HTTP 200 no site, no fallback SPA e na API; **401** nas rotas protegidas sem token).

## Segurança

- **Nenhum segredo no Git nem no script.** Senha do banco e JWT secret vivem só em `/etc/autocenter/autocenter.env`
  (lido pelo systemd via `EnvironmentFile`). O back-end **não inicia** sem `DB_PASSWORD` e `JWT_SECRET` (≥ 32 bytes).
- **PostgreSQL inacessível externamente:** `listen_addresses='localhost'`, Security Group sem a 5432 e UFW bloqueando.
- O Spring Boot escuta só em `127.0.0.1:8080`; todo o tráfego público passa pelo Nginx.
- **Front sem `localhost`:** o Angular usa URLs relativas (`/api/...`); o Nginx faz o proxy, então front e API
  ficam na mesma origem. `CORS_ALLOWED_ORIGINS` é preenchido com o IP/domínio público.
- **JWT:** `authInterceptor` envia `Authorization: Bearer` e trata 401; `authGuard`/`perfilGuard` protegem as rotas
  do Angular; o Spring Security responde **401** (sem/ com token inválido) e **403** (perfil sem permissão).
- Swagger desabilitado em produção (`SWAGGER_ENABLED=false`).

## Operação

```bash
sudo journalctl -u autocenter-backend -f              # logs do back-end em tempo real
sudo journalctl -u autocenter-backend -n 100 --no-pager
sudo tail -f /var/log/nginx/access.log /var/log/nginx/error.log
sudo systemctl status autocenter-backend nginx postgresql
sudo systemctl restart autocenter-backend
cat /var/log/autocenter-deploy.log                    # log completo do último deploy
```

## Problemas comuns

| Sintoma | Causa provável / solução |
|---|---|
| `502 Bad Gateway` logo após o deploy | O Spring ainda está iniciando (30–90 s). Acompanhe com `journalctl -u autocenter-backend -f`. |
| Serviço reinicia em loop | Veja `journalctl`. Ex.: `Could not resolve placeholder 'DB_PASSWORD'` → arquivo `/etc/autocenter/autocenter.env` ausente/incompleto. |
| Build falha com `Killed` / `OutOfMemory` | Confira `free -h`. O swap precisa estar ativo (`swapon --show`); rode `./deploy.sh` de novo. |
| Site não abre no navegador | Security Group sem a porta 80, ou a instância sem IP público. |
| `Invalid CORS request` após parar/iniciar a instância | O IP público mudou. Rode `./deploy.sh` (atualiza o CORS) ou associe um **Elastic IP**. |
| Rota de admin devolve 403 | O usuário é `CLIENTE`. Use `./deploy.sh promover-admin <telefone>`. |

## Custos

Pare (**Stop**) ou encerre (**Terminate**) a instância após a apresentação e libere Elastic IPs não usados.
Confira os limites do nível gratuito no painel **Billing**.

## Estrutura dos projetos analisados

- `backend-ecommerce-v02-main/demo` — Spring Boot 3.2.5, Java 17, Maven, Flyway (V1–V14), JWT (jjwt 0.12.6), OpenFeign.
- `frontend-ecommerce-v02-main` — Angular 19.2, standalone components, MDB UI Kit, SweetAlert2.
  `deploy/nginx-autocenter.conf` é a referência da configuração do Nginx (o `deploy.sh` já a gera).
