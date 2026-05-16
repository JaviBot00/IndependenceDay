package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.utils.Assets;

/**
 * Representa una nave enemiga individual dentro de un {@link EnemyRow}.
 *
 * <p>No gestiona su posición absoluta directamente — trabaja con un offset
 * relativo a la posición base del {@link EnemyGrid}. La posición absoluta
 * se calcula en el momento del renderizado, evitando recálculos innecesarios
 * cada frame gracias al dirty flag del grid.</p>
 *
 * <p>La animación de dos frames es compartida a nivel de {@link EnemyRow},
 * por lo que esta clase solo consulta el {@code stateTime} externo para
 * obtener el frame correcto.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class Enemy extends LivingEntity {

    // ─────────────────────────────────────────────
    // TIPO Y PUNTUACIÓN
    // ─────────────────────────────────────────────

    /** Tipo de enemigo que determina skin y puntuación. */
    private final EnemyType type;

    // ─────────────────────────────────────────────
    // POSICIÓN RELATIVA
    // ─────────────────────────────────────────────

    /**
     * Offset horizontal respecto a la posición base del {@link EnemyGrid}.
     * No cambia durante la partida salvo que el grid se reorganice.
     */
    private final float offsetX;

    /**
     * Offset vertical respecto a la posición base del {@link EnemyGrid}.
     * No cambia durante la partida.
     */
    private final float offsetY;

    // ─────────────────────────────────────────────
    // ANIMACIÓN
    // ─────────────────────────────────────────────

    /**
     * Animación de dos frames compartida con el resto de enemigos
     * de la misma fila. Se actualiza desde {@link EnemyRow}.
     */
    private final Animation<TextureRegion> animation;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea un enemigo individual con su offset relativo al grid y su animación.
     *
     * @param offsetX   Desplazamiento horizontal respecto al origen del grid.
     * @param offsetY   Desplazamiento vertical respecto al origen del grid.
     * @param width     Ancho del sprite en píxeles.
     * @param height    Alto del sprite en píxeles.
     * @param type      Tipo de enemigo (determina skin y puntuación).
     * @param animation Animación de dos frames compartida con la fila.
     */
    public Enemy(float offsetX, float offsetY, float width, float height,
                 EnemyType type, Animation<TextureRegion> animation) {
        super(0, 0, width, height, 1);
        this.offsetX   = offsetX;
        this.offsetY   = offsetY;
        this.type      = type;
        this.animation = animation;
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * Actualiza el estado interno del enemigo.
     *
     * <p>La animación no se actualiza aquí sino en {@link EnemyRow}
     * mediante el stateTime compartido. Solo se delega al padre
     * para mantener el flash de impacto.</p>
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        super.update(delta);
    }

    // ─────────────────────────────────────────────
    // RENDERABLE
    // ─────────────────────────────────────────────

    /**
     * Renderiza el enemigo en su posición absoluta calculada a partir
     * del origen del grid y su offset relativo.
     *
     * @param batch     Batch activo con el que se realiza el renderizado.
     * @param gridX     Posición X base del {@link EnemyGrid}.
     * @param gridY     Posición Y base del {@link EnemyGrid}.
     * @param stateTime Tiempo de animación compartido con la fila.
     */
    public void render(SpriteBatch batch, float gridX, float gridY, float stateTime) {
        if (!active) return;

        x = gridX + offsetX;
        y = gridY + offsetY;

        textureRegion = animation.getKeyFrame(stateTime, true);
        super.render(batch);
    }

    /**
     * Implementación de {@link com.hotguy.independenceday.interfaces.Renderable}.
     * No usar directamente — utilizar {@link #render(SpriteBatch, float, float, float)}.
     *
     * @param batch Batch activo.
     */
    @Override
    public void render(SpriteBatch batch) {
        // Delegado al método con parámetros de grid
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Llamado por el {@code CollisionManager} cuando una bala impacta
     * contra este enemigo.
     *
     * @param other El objeto con el que se ha producido la colisión.
     */
    @Override
    public void onCollision(Collidable other) {
        if (other instanceof Bullet) {
            Bullet bullet = (Bullet) other;
            if (bullet.isFromPlayer()) {
                takeDamage(1);
            }
        }
    }

    // ─────────────────────────────────────────────
    // LIVING ENTITY
    // ─────────────────────────────────────────────

    /**
     * Lógica ejecutada al morir el enemigo.
     *
     * <p>El sonido y las partículas se gestionan desde {@link EnemyGrid}
     * al detectar el cambio de estado, para centralizar efectos globales
     * como el incremento de velocidad del bloque.</p>
     */
    @Override
    protected void onDeath() {
        // Gestionado por EnemyGrid al detectar aliveCount decrementado
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve el tipo de este enemigo.
     *
     * @return {@link EnemyType} con la puntuación y clave de atlas.
     */
    public EnemyType getType() { return type; }

    /**
     * Devuelve el offset horizontal relativo al grid.
     *
     * @return Desplazamiento X en píxeles.
     */
    public float getOffsetX() { return offsetX; }

    /**
     * Devuelve el offset vertical relativo al grid.
     *
     * @return Desplazamiento Y en píxeles.
     */
    public float getOffsetY() { return offsetY; }
}
