package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Representa una fila completa de enemigos dentro del {@link EnemyGrid}.
 *
 * <p>Agrupa un conjunto de {@link Enemy} del mismo {@link EnemyType} y
 * gestiona los recursos compartidos de la fila: la animación, el contador
 * de enemigos vivos y la cache del enemigo más bajo por columna.</p>
 *
 * <p>El stateTime de la animación es único por fila, de modo que todos
 * los enemigos de la misma fila están sincronizados visualmente sin
 * necesidad de que cada uno mantenga su propio temporizador.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class EnemyRow {

    // ─────────────────────────────────────────────
    // TIPO Y PUNTUACIÓN
    // ─────────────────────────────────────────────

    /** Tipo de enemigo de esta fila, común a todos sus integrantes. */
    private final EnemyType type;

    // ─────────────────────────────────────────────
    // ENEMIGOS
    // ─────────────────────────────────────────────

    /** Lista de enemigos individuales que componen esta fila. */
    private final Array<Enemy> enemies;

    /** Número de enemigos vivos actualmente en esta fila. */
    private int aliveCount;

    // ─────────────────────────────────────────────
    // ANIMACIÓN COMPARTIDA
    // ─────────────────────────────────────────────

    /** Animación de dos frames compartida por todos los enemigos de la fila. */
    private final Animation<TextureRegion> animation;

    /**
     * Temporizador acumulado de animación compartido.
     * Se incrementa cada frame y se pasa a cada Enemy en el renderizado.
     */
    private float stateTime;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea una fila de enemigos del tipo indicado.
     *
     * <p>Construye la animación compartida a partir del atlas de sprites
     * y genera cada {@link Enemy} con su offset relativo al grid.</p>
     *
     * @param type     Tipo de enemigo que define skin y puntuación.
     * @param rowIndex Índice de fila dentro del grid (0 = inferior).
     * @param cols     Número de columnas (enemigos por fila).
     */
    public EnemyRow(EnemyType type, int rowIndex, int cols) {
        this.type      = type;
        this.stateTime = 0f;
        this.enemies   = new Array<>(cols);

        // Construir animación compartida desde el atlas
        TextureRegion frame0 = Assets.atlas.findRegion(type.atlasKey, 0);
        TextureRegion frame1 = Assets.atlas.findRegion(type.atlasKey, 1);
        Array<TextureRegion> frames = new Array<>();
        frames.add(frame0);
        frames.add(frame1);
        animation = new Animation<>(Constants.ENEMY_ANIM_FRAME, frames);

        // Calcular offset vertical de esta fila
        float offsetY = rowIndex * (Constants.ENEMY_HEIGHT + Constants.ENEMY_V_SPACING);

        // Crear enemigos con su offset relativo al grid
        for (int col = 0; col < cols; col++) {
            float offsetX = col * (Constants.ENEMY_WIDTH + Constants.ENEMY_H_SPACING);
            enemies.add(new Enemy(
                offsetX, offsetY,
                Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT,
                type, animation
            ));
        }

        aliveCount = cols;
    }

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Actualiza el stateTime compartido y el estado de cada enemigo vivo.
     *
     * <p>También detecta si algún enemigo ha muerto en este frame para
     * decrementar {@code aliveCount} y notificarlo al {@link EnemyGrid}.</p>
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     * @return Número de enemigos que han muerto en este frame (0 o más).
     */
    public int update(float delta) {
        stateTime += delta;

        int deathsThisFrame = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isActive() && enemy.isAlive()) {
                // Estado inconsistente — ya gestionado
            }
            if (enemy.isActive()) {
                enemy.update(delta);
            } else if (enemy.getHealth() == 0 && aliveCount > 0) {
                // El enemigo acaba de morir este frame
                aliveCount--;
                deathsThisFrame++;
            }
        }
        return deathsThisFrame;
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Renderiza todos los enemigos vivos de esta fila.
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     * @param gridX Posición X base del {@link EnemyGrid}.
     * @param gridY Posición Y base del {@link EnemyGrid}.
     */
    public void render(SpriteBatch batch, float gridX, float gridY) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.render(batch, gridX, gridY, stateTime);
            }
        }
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    /**
     * Devuelve el enemigo más bajo vivo en la columna indicada.
     *
     * <p>Usado por {@link EnemyGrid} para actualizar la cache
     * {@code bottomEnemies} cuando muere un enemigo.</p>
     *
     * @param col Índice de columna a consultar.
     * @return El {@link Enemy} vivo de esa columna, o {@code null} si no hay ninguno.
     */
    public Enemy getAliveEnemyInColumn(int col) {
        if (col < 0 || col >= enemies.size) return null;
        Enemy enemy = enemies.get(col);
        return enemy.isActive() ? enemy : null;
    }

    /**
     * Devuelve la lista completa de enemigos de esta fila,
     * incluyendo los ya eliminados.
     *
     * @return {@link Array} de {@link Enemy}.
     */
    public Array<Enemy> getEnemies() { return enemies; }

    /**
     * Devuelve el número de enemigos vivos en esta fila.
     *
     * @return Cantidad de enemigos activos.
     */
    public int getAliveCount() { return aliveCount; }

    /**
     * Indica si todos los enemigos de esta fila han sido eliminados.
     *
     * @return {@code true} si no queda ningún enemigo vivo.
     */
    public boolean isEmpty() { return aliveCount <= 0; }

    /**
     * Devuelve el tipo de enemigo de esta fila.
     *
     * @return {@link EnemyType} de la fila.
     */
    public EnemyType getType() { return type; }
}
