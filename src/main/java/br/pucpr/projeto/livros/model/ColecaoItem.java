package br.pucpr.projeto.livros.model;

import br.pucpr.projeto.auth.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "colecao_itens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_colecao_usuario_livro", columnNames = {"usuario_id", "livro_id"})
})
public class ColecaoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id")
    private Livro livro;

    @Column(nullable = false)
    private LocalDateTime adicionadoEm = LocalDateTime.now();

    protected ColecaoItem() {}
    public ColecaoItem(User usuario, Livro livro) {
        this.usuario = usuario;
        this.livro = livro;
    }

    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public Livro getLivro() { return livro; }
    public LocalDateTime getAdicionadoEm() { return adicionadoEm; }
}
