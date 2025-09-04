# Microsserviço de Carrinho de Compras (Shopping Cart)

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)
![Maven](https://img.shields.io/badge/Maven-4.0-orange.svg)
![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)

## 📖 Sobre o Projeto

Este é o **Microsserviço de Carrinho de Compras**, parte de um sistema de e-commerce distribuído. Desenvolvido em Java com o ecossistema Spring, a sua principal responsabilidade é gerir os carrinhos de compras temporários dos utilizadores.

Este serviço é um cliente do `auth-service` (Go), delegando a ele a validação de tokens JWT para garantir que apenas utilizadores autenticados possam gerir os seus carrinhos. Ele também depende de um serviço de Produtos para consultar informações dos itens.

### ✨ Funcionalidades Principais
* Adicionar itens a um carrinho de compras.
* Visualizar o conteúdo completo do carrinho.
* Limpar todos os itens do carrinho.
* Validação de token JWT via comunicação com o `auth-service`.
* Persistência temporária dos carrinhos em Redis.

## 🛠️ Arquitetura e Tecnologias

O projeto segue uma arquitetura em camadas para uma clara separação de responsabilidades.

### Tecnologias Utilizadas
* **Linguagem:** Java 17+
* **Framework:** Spring Boot
* **Persistência:** Redis (via Spring Data Redis)
* **Build:** Maven
* **Containerização:** Docker & Docker Compose
* **Utilitários:** Lombok

### Estrutura de Diretórios

📦 shopping-cart-service/
├── src/main/java/com/seu_dominio/shoppingcartservice/
│   ├── config/             # Configurações do Spring (ex: WebConfig para interceptors)
│   ├── controller/         # Camada de API (Endpoints REST)
│   ├── domain/             # Entidades principais (Cart, CartItem)
│   ├── dto/                # Data Transfer Objects (para requests/responses)
│   ├── repository/         # Camada de acesso a dados (Redis)
│   ├── security/           # Interceptor para validação de JWT
│   └── service/            # Camada de lógica de negócio
├── src/main/resources/
│   └── application.properties # Ficheiro de configuração
└── pom.xml                 # Definições do projeto Maven

## 📜 Contratos da API

Todos os endpoints abaixo requerem um cabeçalho de autenticação: `Authorization: Bearer <seu-token-jwt>`

### `POST /cart`
* **Descrição:** Adiciona um novo item ao carrinho do utilizador autenticado. Se o item já existir, atualiza a sua quantidade.
* **Corpo da Requisição:**
    ```json
    {
      "productId": "string",
      "quantity": int
    }
    ```
* **Resposta de Sucesso:** `200 OK` com o objeto `Cart` atualizado.

### `GET /cart`
* **Descrição:** Retorna o conteúdo completo do carrinho do utilizador autenticado.
* **Resposta de Sucesso:** `200 OK` com o objeto `Cart`.

### `DELETE /cart`
* **Descrição:** Esvazia o carrinho do utilizador autenticado.
* **Resposta de Sucesso:** `204 No Content`.

## 🚀 Como Executar o Projeto

Este serviço foi desenhado para ser executado como parte de um ambiente Docker Compose junto com os outros microsserviços do e-commerce.

### Pré-requisitos
* Java (JDK 17+)
* Maven
* Docker e Docker Compose

### Passo a Passo
1.  **Clone o repositório.**

2.  **Configure o `application.properties`:**
    Garanta que as URLs para os outros serviços e a conexão com o Redis estão corretas.
    ```properties
    server.port=8082
    spring.data.redis.host=redis
    spring.data.redis.port=6379
    service.auth.url=http://auth-app:8081/auth/validate
    service.internal.api-key=${INTERNAL_API_KEY}
    ```

3.  **Atualize o `docker-compose.yml` Principal:**
    No `docker-compose.yml` da raiz do seu e-commerce, adicione os serviços para o `shopping-cart-service` e o `redis`.
    ```yaml
    services:
      # ... (serviços do auth-service)

      # Serviço do Carrinho de Compras (Java)
      shopping-cart-app:
        build: ./shopping-cart-service # Caminho para a pasta do projeto Java
        container_name: shopping-cart-app
        ports:
          - "8082:8082"
        env_file:
          - ./.env # Partilha o mesmo .env
        depends_on:
          - auth-app
          - redis
        restart: always

      # Serviço do Redis
      redis:
        image: redis:7-alpine
        container_name: redis
        restart: always

4.  **Execute o Ambiente Completo:**
    A partir da pasta raiz que contém o `docker-compose.yml`, execute:
    ```bash
    docker-compose up --build
    ```
    Isto irá construir e iniciar todos os seus microsserviços (Go e Java) juntos. O seu `cart-service` estará acessível em `http://localhost:8082`.