package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.LivroRequest;
import br.pucpr.projeto.livros.dto.LivroResponse;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.repository.ColecaoItemRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/livros")
public class AdminLivroController {
    private final LivroRepository livroRepository;
    private final CategoriaRepository categoriaRepository;
    private final ColecaoItemRepository colecaoItemRepository;

    public AdminLivroController(LivroRepository livroRepository, CategoriaRepository categoriaRepository, ColecaoItemRepository colecaoItemRepository) {
        this.livroRepository = livroRepository;
        this.categoriaRepository = categoriaRepository;
        this.colecaoItemRepository = colecaoItemRepository;
    }

    @GetMapping
    public ResponseEntity<Object> list() {
        var list = livroRepository.findAll().stream().map(l -> new LivroResponse(
                l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria().getId(), l.getCategoria().getNome(), l.getPreco(), l.getIsbn(), l.getImagemCapaUrl()
        )).toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody LivroRequest req) {
        var opt = livroRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var livro = opt.get();
        var categoria = categoriaRepository.findById(req.categoriaId()).orElse(null);
        if (categoria == null) return ResponseEntity.unprocessableEntity().build();
        livro.setTitulo(req.titulo());
        livro.setAutor(req.autor());
        livro.setCategoria(categoria);
        livro.setPreco(req.preco());
        livro.setIsbn(req.isbn());
        if (req.imagemCapaUrl() != null && !req.imagemCapaUrl().isBlank()) {
            livro.setImagemCapaUrl(req.imagemCapaUrl());
        } else {
            livro.setImagemCapaUrl(null);
        }
        livroRepository.save(livro);
        var resp = new LivroResponse(livro.getId(), livro.getTitulo(), livro.getAutor(), categoria.getId(), categoria.getNome(), livro.getPreco(), livro.getIsbn(), livro.getImagemCapaUrl());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Object> deleteLivro(@PathVariable Long id, @RequestParam(name = "force", defaultValue = "false") boolean force) {
        var livro = livroRepository.findById(id);
        if (livro.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Evita deletar livro que esteja na coleção de algum usuário, a menos que force=true
        if (colecaoItemRepository.existsByLivro_Id(id)) {
            if (!force) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("erro", "Livro está na coleção de usuários e não pode ser removido. Use force=true para remoção forçada."));
            }
            colecaoItemRepository.deleteAllByLivroId(id);
        }
        livroRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Object> deleteAll(@RequestParam(name = "force", defaultValue = "false") boolean force) {
        if (!force) {
            // Verifica se há algum livro em uso
            var emUso = livroRepository.findAll().stream().anyMatch(l -> colecaoItemRepository.existsByLivro_Id(l.getId()));
            if (emUso) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("erro", "Existem livros na coleção de usuários. Use force=true para remoção em massa."));
            }
        } else {
            // Remove vínculos de todos os livros antes de remover
            for (var l : livroRepository.findAll()) {
                colecaoItemRepository.deleteAllByLivroId(l.getId());
            }
        }
        livroRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
