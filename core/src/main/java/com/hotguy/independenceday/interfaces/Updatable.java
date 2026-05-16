package com.hotguy.independenceday.interfaces;

/**
 * Contrato para todos los objetos del juego que necesitan
 * actualizarse cada frame con la lógica de juego.
 *
 * @author Javier Botella
 * @version 1.0
 */
public interface Updatable {

    /**
     * Actualiza el estado del objeto.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    void update(float delta);
}
