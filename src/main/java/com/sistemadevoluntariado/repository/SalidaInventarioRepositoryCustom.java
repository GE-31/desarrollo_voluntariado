package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.SalidaInventario;
import com.sistemadevoluntariado.entity.SalidaInventarioDetalle;

public interface SalidaInventarioRepositoryCustom {

    List<SalidaInventario> listarTodos();

    SalidaInventario obtenerPorId(int idSalidaInv);

    List<SalidaInventarioDetalle> obtenerDetalle(int idSalidaInv);

    int registrarCabecera(SalidaInventario salida);

    boolean registrarDetalle(int idSalidaInv, int idItem, double cantidad);

    boolean anular(int idSalidaInv, int idUsuario, String motivo);

    List<InventarioItem> listarItemsDisponibles();
}
