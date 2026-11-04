package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.out.AnuncioRepositoryPort;
import com.psnf.api.psnf.domain.ordemDePedido.models.ItemPedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.CadastrarOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.CadastrarOrdemDePedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.ItemPedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalvarOrdemDePedidoService implements CadastrarOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AnuncioRepositoryPort anuncioRepositoryPort;

    @Override
    @Transactional
    public OrdemDePedido cadastrarOrdemDePedido(CadastrarOrdemDePedidoCommand command) {

        Usuario comprador = usuarioRepositoryPort.buscarPorId(command.compradorId());

        UUID vendedorId = null;
        List<ItemPedido> itens = new ArrayList<>();

        for (ItemPedidoCommand itemCommand : command.itens()) {
            Anuncio anuncio = anuncioRepositoryPort.buscarPorId(itemCommand.anuncioId());

            if (!anuncio.isAtivo()) {
                throw new RegraDeNegocioException("O anúncio '" + anuncio.getTitulo() + "' não está ativo");
            }
            if (vendedorId == null) {
                vendedorId = anuncio.getVendedor();
            } else if (!vendedorId.equals(anuncio.getVendedor())) {
                throw new RegraDeNegocioException("Todos os itens de uma ordem de pedido devem ser do mesmo vendedor");
            }

            String variacao = itemCommand.variacaoId() == null
                    ? null
                    : anuncio.buscarVariacao(itemCommand.variacaoId()).getVariacao();

            itens.add(ItemPedido.criar(
                    anuncio.getId(),
                    anuncio.getTitulo(),
                    variacao,
                    itemCommand.quantidade(),
                    anuncio.calcularValorUnidade(itemCommand.quantidade())
            ));
        }

        Usuario vendedor = usuarioRepositoryPort.buscarPorId(vendedorId);

        OrdemDePedido ordemDePedido = OrdemDePedido.criar(gerarCodigo(), comprador, vendedor, itens);

        return ordemDePedidoRepositoryPort.save(ordemDePedido);
    }

    private String gerarCodigo() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
