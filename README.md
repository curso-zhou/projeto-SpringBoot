# Alexandria — Projeto Spring Boot

API simples de autenticação (registro e login) com H2 em memória e uma interface estática com visual aconchegante em tons terrosos.

## Requisitos
- JDK 21 (toolchain configurada)
- Gradle Wrapper (já incluso)

## Executar testes
```powershell
./gradlew.bat test
```

## Rodar aplicação
```powershell
./gradlew.bat bootRun
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

## Interface (HTML)
As páginas estáticas ficam em `src/main/resources/static`.

- Página principal (demo): http://localhost:8080/
- Login: http://localhost:8080/login.html
- Registro: http://localhost:8080/register.html

### Como usar
1. Inicie a aplicação:
```
./gradlew.bat bootRun
```
2. Acesse: http://localhost:8080/
3. Preencha o formulário de Registro. O resultado aparecerá na área de resposta.
4. Faça Login com o mesmo email e senha para validar.

As requisições são feitas via Fetch API para os endpoints `/api/auth/register` e `/api/auth/login`.

## Próximos Passos (idéias)
- Melhorar responsividade e animações das telas
- Adicionar página de recuperação de senha
- Página de perfil com edição mais rica
- Cobertura de testes de serviço

