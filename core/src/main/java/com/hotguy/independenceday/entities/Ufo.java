package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;
import com.badlogic.gdx.math.MathUtils;

/**
 * Representa el OVNI que cruza ocasionalmente por la parte superior
 * de la pantalla otorgando puntos extra al ser destruido.
 *
 * <p>Aparece desde un borde lateral aleatorio y cruza en línea recta
 * hasta desaparecer por el borde opuesto. La puntuación que otorga
 * es aleatoria, múltiplo de {@link Constants#SCORE_UFO_BASE},
 * añadiendo un elemento de sorpresa al juego.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class Ufo extends LivingEntity {

    // ─────────────────────────────────────────────
    // MOVIMIENTO
    // ─────────────────────────────────────────────

    /** Velocidad horizontal: positiva = izquierda a derecha, negativa = al revés. */
    private float velocityX;

    // ─────────────────────────────────────────────
    // PUNTUACIÓN
    // ─────────────────────────────────────────────

    /** Puntos que otorga este OVNI al ser destruido. Se fija al aparecer. */
    private int scoreValue;

    // ─────────────────────────────────────────────
    // CALLBACKS
    // ─────────────────────────────────────────────

    /** Listener para notificar eventos del OVNI al GameScreen. */
    private UfoListener listener;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el OVNI en estado inactivo.
     *
     * <p>Para hacerlo aparecer en pantalla usar {@link #spawn()}.</p>
     */
    public Ufo() {
        super(0, Constants.UFO_Y, Constants.UFO_WIDTH, Constants.UFO_HEIGHT, 1);
        this.active = false;

        TextureRegion region = Assets.atlas.findRegion("ufo");
        if (region != null) setTextureRegion(region);
    }

    // ─────────────────────────────────────────────
    // APARICIÓN
    // ─────────────────────────────────────────────

    /**
     * Hace aparecer el OVNI desde un borde lateral aleatorio
     * con una puntuación aleatoria.
     *
     * <p>Debe llamarse desde el temporizador gestionado por {@code GameScreen}.</p>
     */
    public void spawn() {
        // Dirección aleatoria
        boolean fromLeft = MathUtils.randomBoolean();
        velocityX = fromLeft ? Constants.UFO_SPEED : -Constants.UFO_SPEED;
        x = fromLeft ? -width : Constants.WORLD_WIDTH;
        y = Constants.UFO_Y;

        // Puntuación aleatoria: múltiplo de SCORE_UFO_BASE entre 1x y 6x
        scoreValue = Constants.SCORE_UFO_BASE * MathUtils.random(1, 6);

        // Resetear salud y activar
        health = maxHealth;
        active = true;

        if (Assets.sfxUfo != null) {
            Assets.sfxUfo.loop(0.5f);
        }
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * Mueve el OVNI horizontalmente y lo desactiva al salir de pantalla.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        if (!active) return;
        super.update(delta);

        x += velocityX * delta;

        // Salida por borde derecho o izquierdo
        boolean exitRight = velocityX > 0 && x > Constants.WORLD_WIDTH;
        boolean exitLeft  = velocityX < 0 && x + width < 0;

        if (exitRight || exitLeft) {
            active = false;
            stopSound();
        }
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Llamado al recibir el impacto de una bala del jugador.
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
     * Lógica ejecutada al destruir el OVNI.
     * Notifica la puntuación al listener y detiene el sonido.
     */
    @Override
    protected void onDeath() {
        stopSound();
        if (listener != null) {
            listener.onUfoDestroyed(scoreValue);
        }
        if (Assets.sfxExplosion != null) {
            Assets.sfxExplosion.play();
        }
    }

    // ─────────────────────────────────────────────
    // AUDIO
    // ─────────────────────────────────────────────

    /**
     * Detiene el sonido del OVNI si estaba reproduciéndose.
     */
    private void stopSound() {
        if (Assets.sfxUfo != null) {
            Assets.sfxUfo.stop();
        }
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve la puntuación que otorga este OVNI al ser destruido.
     *
     * @return Puntos del OVNI fijados al aparecer.
     */
    public int getScoreValue() { return scoreValue; }

    /**
     * Asigna el listener de eventos del OVNI.
     *
     * @param listener Implementación de {@link UfoListener}.
     */
    public void setListener(UfoListener listener) {
        this.listener = listener;
    }

    // ─────────────────────────────────────────────
    // LISTENER
    // ─────────────────────────────────────────────

    /**
     * Interfaz de callbacks para notificar eventos del OVNI
     * sin acoplamiento directo con las pantallas.
     *
     * @author Javier Botella
     * @version 1.0
     */
    public interface UfoListener {

        /**
         * Llamado cuando el OVNI es destruido por el jugador.
         *
         * @param score Puntos obtenidos por destruir el OVNI.
         */
        void onUfoDestroyed(int score);
    }
}
