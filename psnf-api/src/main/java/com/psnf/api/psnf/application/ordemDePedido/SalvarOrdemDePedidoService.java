package com.psnf.api.psnf.application.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.CadastrarOrdemDePedidoUseCase;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.command.CadastrarOrdemDePedidoCommand;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.mapper.CadastrarOrdemDePedidoCommandMapper;
import com.psnf.api.psnf.domain.ordemDePedido.port.out.OrdemDePedidoRepositoryPort;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalvarOrdemDePedidoService implements CadastrarOrdemDePedidoUseCase {

    private final OrdemDePedidoRepositoryPort ordemDePedidoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    public OrdemDePedido cadastrarOrdemDePedido(CadastrarOrdemDePedidoCommand cadastrarOrdemDePedidoCommand) {

        Usuario comprador = usuarioRepositoryPort.detalhesUsuario(cadastrarOrdemDePedidoCommand.compradorId());
        Usuario vendedor = usuarioRepositoryPort.detalhesUsuario(cadastrarOrdemDePedidoCommand.vendedorId());


        OrdemDePedido ordemDePedido = OrdemDePedido.criar(
                LocalDateTime.now(),
                null,
                null,
                gerarCodigo(),
                cadastrarOrdemDePedidoCommand.previsaoDeEntrega(),
                comprador,
                vendedor,
                List.of(),
                List.of(),
                List.of(),
                CadastrarOrdemDePedidoCommandMapper.toItens(cadastrarOrdemDePedidoCommand.itens())
        );

        return ordemDePedidoRepositoryPort.save(ordemDePedido);
    }

    private String gerarCodigo() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
