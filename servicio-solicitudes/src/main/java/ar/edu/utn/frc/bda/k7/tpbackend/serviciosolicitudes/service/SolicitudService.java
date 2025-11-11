package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service;

import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.ClienteRestAPIClient;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.ProductoRestAPIClient;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository.PersistenciaSolicitud;


@Service
public class SolicitudService {

    private final PersistenciaSolicitud persistenciaPedido;
    private final ClienteRestAPIClient clienteClient;
    private final ProductoRestAPIClient productoClient;

    public SolicitudService(PersistenciaSolicitud persistenciaPedido, 
                         ClienteRestAPIClient clienteClient
                         , ProductoRestAPIClient productoClient) {
        this.persistenciaPedido = persistenciaPedido;
        this.clienteClient = clienteClient;
        this.productoClient = productoClient;
    }

    public Solicitud crearPedido(Solicitud pedido) {
        
        if (!clienteClient.existeCliente(pedido.getClienteId())) {
             throw new RuntimeException("El Cliente con ID " + pedido.getClienteId() + " no existe.");
        }
        
        if (!productoClient.verificarYDescontarStock(pedido.getProductoId(), pedido.getCantidadProducto())) {
             throw new RuntimeException("Producto no disponible o stock insuficiente.");
        }
        
        pedido.setEstado("CREADO");
        return persistenciaPedido.save(pedido);
    }

    public Solicitud obtenerPedidoPorId(Long id) {
        return persistenciaPedido.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public java.util.List<Solicitud> obtenerTodosLosPedidos() {
        return persistenciaPedido.findAll();
    }
}