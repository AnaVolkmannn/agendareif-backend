# agendareif-backend

## Dependências necessárias para rodar o projeto localmente

- Dbeaver ou pgadmin (interface gráfica do postgres)
- Docker
- Maven
- Java 21

## Para rodar o projeto pela primeira vez

- docker compose up -d (precisa estar com docker aberto, isso para subir o banco de dados localmente)
- mvn clean install (para rodar os testes e compilar o projeto)
- mvn spring-boot:run

- Para acessar o swagger para testar as rotas, com o projeto rodando, abrir no navegador:
  http://localhost:8081/swagger-ui/index.html#/
