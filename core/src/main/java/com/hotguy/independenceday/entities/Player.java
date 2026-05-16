package com.hotguy.independenceday.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Representa la nave del jugador.
 *
 * <p>Gestiona el movimiento horizontal, el cooldown de disparo,
 * las vidas y la invulnerabilidad temporal tras recibir daño.
 * El input se lee directamente desde esta clase para mantener
 * la lógica del jugador autocontenida.</p>
 *
 * <p>En Android el movimiento se controla mediante touch/drag
 * horizontal; en desktop mediante las teclas de dirección o A/D.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class Player extends LivingEntity {

    // ─────────────────────────────────────────────
    // VIDAS
    // ─────────────────────────────────────────────

    /** Número de vidas restantes del jugador. */
    private int lives;

    // ─────────────────────────────────────────────
    // DISPARO
    // ─────────────────────────────────────────────

    /** Temporizador acumulado desde el último disparo. */
    private float shootCooldownTimer;

    /** Indica si el jugador ha solicitado disparar en este frame. */
    private boolean shootRequested;

    // ─────────────────────────────────────────────
    // INVULNERABILIDAD
    // ─────────────────────────────────────────────

    /** Duración de la invulnerabilidad tras recibir daño (segundos). */
    private static final float INVULNERABLE_DURATION = 2.0f;

    /** Temporizador de invulnerabilidad restante. */
    private float invulnerableTimer;

    /** Frecuencia de parpadeo durante la invulnerabilidad (veces/segundo). */
    private static final float BLINK_FREQUENCY = 8f;

    // ─────────────────────────────────────────────
    // INPUT ANDROID
    // ─────────────────────────────────────────────

    /** Posición X del último toque registrado en Android. */
    private float lastTouchX;

    /** Indica si hay un toque activo en pantalla. */
    private boolean touching;

    // ─────────────────────────────────────────────
    // CALLBACKS
    // ─────────────────────────────────────────────

    /** Listener para notificar eventos del jugador al GameScreen. */
    private PlayerListener listener;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea la nave del jugador centrada horizontalmente en la posición Y fija.
     */
    public Player() {
        super(
            (Constants.WORLD_WIDTH - Constants.PLAYER_WIDTH) * 0.5f,
            Constants.PLAYER_Y,
            Constants.PLAYER_WIDTH,
            Constants.PLAYER_HEIGHT,
            1
        );
        this.lives               = Constants.PLAYER_LIVES;
        this.shootCooldownTimer  = 0f;
        this.invulnerableTimer   = 0f;
        this.touching            = false;

        // Asignar textura desde el atlas
        TextureRegion region = Assets.atlas.findRegion("player");
        if (region != null) setTextureRegion(region);
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * Actualiza el input, el movimiento, el cooldown de disparo
     * y el temporizador de invulnerabilidad.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        updateInvulnerability(delta);
        updateInput(delta);
        updateShootCooldown(delta);
        clampPosition();
    }

    /**
     * Decrementa el temporizador de invulnerabilidad.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateInvulnerability(float delta) {
        if (invulnerableTimer > 0f) {
            invulnerableTimer -= delta;
            if (invulnerableTimer < 0f) invulnerableTimer = 0f;
        }
    }

    /**
     * Lee el input de teclado (desktop) y táctil (Android)
     * y actualiza la posición y el flag de disparo.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateInput(float delta) {
        shootRequested = false;

        // ── Desktop: teclado ──────────────────────
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= Constants.PLAYER_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += Constants.PLAYER_SPEED * delta;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            shootRequested = true;
        }

        // ── Android: táctil ───────────────────────
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();

            if (touching) {
                // Mover proporcionalmente al desplazamiento del dedo
                float deltaX = touchX - lastTouchX;
                x += deltaX * 1.2f;
            }
            lastTouchX = touchX;
            touching   = true;
        } else {
            if (touching) {
                // Al soltar el dedo se dispara
                shootRequested = true;
            }
            touching = false;
        }
    }

    /**
     * Actualiza el cooldown de disparo y notifica al listener
     * cuando el jugador puede y quiere disparar.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateShootCooldown(float delta) {
        if (shootCooldownTimer > 0f) {
            shootCooldownTimer -= delta;
        }

        if (shootRequested && shootCooldownTimer <= 0f) {
            shootCooldownTimer = Constants.PLAYER_SHOOT_COOLDOWN;
            if (listener != null) {
                float bulletX = x + width * 0.5f;
                float bulletY = y + height;
                listener.onPlayerShoot(bulletX, bulletY);
            }
            if (Assets.sfxShoot != null) {
                Assets.sfxShoot.play(0.7f);
            }
        }
    }

    /**
     * Mantiene al jugador dentro de los límites horizontales de la pantalla.
     */
    private void clampPosition() {
        if (x < 0) x = 0;
        if (x + width > Constants.WORLD_WIDTH) x = Constants.WORLD_WIDTH - width;
    }

    // ─────────────────────────────────────────────
    // RENDERABLE
    // ─────────────────────────────────────────────

    /**
     * Renderiza el jugador aplicando el parpadeo durante la invulnerabilidad.
     *
     * <p>Durante la invulnerabilidad la nave parpadea alternando
     * su visibilidad a {@link #BLINK_FREQUENCY} veces por segundo.</p>
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;

        if (invulnerableTimer > 0f) {
            // Parpadeo: visible/invisible alternado
            float blinkPhase = invulnerableTimer * BLINK_FREQUENCY;
            if ((int) blinkPhase % 2 == 0) return;
        }

        super.render(batch);
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Llamado al recibir un impacto de una bala enemiga.
     *
     * <p>Si el jugador no es invulnerable, pierde una vida y
     * activa la invulnerabilidad temporal.</p>
     *
     * @param other El objeto con el que se ha producido la colisión.
     */
    @Override
    public void onCollision(Collidable other) {
        if (invulnerableTimer > 0f) return;

        if (other instanceof Bullet) {
            Bullet bullet = (Bullet) other;
            if (!bullet.isFromPlayer()) {
                loseLife();
            }
        }

        if (other instanceof Enemy) {
            loseLife();
        }
    }

    /**
     * Resta una vida al jugador y gestiona la muerte o invulnerabilidad.
     */
    private void loseLife() {
        lives--;
        invulnerableTimer = INVULNERABLE_DURATION;

        if (Assets.sfxPlayerDeath != null) {
            Assets.sfxPlayerDeath.play();
        }

        if (lives <= 0) {
            lives  = 0;
            active = false;
            if (listener != null) listener.onPlayerDead();
        } else {
            if (listener != null) listener.onPlayerLostLife(lives);
        }
    }

    // ─────────────────────────────────────────────
    // LIVING ENTITY
    // ─────────────────────────────────────────────

    /**
     * No usado directamente — la muerte del jugador se gestiona
     * mediante el sistema de vidas en {@link #loseLife()}.
     */
    @Override
    protected void onDeath() {}

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve el número de vidas restantes.
     *
     * @return Vidas actuales del jugador.
     */
    public int getLives() { return lives; }

    /**
     * Indica si el jugador es actualmente invulnerable.
     *
     * @return {@code true} si está en período de invulnerabilidad.
     */
    public boolean isInvulnerable() { return invulnerableTimer > 0f; }

    /**
     * Asigna el listener de eventos del jugador.
     *
     * @param listener Implementación de {@link PlayerListener}.
     */
    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    // ─────────────────────────────────────────────
    // LISTENER
    // ─────────────────────────────────────────────

    /**
     * Interfaz de callbacks para notificar eventos del jugador
     * sin acoplamiento directo con las pantallas.
     *
     * @author Javier Botella
     * @version 1.0
     */
    public interface PlayerListener {

        /**
         * Llamado cuando el jugador dispara.
         *
         * @param x Posición X del centro del cañón.
         * @param y Posición Y del borde superior de la nave.
         */
        void onPlayerShoot(float x, float y);

        /**
         * Llamado cuando el jugador pierde una vida pero sigue vivo.
         *
         * @param livesRemaining Vidas restantes tras la pérdida.
         */
        void onPlayerLostLife(int livesRemaining);

        /**
         * Llamado cuando el jugador pierde su última vida.
         */
        void onPlayerDead();
    }
}
