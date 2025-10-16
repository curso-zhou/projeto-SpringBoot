package br.pucpr.projeto.livros.repository;

import br.pucpr.projeto.livros.model.ColecaoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColecaoItemRepository extends JpaRepository<ColecaoItem, Long> {
    @Query("select ci from ColecaoItem ci where ci.usuario.id = :userId order by ci.adicionadoEm desc")
    List<ColecaoItem> findByUsuarioOrderByData(@Param("userId") Long userId);

    @Query("select ci from ColecaoItem ci where ci.usuario.id = :userId and ci.livro.id = :livroId")
    Optional<ColecaoItem> findByUsuarioAndLivro(@Param("userId") Long userId, @Param("livroId") Long livroId);

    @Query("select ci from ColecaoItem ci where ci.usuario.id = :userId and ci.livro.isbn = :isbn")
    Optional<ColecaoItem> findByUsuarioAndIsbn(@Param("userId") Long userId, @Param("isbn") String isbn);
}
