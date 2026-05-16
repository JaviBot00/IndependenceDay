package com.hotguy.independenceday.entities;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.hotguy.independenceday.utils.Constants;

/**
 * Gestiona el ciclo de vida de los proyectiles mediante object pooling.
 *
 * <p>Mantiene dos colecciones separadas:</p>
 * <ul>
 *   <li>{@code activeBullets} — balas actualmente en vuelo.</li>
 *   <li>Pool interno de libGDX — balas inactivas esperando ser reutilizadas.</li>
 * </ul>
 *
 * <p>Al disparar se obtiene una bala del pool (reciclada o nueva).
 * Al salir de pantalla o impactar se devuelve al pool automáticamente,
 * eliminando la creación y destrucción continua de objetos.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class BulletPool {

    // ─────────────────────────────────────────────
    // POOL INTERNO
    // ─────────────────────────────────────────────

    /**
     * Pool de libGDX que crea nuevas balas cuando no hay recicladas
     * y las reutiliza cuando las hay.
     */
    private final Pool<Bullet> pool = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };

    // ─────────────────────────────────────────────
    // BALAS ACTIVAS
    // ─────────────────────────────────────────────

    /** Lista de balas actualmente en vuelo. */
    private final Array<Bullet> activeBullets;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el pool con una capacidad inicial para evitar
     * redimensionamientos durante el juego.
     */
    public BulletPool() {
        // Capacidad inicial generosa para no redimensionar durante el juego
        activeBullets = new Array<>(false, 64);
        // Pre-calentar el pool con algunas balas
        pool.fill(8);
    }

    // ─────────────────────────────────────────────
    // DISPARO
    // ─────────────────────────────────────────────

    /**
     * Obtiene una bala del pool, la inicializa y la añade a las activas.
     *
     * @param x          Posición X del centro del cañón que dispara.
     * @param y          Posición Y del borde desde el que sale la bala.
     * @param fromPlayer {@code true} si la dispara el jugador.
     * @return La {@link Bullet} inicializada y lista para usarse.
     */
    public Bullet fire(float x, float y, boolean fromPlayer) {
        Bullet bullet = pool.obtain();
        bullet.init(x, y, fromPlayer);
        activeBullets.add(bullet);
        return bullet;
    }

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Actualiza todas las balas activas y devuelve al pool las que
     * han salido de pantalla o han colisionado.
     *
     * <p>Itera la lista al revés para poder eliminar elementos
     * de forma segura sin afectar los índices restantes.</p>
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    public void update(float delta) {
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet bullet = activeBullets.get(i);
            bullet.update(delta);
            if (!bullet.isActive()) {
                activeBullets.removeIndex(i);
                pool.free(bullet);
            }
        }
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Renderiza todas las balas activas.
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        for (Bullet bullet : activeBullets) {
            bullet.render(batch);
        }
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    /**
     * Devuelve la lista de balas actualmente en vuelo.
     *
     * <p>Usada por el {@code CollisionManager} para comprobar colisiones.</p>
     *
     * @return {@link Array} de {@link Bullet} activas.
     */
    public Array<Bullet> getActiveBullets() { return activeBullets; }

    /**
     * Devuelve al pool todas las balas activas de golpe.
     * Útil al reiniciar la partida o cambiar de pantalla.
     */
    public void clear() {
        pool.freeAll(activeBullets);
        activeBullets.clear();
    }

    // ─────────────────────────────────────────────
    // DISPOSE
    // ─────────────────────────────────────────────

    /**
     * Libera todos los recursos del pool.
     */
    public void dispose() {
        clear();
        pool.clear();
    }
}
