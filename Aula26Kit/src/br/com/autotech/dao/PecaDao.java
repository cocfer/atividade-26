package br.com.autotech.dao;

import br.com.autotech.factory.ConexaoFactory;
import br.com.autotech.model.Peca;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de peças (AutoTech) - persistência local em SQLite.
 * Todas as operações usam try-with-resources e PreparedStatement parametrizado.
 */
public class PecaDao {

    private static final String COLUNAS = "id, codigo, nome, preco, estoque";

    /** Missão 1 - DDL automático. */
    public void criarTabelaSeNaoExistir() {
        final String ddl = "CREATE TABLE IF NOT EXISTS pecas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "codigo TEXT UNIQUE NOT NULL, "
                + "nome TEXT NOT NULL, "
                + "preco REAL NOT NULL, "
                + "estoque INTEGER NOT NULL);";

        try (Connection conn = ConexaoFactory.obterConexao();
             Statement st = conn.createStatement()) {
            st.execute(ddl);
            System.out.println("🔧 [AutoTech] Tabela 'pecas' verificada/criada com sucesso!");
        } catch (SQLException e) {
            System.err.println("❌ Erro na Missão 1 (criarTabela): " + e.getMessage());
        }
    }

    /** Missão 2 - INSERT com resgate da chave primária gerada. */
    public Peca salvar(Peca peca) {
        final String sql = "INSERT INTO pecas (codigo, nome, preco, estoque) VALUES (?, ?, ?, ?);";

        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, peca.getCodigo());
            ps.setString(2, peca.getNome());
            ps.setDouble(3, peca.getPreco());
            ps.setInt(4, peca.getEstoque());

            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        peca.setId(keys.getLong(1));
                    }
                }
                System.out.printf("✅ Peça '%s' gravada com ID %d%n", peca.getCodigo(), peca.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro na Missão 2 (salvar): " + e.getMessage());
        }
        return peca;
    }

    /** Missão 3 - SELECT geral ordenado por nome. */
    public List<Peca> listarTodas() {
        final String sql = "SELECT " + COLUNAS + " FROM pecas ORDER BY nome;";
        List<Peca> resultado = new ArrayList<>();

        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro na Missão 3 (listarTodas): " + e.getMessage());
        }
        return resultado;
    }

    /** Missão 4 - SELECT filtrado pelo código único. */
    public Peca buscarPorCodigo(String codigo) {
        final String sql = "SELECT " + COLUNAS + " FROM pecas WHERE codigo = ?;";

        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro na Missão 4 (buscarPorCodigo): " + e.getMessage());
            return null;
        }
    }

    /** Missão 5 - UPDATE que impede estoque negativo. */
    public boolean darBaixaEstoque(Long id, int quantidadeRetirada) {
        if (id == null || quantidadeRetirada <= 0) {
            return false;
        }
        final String sql = "UPDATE pecas SET estoque = estoque - ? WHERE id = ? AND estoque >= ?;";

        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantidadeRetirada);
            ps.setLong(2, id);
            ps.setInt(3, quantidadeRetirada);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro na Missão 5 (darBaixaEstoque): " + e.getMessage());
            return false;
        }
    }

    private Peca mapear(ResultSet rs) throws SQLException {
        return new Peca(rs.getLong("id"), rs.getString("codigo"), rs.getString("nome"),
                rs.getDouble("preco"), rs.getInt("estoque"));
    }
}
