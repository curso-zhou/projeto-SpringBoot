package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.model.ColecaoItem;
import br.pucpr.projeto.livros.service.ColecaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colecao")
public class ColecaoController {

    private final ColecaoService service;

    public ColecaoController(ColecaoService service) { this.service = service; }

    public record ItemResponse(Long id, Long livroId, String titulo, String autor, String isbn, LocalDateTime adicionadoEm) {
        public static ItemResponse of(ColecaoItem it) {
            return new ItemResponse(it.getId(), it.getLivro().getId(), it.getLivro().getTitulo(), it.getLivro().getAutor(), it.getLivro().getIsbn(), it.getAdicionadoEm());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            var isbn = body.get("isbn");
            var item = service.adicionarPorIsbn(isbn, auth.getName());
            return ResponseEntity.ok(ItemResponse.of(item));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    @GetMapping
    public List<ItemResponse> list(Authentication auth) {
        return service.listar(auth.getName()).stream().map(ItemResponse::of).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> remove(@PathVariable Long id, Authentication auth) {
        try {
            service.remover(id, auth.getName());
            return ResponseEntity.ok(Map.of("mensagem", "Removido"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }
}
