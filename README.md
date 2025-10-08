# Projeto Spring Boot

API simples de autenticação (registro e login) com H2 em memória e um terminal interativo opcional.

## Requisitos
- JDK 21 (toolchain configurada)
- Gradle Wrapper (já incluso)

## Executar testes
```
./gradlew test
```

## Rodar aplicação
```
./gradlew bootRun
```
Console H2: http://localhost:8080/h2 (JDBC URL: jdbc:h2:mem:projeto)

## Endpoints
| Método | URL                 | Descrição          |
|--------|---------------------|--------------------|
| POST   | /api/auth/register  | Registrar usuário  |
| POST   | /api/auth/login     | Login (credenciais)|

### Exemplo JSON registro
```json
{
	"nome": "João da Silva",
	"email": "joao@example.com",
	"senha": "segredo123"
}
```

### Exemplo JSON login
```json
{
	"email": "joao@example.com",
	"senha": "segredo123"
}
```

## Interface simples (HTML)
Uma página estática `index.html` foi adicionada (em `src/main/resources/static`) com formulários de Registro e Login.

### Como usar
1. Inicie a aplicação:
```
./gradlew bootRun
```
2. Acesse: http://localhost:8080/
3. Preencha o formulário de Registro. O resultado aparecerá na área de resposta.
4. Faça Login com o mesmo email e senha para validar.

As requisições são feitas via Fetch API para os endpoints `/api/auth/register` e `/api/auth/login`.

## Próximos Passos (idéias)
- Adicionar JWT em vez de login simples
- Endpoint para usuário atual
- Perfis separados (dev/test)
- Cobertura de testes de serviço

