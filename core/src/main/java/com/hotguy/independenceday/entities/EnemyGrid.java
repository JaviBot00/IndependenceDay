package com.hotguy.independenceday.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Gestiona el batallón completo de enemigos como una unidad coordinada.
 *
 * <p>Es responsable de:</p>
 * <ul>
 *   <li>Mover el bloque horizontalmente y descenderlo al tocar los bordes.</li>
 *   <li>Incrementar la velocidad progresivamente al morir enemigos.</li>
 *   <li>Mantener la cache {@code bottomEnemies} para disparos O(1).</li>
 *   <li>Coordinar el disparo aleatorio desde las columnas activas.</li>
 *   <li>Detectar condiciones de victoria y derrota.</li>
 * </ul>
 *
 * <p>Utiliza un dirty flag para que los {@link Enemy} solo recalculen
 * su posición absoluta cuando el grid se ha movido realmente.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class EnemyGrid {

    // ─────────────────────────────────────────────
    // FILAS
    // ─────────────────────────────────────────────

    /** Lista de filas que componen el batallón. */
    private final Array<EnemyRow> rows;

    // ─────────────────────────────────────────────
    // POSICIÓN BASE
    // ─────────────────────────────────────────────

    /** Posición X del origen del grid (esquina inferior izquierda). */
    private float gridX;

    /** Posición Y del origen del grid (esquina inferior izquierda). */
    private float gridY;

    // ─────────────────────────────────────────────
    // MOVIMIENTO
    // ─────────────────────────────────────────────

    /** Velocidad horizontal actual del bloque en píxeles/segundo. */
    private float speed;

    /** Dirección actual: +1 derecha, -1 izquierda. */
    private float direction;

    /**
     * Indica si el grid se ha movido en este frame.
     * Evita recalcular posiciones absolutas innecesariamente.
     */
    private boolean dirty;

    // ─────────────────────────────────────────────
    // DIMENSIONES DEL GRID
    // ─────────────────────────────────────────────

    /** Número de columnas del grid. */
    private final int cols;

    /** Número de filas del grid. */
    private final int rows_count;

    /** Ancho total del bloque de enemigos en píxeles. */
    private final float gridWidth;

    /** Alto total del bloque de enemigos en píxeles. */
    private final float gridHeight;

    // ─────────────────────────────────────────────
    // CACHE DE ENEMIGOS INFERIORES
    // ─────────────────────────────────────────────

    /**
     * Cache del enemigo más bajo vivo por columna.
     * Solo se actualiza cuando muere un enemigo, no cada frame.
     * Permite seleccionar el tirador en O(1).
     */
    private final Enemy[] bottomEnemies;

    // ─────────────────────────────────────────────
    // DISPARO ENEMIGO
    // ─────────────────────────────────────────────

    /** Temporizador acumulado hasta el próximo disparo enemigo. */
    private float shootTimer;

    /** Intervalo aleatorio actual hasta el próximo disparo. */
    private float shootInterval;

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────

    /** Total de enemigos vivos en el grid. */
    private int totalAlive;

    /** Indica si el grid ha llegado a la línea del jugador (derrota). */
    private boolean reachedBottom;

    // ─────────────────────────────────────────────
    // CALLBACKS
    // ─────────────────────────────────────────────

    /**
     * Listener para notificar eventos del grid al {@code GameScreen}.
     * Evita acoplamiento directo entre entidades y pantallas.
     */
    private EnemyGridListener listener;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el batallón completo de enemigos.
     *
     * <p>Distribuye las filas según el esquema clásico de Space Invaders:
     * fila superior tipo C, dos filas medias tipo B, dos filas inferiores tipo A.</p>
     *
     * @param startX Posición X inicial del origen del grid.
     * @param startY Posición Y inicial del origen del grid.
     */
    public EnemyGrid(float startX, float startY) {
        this.gridX     = startX;
        this.gridY     = startY;
        this.cols      = Constants.ENEMY_COLS;
        this.rows_count = Constants.ENEMY_ROWS;
        this.speed     = Constants.ENEMY_SPEED_INIT;
        this.direction = 1f;
        this.dirty     = true;
        this.rows      = new Array<>(rows_count);
        this.bottomEnemies = new Enemy[cols];
        this.totalAlive    = cols * rows_count;
        this.reachedBottom = false;

        // Calcular dimensiones totales del grid
        gridWidth  = cols * Constants.ENEMY_WIDTH
            + (cols - 1) * Constants.ENEMY_H_SPACING;
        gridHeight = rows_count * Constants.ENEMY_HEIGHT
            + (rows_count - 1) * Constants.ENEMY_V_SPACING;

        // Construir filas según esquema clásico (índice 0 = fila inferior)
        // Fila 0-1: tipo A (inferior, menos puntos)
        // Fila 2-3: tipo B (media)
        // Fila 4:   tipo C (superior, más puntos)
        EnemyType[] layout = {
            EnemyType.A, EnemyType.A,
            EnemyType.B, EnemyType.B,
            EnemyType.C
        };

        for (int i = 0; i < rows_count; i++) {
            rows.add(new EnemyRow(layout[i], i, cols));
        }

        // Inicializar cache de enemigos inferiores
        rebuildBottomCache();

        // Inicializar temporizador de disparo
        resetShootTimer();
    }

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Actualiza el movimiento del grid, el disparo enemigo y el estado
     * de cada fila. Detecta colisión con los bordes y descenso del bloque.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    public void update(float delta) {
        dirty = false;

        updateMovement(delta);
        updateShooting(delta);
        updateRows(delta);
        checkReachedBottom();
    }

    /**
     * Mueve el grid horizontalmente y lo desciende al tocar un borde lateral.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateMovement(float delta) {
        gridX += direction * speed * delta;
        dirty = true;

        boolean hitRight = direction > 0 && gridX + gridWidth >= Constants.WORLD_WIDTH;
        boolean hitLeft  = direction < 0 && gridX <= 0;

        if (hitRight || hitLeft) {
            direction *= -1f;
            gridY     -= Constants.ENEMY_DROP;
            // Corregir posición para no salirse del borde
            if (hitRight) gridX = Constants.WORLD_WIDTH - gridWidth;
            if (hitLeft)  gridX = 0;
        }
    }

    /**
     * Gestiona el temporizador de disparo y lanza una bala desde
     * el enemigo inferior de una columna aleatoria activa.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateShooting(float delta) {
        shootTimer += delta;
        if (shootTimer >= shootInterval) {
            shootTimer = 0f;
            resetShootTimer();
            fireFromRandomColumn();
        }
    }

    /**
     * Actualiza todas las filas y procesa las muertes ocurridas en este frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateRows(float delta) {
        for (int r = 0; r < rows.size; r++) {
            EnemyRow row = rows.get(r);
            int deaths = row.update(delta);
            if (deaths > 0) {
                totalAlive -= deaths;
                speed += Constants.ENEMY_SPEED_STEP * deaths;
                rebuildBottomCache();
                if (listener != null) {
                    listener.onEnemiesDied(deaths, row.getType().score);
                }
            }
        }
    }

    /**
     * Comprueba si el bloque de enemigos ha alcanzado la línea del jugador.
     */
    private void checkReachedBottom() {
        if (!reachedBottom && gridY <= Constants.SHIELD_Y) {
            reachedBottom = true;
            if (listener != null) listener.onGridReachedBottom();
        }
    }

    // ─────────────────────────────────────────────
    // DISPARO
    // ─────────────────────────────────────────────

    /**
     * Selecciona aleatoriamente una columna activa y notifica al listener
     * para que genere una bala en la posición del enemigo inferior.
     */
    private void fireFromRandomColumn() {
        // Recopilar columnas activas
        Array<Integer> activeCols = new Array<>();
        for (int col = 0; col < cols; col++) {
            if (bottomEnemies[col] != null) {
                activeCols.add(col);
            }
        }
        if (activeCols.size == 0) return;

        int col = activeCols.get(MathUtils.random(activeCols.size - 1));
        Enemy shooter = bottomEnemies[col];
        if (shooter != null && listener != null) {
            float bulletX = shooter.getX() + shooter.getWidth()  * 0.5f;
            float bulletY = shooter.getY();
            listener.onEnemyShoot(bulletX, bulletY);
        }

        if (Assets.sfxEnemyShoot != null) {
            Assets.sfxEnemyShoot.play(0.6f);
        }
    }

    /**
     * Reinicia el temporizador de disparo con un intervalo aleatorio.
     */
    private void resetShootTimer() {
        shootInterval = MathUtils.random(
            Constants.ENEMY_SHOOT_MIN,
            Constants.ENEMY_SHOOT_MAX
        );
    }

    // ─────────────────────────────────────────────
    // CACHE
    // ─────────────────────────────────────────────

    /**
     * Reconstruye la cache de enemigos inferiores por columna.
     *
     * <p>Itera las filas de abajo a arriba para encontrar el enemigo
     * vivo más bajo en cada columna. Solo se llama cuando muere
     * algún enemigo, no cada frame.</p>
     */
    private void rebuildBottomCache() {
        for (int col = 0; col < cols; col++) {
            bottomEnemies[col] = null;
            // Iterar de fila inferior (0) a superior
            for (int r = 0; r < rows.size; r++) {
                Enemy candidate = rows.get(r).getAliveEnemyInColumn(col);
                if (candidate != null) {
                    bottomEnemies[col] = candidate;
                    break;
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Renderiza todas las filas del grid pasando la posición base actual.
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    public void render(SpriteBatch batch) {
        for (EnemyRow row : rows) {
            row.render(batch, gridX, gridY);
        }
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    /**
     * Devuelve la lista de todas las filas del grid.
     *
     * @return {@link Array} de {@link EnemyRow}.
     */
    public Array<EnemyRow> getRows() { return rows; }

    /**
     * Devuelve el total de enemigos vivos en el grid.
     *
     * @return Número de enemigos activos.
     */
    public int getTotalAlive() { return totalAlive; }

    /**
     * Indica si todos los enemigos han sido eliminados.
     *
     * @return {@code true} si no queda ningún enemigo vivo.
     */
    public boolean isCleared() { return totalAlive <= 0; }

    /**
     * Indica si el grid ha alcanzado la línea del jugador.
     *
     * @return {@code true} si el grid ha llegado abajo.
     */
    public boolean hasReachedBottom() { return reachedBottom; }

    /**
     * Asigna el listener de eventos del grid.
     *
     * @param listener Implementación de {@link EnemyGridListener}.
     */
    public void setListener(EnemyGridListener listener) {
        this.listener = listener;
    }

    // ─────────────────────────────────────────────
    // LISTENER
    // ─────────────────────────────────────────────

    /**
     * Interfaz de callbacks para notificar eventos del grid
     * sin acoplamiento directo con las pantallas.
     *
     * @author Javier Botella
     * @version 1.0
     */
    public interface EnemyGridListener {

        /**
         * Llamado cuando uno o más enemigos mueren en el mismo frame.
         *
         * @param count     Número de enemigos muertos.
         * @param scoreEach Puntos que vale cada enemigo muerto.
         */
        void onEnemiesDied(int count, int scoreEach);

        /**
         * Llamado cuando un enemigo va a disparar.
         *
         * @param x Posición X del centro inferior del enemigo.
         * @param y Posición Y del borde inferior del enemigo.
         */
        void onEnemyShoot(float x, float y);

        /**
         * Llamado cuando el grid alcanza la línea del jugador.
         */
        void onGridReachedBottom();
    }
}
