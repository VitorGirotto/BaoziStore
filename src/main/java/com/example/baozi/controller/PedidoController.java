package com.example.baozi.controller;

import com.example.baozi.model.Cliente;
import com.example.baozi.model.Pedido;
import com.example.baozi.model.Produto;
import com.example.baozi.repository.ClienteRepository;
import com.example.baozi.repository.PedidoRepository;
import com.example.baozi.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(PedidoRepository pedidoRepository,
                            ClienteRepository clienteRepository,
                            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscar(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pedido> cadastrar(@RequestBody Pedido pedido) {
        Optional<Cliente> cliente = buscarClienteDoPedido(pedido);
        Optional<Produto> produto = buscarProdutoDoPedido(pedido);

        if (cliente.isEmpty() || produto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        pedido.setId(null);
        pedido.setCliente(cliente.get());
        pedido.setProduto(produto.get());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizar(@PathVariable Long id,
                                            @RequestBody Pedido pedido) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Optional<Cliente> cliente = buscarClienteDoPedido(pedido);
        Optional<Produto> produto = buscarProdutoDoPedido(pedido);

        if (cliente.isEmpty() || produto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        pedido.setId(id);
        pedido.setCliente(cliente.get());
        pedido.setProduto(produto.get());
        return ResponseEntity.ok(pedidoRepository.save(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Optional<Cliente> buscarClienteDoPedido(Pedido pedido) {
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            return Optional.empty();
        }
        return clienteRepository.findById(pedido.getCliente().getId());
    }

    private Optional<Produto> buscarProdutoDoPedido(Pedido pedido) {
        if (pedido.getProduto() == null || pedido.getProduto().getId() == null) {
            return Optional.empty();
        }
        return produtoRepository.findById(pedido.getProduto().getId());
    }
}
