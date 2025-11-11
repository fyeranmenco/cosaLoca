package ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.service;


import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.integration.ClienteRestAPIClient;
import ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.integration.ProductoRestAPIClient;
import ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.model.Pedido;
import ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.repository.PersistenciaPedido;

@Service
public class PedidoService {

    private final PersistenciaPedido persistenciaPedido;
    private final ClienteRestAPIClient clienteClient;
    private final ProductoRestAPIClient productoClient;

    public PedidoService(PersistenciaPedido persistenciaPedido, 
                         ClienteRestAPIClient clienteClient
                         , ProductoRestAPIClient productoClient) {
        this.persistenciaPedido = persistenciaPedido;
        this.clienteClient = clienteClient;
        this.productoClient = productoClient;
    }

    public Pedido crearPedido(Pedido pedido) {
        
        if (!clienteClient.existeCliente(pedido.getClienteId())) {
             throw new RuntimeException("El Cliente con ID " + pedido.getClienteId() + " no existe.");
        }
        
        if (!productoClient.verificarYDescontarStock(pedido.getProductoId(), pedido.getCantidadProducto())) {
             throw new RuntimeException("Producto no disponible o stock insuficiente.");
        }
        
        pedido.setEstado("CREADO");
        return persistenciaPedido.save(pedido);
    }

    public Pedido obtenerPedidoPorId(Long id) {
        return persistenciaPedido.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public java.util.List<Pedido> obtenerTodosLosPedidos() {
        return persistenciaPedido.findAll();
    }
}