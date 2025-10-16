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
| POST   | /api/colecao/add    | Adicionar livro por ISBN (usuário)
| GET    | /api/colecao        | Listar minha coleção
| DELETE | /api/colecao/{id}   | Remover item da coleção

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
 - Início (com coleção e adicionar ISBN): http://localhost:8080/home.html

### Como usar
1. Inicie a aplicação:
```
./gradlew.bat bootRun

SE DER ERRO:
# opção 1 - passar argumento
./gradlew.bat bootRun --args='--spring.profiles.active=mysql'

# opção 2 - setar variável de ambiente na sessão atual
$env:SPRING_PROFILES_ACTIVE='mysql'
./gradlew.bat bootRun
```
2. Acesse: http://localhost:8080/
3. Preencha o formulário de Registro. O resultado aparecerá na área de resposta.
4. Faça Login com o mesmo email e senha para validar.

Na página Home (http://localhost:8080/home.html), após logar:
- Use o campo "Adicionar por ISBN" para incluir um livro automaticamente na sua coleção.
- Clique em "Minha Coleção" (já é carregada automaticamente) para visualizar os itens.

As requisições são feitas via Fetch API para os endpoints `/api/auth/register` e `/api/auth/login`.

### Preço dos livros
- Ao adicionar por ISBN, o sistema tenta obter o preço automaticamente.
- Fallback atual: Google Books (saleInfo) em moeda BRL. Se houver valor, ele será exibido; caso contrário, a UI mostra “Fora de estoque”.
- Integração com Amazon Brasil (PA-API) não está habilitada no momento. Para ativar, é necessário informar credenciais válidas da Amazon Product Advertising API (Partner Tag, Access Key e Secret Key) e habilitar a integração no serviço de preços. Podemos implementar isso assim que você disponibilizar as credenciais.

## Próximos Passos (idéias)
- Melhorar responsividade e animações das telas
- Adicionar página de recuperação de senha
- Página de perfil com edição mais rica
- Cobertura de testes de serviço

## Usando MySQL (local via Docker) 🐬

Criamos um profile `mysql` para executar a aplicação contra um banco MySQL.

1. Suba o banco com Docker Compose (requer Docker):

```powershell
docker-compose up -d
```

2. Inicie a aplicação usando o profile `mysql`:

```powershell
./gradlew.bat bootRun --args='--spring.profiles.active=mysql'
```

As credenciais configuradas no `docker-compose.yml` e em `src/main/resources/application-mysql.properties` são:

- banco: `projeto`
- usuário: `projeto`
- senha: `projeto123`

Se quiser conectar um MySQL externo, altere a URL/usuário/senha em `application-mysql.properties` ou defina variáveis de ambiente.

