package br.pucpr.projeto.livros;

import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LivroControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    LivroRepository livroRepository;

    @AfterEach
    void cleanup() {
        livroRepository.deleteAll();
        categoriaRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Vendedor consegue criar e editar seu próprio livro")
    void vendedorCreateAndEditOwnBook() throws Exception {
        // cria categoria
        var cat = categoriaRepository.save(new br.pucpr.projeto.livros.model.Categoria("Teste"));

        // registra vendedor
        String reg = objectMapper.writeValueAsString(java.util.Map.of(
                "nome", "Vendedor Teste",
                "email", "vend1@example.com",
                "senha", "senha123",
                "vendedor", true
        ));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg)).andExpect(status().isCreated());

        // login
        String login = objectMapper.writeValueAsString(java.util.Map.of(
                "email", "vend1@example.com",
                "senha", "senha123"
        ));
        var loginResp = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andReturn();
        var token = objectMapper.readTree(loginResp.getResponse().getContentAsString()).get("token").asText();

        // cria livro
        var livroPayload = objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", "Livro do Vendedor",
                "autor", "Autor X",
                "categoriaId", cat.getId(),
                "preco", 39.90,
                "isbn", "ISBN-1111"
        ));

        var createResp = mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(livroPayload).header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Livro do Vendedor"))
                .andReturn();

        var id = objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asLong();

        // edita livro com mesmo vendedor
        var updatePayload = objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", "Livro Editado",
                "autor", "Autor X",
                "categoriaId", cat.getId(),
                "preco", 45.00,
                "isbn", "ISBN-1111"
        ));

        mockMvc.perform(put("/api/livros/" + id).contentType(MediaType.APPLICATION_JSON).content(updatePayload).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Livro Editado"));
    }

    @Test
    @DisplayName("Usuário que não é dono não consegue editar livro de outro vendedor")
    void nonOwnerCannotEdit() throws Exception {
        var cat = categoriaRepository.save(new br.pucpr.projeto.livros.model.Categoria("Teste2"));

        // registra vendedor A e cria livro
        String regA = objectMapper.writeValueAsString(java.util.Map.of(
                "nome", "Vendedor A",
                "email", "vendA@example.com",
                "senha", "senha123",
                "vendedor", true
        ));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(regA)).andExpect(status().isCreated());
        String loginA = objectMapper.writeValueAsString(java.util.Map.of("email", "vendA@example.com", "senha", "senha123"));
        var tokenAResp = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginA)).andExpect(status().isOk()).andReturn();
        var tokenA = objectMapper.readTree(tokenAResp.getResponse().getContentAsString()).get("token").asText();

        var livroPayload = objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", "Livro A",
                "autor", "Autor A",
                "categoriaId", cat.getId(),
                "preco", 10.00,
                "isbn", "ISBN-A"
        ));
        var createResp = mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(livroPayload).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated()).andReturn();
        var id = objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asLong();

        // registra usuário B (não vendedor)
        String regB = objectMapper.writeValueAsString(java.util.Map.of(
                "nome", "Usuario B",
                "email", "userB@example.com",
                "senha", "senha123"
        ));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(regB)).andExpect(status().isCreated());
        var tokenBResp = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(java.util.Map.of("email", "userB@example.com", "senha", "senha123"))))
                .andExpect(status().isOk()).andReturn();
        var tokenB = objectMapper.readTree(tokenBResp.getResponse().getContentAsString()).get("token").asText();

        var updatePayload = objectMapper.writeValueAsString(java.util.Map.of(
                "titulo", "Tentativa Edit",
                "autor", "Autor A",
                "categoriaId", cat.getId(),
                "preco", 11.00,
                "isbn", "ISBN-A"
        ));

        mockMvc.perform(put("/api/livros/" + id).contentType(MediaType.APPLICATION_JSON).content(updatePayload).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }
}
