package com.hotguy.independenceday.interfaces;

import com.badlogic.gdx.math.Rectangle;

/**
 * Contrato para todos los objetos del juego que participan
 * en el sistema de detección de colisiones.
 *
 * @author Javier Botella
 * @version 1.0
 */
public interface Collidable {

    /**
     * Devuelve el rectángulo de colisión (hitbox) del objeto
     * en coordenadas del mundo.
     *
     * @return {@link Rectangle} que representa el área de colisión.
     */
    Rectangle getBounds();

    /**
     * Llamado por el {@code CollisionManager} cuando este objeto
     * colisiona con otro {@link Collidable}.
     *
     * @param other El otro objeto con el que se ha producido la colisión.
     */
    void onCollision(Collidable other);
}
