package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Representa un proyectil en vuelo, ya sea del jugador o de un enemigo.
 *
 * <p>Implementa {@link Pool.Poolable} para ser compatible con el sistema
 * de object pooling de libGDX. Cuando una bala sale de pantalla o impacta,
 * no se destruye sino que se devuelve al {@link BulletPool} para su
 * reutilización, evitando presión innecesaria sobre el GC.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class Bullet extends Entity implements Pool.Poolable {

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────

    /** Velocidad vertical de la bala en píxeles/segundo. Positiva = sube, negativa = baja. */
    private float velocityY;

    /** Indica si la bala fue disparada por el jugador ({@code true}) o por un enemigo. */
    private boolean fromPlayer;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea una bala en estado inactivo lista para ser inicializada por el pool.
     *
     * <p>No usar directamente — obtener instancias a través de {@link BulletPool}.</p>
     */
    public Bullet() {
        super(0, 0, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);
        this.active = false;
    }

    // ─────────────────────────────────────────────
    // INICIALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Inicializa la bala con posición y tipo al ser obtenida del pool.
     *
     * @param x          Posición X del centro de la bala.
     * @param y          Posición Y del borde inferior de la bala.
     * @param fromPlayer {@code true} si la dispara el jugador, {@code false} si es enemiga.
     */
    public void init(float x, float y, boolean fromPlayer) {
        this.x          = x - width * 0.5f;
        this.y          = y;
        this.fromPlayer = fromPlayer;
        this.active     = true;
        this.velocityY  = fromPlayer
            ? Constants.BULLET_PLAYER_SPEED
            : -Constants.BULLET_ENEMY_SPEED;
        this.textureRegion = fromPlayer
            ? new com.badlogic.gdx.graphics.g2d.TextureRegion(Assets.bulletPlayerTexture)
            : new com.badlogic.gdx.graphics.g2d.TextureRegion(Assets.bulletEnemyTexture);
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * Mueve la bala verticalmente según su velocidad.
     * La desactiva si sale por los bordes de la pantalla.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        if (!active) return;
        y += velocityY * delta;

        // Salida por borde superior o inferior
        if (y > Constants.WORLD_HEIGHT || y + height < 0) {
            active = false;
        }
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Llamado al colisionar con otra entidad.
     * La bala se desactiva para ser devuelta al pool.
     *
     * @param other El objeto con el que se ha producido la colisión.
     */
    @Override
    public void onCollision(Collidable other) {
        active = false;
    }

    // ─────────────────────────────────────────────
    // POOL.POOLABLE
    // ─────────────────────────────────────────────

    /**
     * Reinicia el estado de la bala al devolverla al pool.
     *
     * <p>libGDX llama a este método automáticamente al hacer
     * {@code pool.free(bullet)}. Deja la bala en estado neutro
     * lista para una nueva inicialización.</p>
     */
    @Override
    public void reset() {
        x          = 0;
        y          = 0;
        velocityY  = 0;
        fromPlayer = false;
        active     = false;
        textureRegion = null;
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Indica si la bala fue disparada por el jugador.
     *
     * @return {@code true} si es del jugador, {@code false} si es enemiga.
     */
    public boolean isFromPlayer() { return fromPlayer; }
}
