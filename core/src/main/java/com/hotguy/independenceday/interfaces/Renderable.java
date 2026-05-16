package com.hotguy.independenceday.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Contrato para todos los objetos del juego que pueden
 * dibujarse en pantalla mediante un {@link SpriteBatch}.
 *
 * @author Javier Botella
 * @version 1.0
 */
public interface Renderable {

    /**
     * Dibuja el objeto en pantalla.
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    void render(SpriteBatch batch);
}
