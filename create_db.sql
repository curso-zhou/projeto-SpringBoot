-- create_db.sql
-- Cria o banco de dados e o usuário usados pelo projeto

CREATE DATABASE IF NOT EXISTS `projeto`
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Cria usuário (ajuste host '%' para 'localhost' se desejar restringir conexões)
CREATE USER IF NOT EXISTS 'projeto'@'%' IDENTIFIED BY 'projeto123';

GRANT ALL PRIVILEGES ON `projeto`.* TO 'projeto'@'%';
FLUSH PRIVILEGES;

-- Seleciona o banco criado para as próximas instruções DDL
USE `projeto`;

-- DDL das tabelas principais (caso deseje criar manualmente; o Hibernate também pode criá-las automaticamente)

-- categorias
CREATE TABLE IF NOT EXISTS categorias (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  UNIQUE KEY uk_categorias_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- users
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  criado_em TIMESTAMP NOT NULL,
  email VARCHAR(180) NOT NULL,
  nome VARCHAR(120) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- livros
CREATE TABLE IF NOT EXISTS livros (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  autor VARCHAR(120) NOT NULL,
  isbn VARCHAR(13),
  preco DECIMAL(12,2) NOT NULL,
  titulo VARCHAR(200) NOT NULL,
  imagem_capa_url VARCHAR(600),
  vendedor_id BIGINT,
  categoria_id BIGINT NOT NULL,
  CONSTRAINT fk_livros_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
  CONSTRAINT fk_livros_vendedor FOREIGN KEY (vendedor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cria índice idx_livros_vendedor se não existir (compatível com várias versões do MySQL)
SET @idx_count := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 'livros' AND index_name = 'idx_livros_vendedor');
SET @sql := IF(@idx_count = 0, 'CREATE INDEX idx_livros_vendedor ON livros(vendedor_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- user_roles (element collection)
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(40) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- colecao_itens (vínculo usuário x livro)
CREATE TABLE IF NOT EXISTS colecao_itens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  livro_id BIGINT NOT NULL,
  adicionado_em TIMESTAMP NOT NULL,
  CONSTRAINT uk_colecao_usuario_livro UNIQUE (usuario_id, livro_id),
  CONSTRAINT fk_colecao_usuario FOREIGN KEY (usuario_id) REFERENCES users(id),
  CONSTRAINT fk_colecao_livro FOREIGN KEY (livro_id) REFERENCES livros(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices auxiliares para consultas (compatível com versões que não suportam IF NOT EXISTS)
SET @idx_count := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 'colecao_itens' AND index_name = 'idx_colecao_usuario_adicionado_em');
SET @sql := IF(@idx_count = 0, 'CREATE INDEX idx_colecao_usuario_adicionado_em ON colecao_itens(usuario_id, adicionado_em)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_count := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 'colecao_itens' AND index_name = 'idx_colecao_livro');
SET @sql := IF(@idx_count = 0, 'CREATE INDEX idx_colecao_livro ON colecao_itens(livro_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Categoria padrão (usada como fallback no fluxo de importação por ISBN)
INSERT IGNORE INTO categorias (nome) VALUES ('Importados');
