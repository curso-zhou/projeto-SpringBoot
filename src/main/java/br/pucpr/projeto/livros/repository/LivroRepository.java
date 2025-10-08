package br.pucpr.projeto.livros.repository;

import br.pucpr.projeto.livros.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
