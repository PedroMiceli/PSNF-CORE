package com.psnf.api.psnf.domain.usuario.model;

import com.psnf.api.psnf.domain.shared.model.BaseDomain;
import com.psnf.api.psnf.domain.usuario.exception.EnderecoNaoEncontradoException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Usuário do marketplace. Pode atuar como comprador e/ou vendedor.
 * <p>
 * Anúncios, compras e vendas NÃO ficam carregados aqui (evita ciclos e carregamento pesado);
 * eles são consultados pelos seus próprios casos de uso (ex.: ListarAnunciosPorVendedor, ListarOrdensDoUsuario).
 */
public class Usuario extends BaseDomain {

    private String nome;
    private final String email;
    private final String senha;
    private final String secret2fa;
    private String imagem;
    private String telefone;
    private final List<Endereco> enderecos;

    private Usuario(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String nome, String email, String senha, String secret2fa, String imagem, String telefone, List<Endereco> enderecos) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.secret2fa = secret2fa;
        this.imagem = imagem;
        this.telefone = telefone;
        this.enderecos = enderecos == null ? new ArrayList<>() : new ArrayList<>(enderecos);
    }

    /**
     * @param senhaCriptografada a senha já criptografada (nunca a senha em texto puro)
     */
    public static Usuario criar(String nome, String email, String senhaCriptografada, String telefone, String imagem, List<Endereco> enderecos) {
        return new Usuario(null, LocalDateTime.now(), null, null, nome, email.trim().toLowerCase(), senhaCriptografada, null, imagem, telefone, enderecos);
    }

    public static Usuario restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String nome, String email, String senha, String secret2fa, String imagem, String telefone, List<Endereco> enderecos) {
        return new Usuario(id, dataCadastro, dataAlteracao, dataExcluido, nome, email, senha, secret2fa, imagem, telefone, enderecos);
    }

    public void atualizarDados(String nome, String telefone, String imagem) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
        this.nome = nome;
        this.telefone = telefone;
        this.imagem = imagem;
    }

    public void adicionarEndereco(Endereco endereco) {
        this.enderecos.add(endereco);
    }

    public void removerEndereco(UUID enderecoId) {
        boolean removido = this.enderecos.removeIf(e -> enderecoId.equals(e.getId()));
        if (!removido) {
            throw new EnderecoNaoEncontradoException(enderecoId);
        }
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getSecret2fa() { return secret2fa; }
    public String getImagem() { return imagem; }
    public String getTelefone() { return telefone; }
    public List<Endereco> getEnderecos() { return List.copyOf(enderecos); }
}
