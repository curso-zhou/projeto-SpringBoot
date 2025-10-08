package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.LivroRequest;
import br.pucpr.projeto.livros.dto.LivroResponse;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository livros;
    private final CategoriaRepository categorias;

    public LivroController(LivroRepository livros, CategoriaRepository categorias) {
        this.livros = livros; this.categorias = categorias;
    }

    @GetMapping
    public List<LivroResponse> list() {
        return livros.findAll().stream().map(l -> new LivroResponse(
                l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria().getId(), l.getCategoria().getNome(), l.getPreco(), l.getIsbn()
        )).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponse> get(@PathVariable Long id) {
        return livros.findById(id)
                .map(l -> ResponseEntity.ok(new LivroResponse(l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria().getId(), l.getCategoria().getNome(), l.getPreco(), l.getIsbn())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LivroResponse> create(@Valid @RequestBody LivroRequest request) {
        var categoria = categorias.findById(request.categoriaId()).orElse(null);
        if (categoria == null) return ResponseEntity.unprocessableEntity().build();
        var livro = livros.save(new br.pucpr.projeto.livros.model.Livro(
                request.titulo(), request.autor(), categoria, request.preco(), request.isbn()
        ));
        var resp = new LivroResponse(livro.getId(), livro.getTitulo(), livro.getAutor(), categoria.getId(), categoria.getNome(), livro.getPreco(), livro.getIsbn());
        return ResponseEntity.created(URI.create("/api/livros/" + livro.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponse> update(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
        var opt = livros.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var livro = opt.get();
        var categoria = categorias.findById(request.categoriaId()).orElse(null);
        if (categoria == null) return ResponseEntity.unprocessableEntity().build();
        livro.setTitulo(request.titulo());
        livro.setAutor(request.autor());
        livro.setCategoria(categoria);
        livro.setPreco(request.preco());
        livro.setIsbn(request.isbn());
        livros.save(livro);
        var resp = new LivroResponse(livro.getId(), livro.getTitulo(), livro.getAutor(), categoria.getId(), categoria.getNome(), livro.getPreco(), livro.getIsbn());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!livros.existsById(id)) return ResponseEntity.notFound().build();
        livros.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
