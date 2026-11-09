package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.domain.negociacao.port.out.NegociacaoRepositoryPort;
import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.CadastrarOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.CadastrarOrdemDePedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.ItemPedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * O vendedor (usuário logado) cria a ordem para o comprador com quem negociou no chat.
 * Cada item informa o anúncio, a quantidade e o valor unitário acordado.
 */
@Service
@RequiredArgsConstructor
public class SalvarOrdemDePedidoService implements CadastrarOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AnuncioRepositoryPort anuncioRepositoryPort;
    private final NegociacaoRepositoryPort negociacaoRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido cadastrarOrdemDePedido(CadastrarOrdemDePedidoCommand command) {

        Usuario vendedor = usuarioRepositoryPort.buscarPorId(command.vendedorId());
        Usuario comprador = usuarioRepositoryPort.buscarPorId(command.compradorId());

        List<ItemPedido> itens = command.itens().stream()
                .map(item -> criarItem(item, vendedor))
                .toList();

        OrdemDePedido ordemDePedido = OrdemDePedido.criar(gerarCodigo(), comprador, vendedor, itens);

        // gerada pelo chat: a ordem guarda a negociação e o chat ganha um aviso com a ordem
        Negociacao negociacao = null;
        if (command.negociacaoId() != null) {
            negociacao = negociacaoRepositoryPort.buscarPorId(command.negociacaoId());
            if (!negociacao.ehVendedor(vendedor.getId()) || !negociacao.getCompradorId().equals(comprador.getId())) {
                throw new RegraDeNegocioException("A negociação informada não é entre este vendedor e este comprador");
            }
            ordemDePedido.vincularNegociacao(negociacao.getId());
        }

        OrdemDePedido salva = ordemDePedidoRepositoryPort.save(ordemDePedido);
        if (negociacao != null) {
            negociacao.registrarOrdemGerada(vendedor.getId(), salva.getId(), salva.getCodigo(), salva.getValorTotal());
            negociacaoRepositoryPort.save(negociacao);
        }
        return salva;
    }

    private ItemPedido criarItem(ItemPedidoCommand item, Usuario vendedor) {
        Anuncio anuncio = anuncioRepositoryPort.buscarPorId(item.anuncioId());

        if (!anuncio.pertenceA(vendedor.getId())) {
            throw new AcessoNegadoException("O anúncio '" + anuncio.getTitulo() + "' não pertence ao vendedor");
        }
        if (!anuncio.isAtivo()) {
            throw new RegraDeNegocioException("O anúncio '" + anuncio.getTitulo() + "' não está ativo");
        }

        Variacao variacao = item.variacaoId() == null ? null : anuncio.buscarVariacao(item.variacaoId());

        return ItemPedido.criar(
                anuncio.getId(),
                anuncio.getTitulo(),
                variacao == null ? null : variacao.getVariacao(),
                item.quantidade(),
                item.valorUnidade(),
                // cópia das informações do produto (SKU, NCM, garantia...) para a ordem
                anuncio.informacoesParaVenda(variacao)
        );
    }

    private String gerarCodigo() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
