package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.interfaces.Renderable;
import com.hotguy.independenceday.interfaces.Updatable;

/**
 * Clase base abstracta para todas las entidades del juego.
 *
 * <p>Encapsula los atributos comunes a cualquier objeto del mundo:
 * posición, dimensiones, textura y hitbox. Implementa las tres
 * interfaces del juego para forzar a las subclases a definir
 * su comportamiento de actualización, renderizado y colisión.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public abstract class Entity implements Updatable, Renderable, Collidable {

    // ─────────────────────────────────────────────
    // POSICIÓN Y DIMENSIONES
    // ─────────────────────────────────────────────

    /** Posición horizontal del borde izquierdo de la entidad. */
    protected float x;

    /** Posición vertical del borde inferior de la entidad. */
    protected float y;

    /** Ancho de la entidad en píxeles. */
    protected float width;

    /** Alto de la entidad en píxeles. */
    protected float height;

    // ─────────────────────────────────────────────
    // TEXTURA
    // ─────────────────────────────────────────────

    /** Región de textura que representa visualmente esta entidad. */
    protected TextureRegion textureRegion;

    // ─────────────────────────────────────────────
    // COLISIÓN
    // ─────────────────────────────────────────────

    /**
     * Rectángulo de colisión reutilizable para evitar crear objetos
     * nuevos cada frame y reducir la presión sobre el GC.
     */
    protected final Rectangle bounds;

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────

    /** Indica si la entidad está activa en el mundo de juego. */
    protected boolean active;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Inicializa la entidad con posición y dimensiones.
     *
     * @param x      Posición horizontal inicial.
     * @param y      Posición vertical inicial.
     * @param width  Ancho de la entidad.
     * @param height Alto de la entidad.
     */
    public Entity(float x, float y, float width, float height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.active = true;
        this.bounds = new Rectangle(x, y, width, height);
    }

    // ─────────────────────────────────────────────
    // RENDERABLE
    // ─────────────────────────────────────────────

    /**
     * Dibuja la entidad en pantalla si está activa y tiene textura asignada.
     *
     * <p>Las subclases pueden sobreescribir este método para añadir
     * efectos adicionales como flash de impacto o partículas.</p>
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    @Override
    public void render(SpriteBatch batch) {
        if (active && textureRegion != null) {
            batch.draw(textureRegion, x, y, width, height);
        }
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Devuelve el rectángulo de colisión actualizado con la posición actual.
     *
     * @return {@link Rectangle} sincronizado con la posición de la entidad.
     */
    @Override
    public Rectangle getBounds() {
        bounds.set(x, y, width, height);
        return bounds;
    }

    // ─────────────────────────────────────────────
    // GETTERS Y SETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve la posición horizontal de la entidad.
     *
     * @return Coordenada X del borde izquierdo.
     */
    public float getX() { return x; }

    /**
     * Devuelve la posición vertical de la entidad.
     *
     * @return Coordenada Y del borde inferior.
     */
    public float getY() { return y; }

    /**
     * Devuelve el ancho de la entidad.
     *
     * @return Ancho en píxeles.
     */
    public float getWidth() { return width; }

    /**
     * Devuelve el alto de la entidad.
     *
     * @return Alto en píxeles.
     */
    public float getHeight() { return height; }

    /**
     * Indica si la entidad está activa en el mundo de juego.
     *
     * @return {@code true} si la entidad está activa.
     */
    public boolean isActive() { return active; }

    /**
     * Establece el estado activo de la entidad.
     *
     * @param active {@code true} para activar, {@code false} para desactivar.
     */
    public void setActive(boolean active) { this.active = active; }

    /**
     * Asigna la región de textura de la entidad.
     *
     * @param textureRegion Región del atlas de sprites a usar.
     */
    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    // ─────────────────────────────────────────────
    // DISPOSE
    // ─────────────────────────────────────────────

    /**
     * Libera los recursos propios de la entidad.
     *
     * <p>Las subclases que gestionen recursos adicionales deben
     * sobreescribir este método llamando a {@code super.dispose()}.</p>
     */
    public void dispose() {}
}
