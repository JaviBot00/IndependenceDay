package com.hotguy.independenceday.managers;

import com.badlogic.gdx.utils.Array;
import com.hotguy.independenceday.entities.Bullet;
import com.hotguy.independenceday.entities.BulletPool;
import com.hotguy.independenceday.entities.Enemy;
import com.hotguy.independenceday.entities.EnemyGrid;
import com.hotguy.independenceday.entities.EnemyRow;
import com.hotguy.independenceday.entities.Player;
import com.hotguy.independenceday.entities.Shield;
import com.hotguy.independenceday.entities.Ufo;
import com.badlogic.gdx.math.Intersector;

/**
 * Gestiona todas las detecciones de colisión del juego.
 *
 * <p>Centraliza la lógica de colisiones para mantener las entidades
 * desacopladas entre sí. Opera siempre contra las interfaces
 * {@link com.hotguy.independenceday.interfaces.Collidable} para
 * maximizar la flexibilidad.</p>
 *
 * <p>El orden de comprobación está optimizado para reducir el número
 * de checks innecesarios:</p>
 * <ol>
 *   <li>Balas del jugador contra OVNI (pocas balas, un solo objeto).</li>
 *   <li>Balas del jugador contra enemigos (pocas balas, grid filtrado).</li>
 *   <li>Balas del jugador contra escudos.</li>
 *   <li>Balas enemigas contra jugador.</li>
 *   <li>Balas enemigas contra escudos.</li>
 * </ol>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class CollisionManager {

    // ─────────────────────────────────────────────
    // REFERENCIAS
    // ─────────────────────────────────────────────

    /** Pool de balas activas en vuelo. */
    private final BulletPool bulletPool;

    /** Nave del jugador. */
    private final Player player;

    /** Batallón de enemigos. */
    private final EnemyGrid enemyGrid;

    /** Lista de escudos activos. */
    private final Array<Shield> shields;

    /** OVNI. */
    private final Ufo ufo;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el gestor de colisiones con referencias a todas las entidades.
     *
     * @param bulletPool Pool de balas activas.
     * @param player     Nave del jugador.
     * @param enemyGrid  Batallón de enemigos.
     * @param shields    Lista de escudos.
     * @param ufo        OVNI.
     */
    public CollisionManager(BulletPool bulletPool, Player player,
                            EnemyGrid enemyGrid, Array<Shield> shields, Ufo ufo) {
        this.bulletPool = bulletPool;
        this.player     = player;
        this.enemyGrid  = enemyGrid;
        this.shields    = shields;
        this.ufo        = ufo;
    }

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Ejecuta todas las comprobaciones de colisión del frame actual.
     *
     * <p>Debe llamarse una vez por frame desde {@code GameScreen.update()}
     * después de actualizar todas las entidades.</p>
     */
    public void update() {
        Array<Bullet> bullets = bulletPool.getActiveBullets();

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isActive()) continue;

            if (bullet.isFromPlayer()) {
                checkPlayerBullet(bullet);
            } else {
                checkEnemyBullet(bullet);
            }
        }
    }

    // ─────────────────────────────────────────────
    // BALAS DEL JUGADOR
    // ─────────────────────────────────────────────

    /**
     * Comprueba colisiones de una bala del jugador contra OVNI,
     * enemigos y escudos.
     *
     * @param bullet Bala del jugador a comprobar.
     */
    private void checkPlayerBullet(Bullet bullet) {
        // Contra OVNI
        if (ufo.isActive() && overlaps(bullet, ufo)) {
            bullet.onCollision(ufo);
            ufo.onCollision(bullet);
            return;
        }

        // Contra enemigos
        if (checkBulletVsGrid(bullet)) return;

        // Contra escudos
        checkBulletVsShields(bullet);
    }

    /**
     * Comprueba colisiones de una bala del jugador contra el grid de enemigos.
     *
     * <p>Itera de fila inferior a superior para dar prioridad a los enemigos
     * más cercanos al jugador, que son los más probables de impactar.</p>
     *
     * @param bullet Bala del jugador.
     * @return {@code true} si se produjo colisión con algún enemigo.
     */
    private boolean checkBulletVsGrid(Bullet bullet) {
        for (EnemyRow row : enemyGrid.getRows()) {
            if (row.isEmpty()) continue;
            for (Enemy enemy : row.getEnemies()) {
                if (!enemy.isActive()) continue;
                if (overlaps(bullet, enemy)) {
                    bullet.onCollision(enemy);
                    enemy.onCollision(bullet);
                    return true;
                }
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // BALAS ENEMIGAS
    // ─────────────────────────────────────────────

    /**
     * Comprueba colisiones de una bala enemiga contra el jugador
     * y contra los escudos.
     *
     * @param bullet Bala enemiga a comprobar.
     */
    private void checkEnemyBullet(Bullet bullet) {
        // Contra el jugador
        if (player.isActive() && overlaps(bullet, player)) {
            bullet.onCollision(player);
            player.onCollision(bullet);
            return;
        }

        // Contra escudos
        checkBulletVsShields(bullet);
    }

    // ─────────────────────────────────────────────
    // ESCUDOS
    // ─────────────────────────────────────────────

    /**
     * Comprueba colisiones de una bala (de cualquier origen) contra
     * todos los escudos activos.
     *
     * @param bullet Bala a comprobar.
     */
    private void checkBulletVsShields(Bullet bullet) {
        for (Shield shield : shields) {
            if (!shield.isActive()) continue;
            if (overlaps(bullet, shield)) {
                bullet.onCollision(shield);
                shield.onCollision(bullet);
                return;
            }
        }
    }

    // ─────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────

    /**
     * Comprueba si los rectángulos de colisión de dos entidades se solapan.
     *
     * @param a Primera entidad.
     * @param b Segunda entidad.
     * @return {@code true} si las hitboxes se solapan.
     */
    private boolean overlaps(
        com.hotguy.independenceday.interfaces.Collidable a,
        com.hotguy.independenceday.interfaces.Collidable b) {
        return Intersector.overlaps(a.getBounds(), b.getBounds());
    }
}
