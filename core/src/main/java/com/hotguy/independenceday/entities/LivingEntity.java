package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase abstracta intermedia para entidades con salud y daño.
 *
 * <p>Extiende {@link Entity} añadiendo un sistema de puntos de vida
 * y un efecto visual de flash blanco al recibir impacto, que aporta
 * ese toque de modernidad sin romper la estética retro.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public abstract class LivingEntity extends Entity {

    // ─────────────────────────────────────────────
    // SALUD
    // ─────────────────────────────────────────────

    /** Puntos de vida actuales de la entidad. */
    protected int health;

    /** Puntos de vida máximos de la entidad. */
    protected int maxHealth;

    // ─────────────────────────────────────────────
    // FLASH DE IMPACTO
    // ─────────────────────────────────────────────

    /** Duración total del efecto de flash al recibir daño (segundos). */
    private static final float FLASH_DURATION = 0.12f;

    /** Temporizador actual del flash; 0 significa sin flash activo. */
    private float flashTimer;

    /** Color blanco reutilizable para el efecto de flash. */
    private static final Color FLASH_COLOR = Color.WHITE;

    /** Color original de la entidad, restaurado tras el flash. */
    private final Color originalColor = new Color(Color.WHITE);

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Inicializa la entidad viva con posición, dimensiones y salud.
     *
     * @param x         Posición horizontal inicial.
     * @param y         Posición vertical inicial.
     * @param width     Ancho de la entidad.
     * @param height    Alto de la entidad.
     * @param maxHealth Puntos de vida máximos (y salud inicial).
     */
    public LivingEntity(float x, float y, float width, float height, int maxHealth) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.health    = maxHealth;
        this.flashTimer = 0f;
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * Actualiza el temporizador del flash de impacto.
     *
     * <p>Las subclases deben llamar a {@code super.update(delta)}
     * para que el efecto de flash funcione correctamente.</p>
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        if (flashTimer > 0f) {
            flashTimer -= delta;
            if (flashTimer < 0f) flashTimer = 0f;
        }
    }

    // ─────────────────────────────────────────────
    // RENDERABLE
    // ─────────────────────────────────────────────

    /**
     * Renderiza la entidad aplicando el flash blanco si está activo.
     *
     * <p>Modifica temporalmente el color del batch durante el flash
     * y lo restaura inmediatamente después para no afectar a otros
     * objetos renderizados en el mismo frame.</p>
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;

        if (flashTimer > 0f) {
            // Intensidad del flash proporcional al tiempo restante
            float intensity = flashTimer / FLASH_DURATION;
            batch.setColor(
                originalColor.r + (FLASH_COLOR.r - originalColor.r) * intensity,
                originalColor.g + (FLASH_COLOR.g - originalColor.g) * intensity,
                originalColor.b + (FLASH_COLOR.b - originalColor.b) * intensity,
                1f
            );
        }

        super.render(batch);

        // Restaurar color original siempre
        batch.setColor(originalColor);
    }

    // ─────────────────────────────────────────────
    // SALUD Y DAÑO
    // ─────────────────────────────────────────────

    /**
     * Aplica daño a la entidad y activa el flash de impacto.
     *
     * <p>Si la salud llega a cero, la entidad se desactiva
     * y se llama a {@link #onDeath()} para que cada subclase
     * gestione su propia lógica de muerte.</p>
     *
     * @param amount Cantidad de daño a aplicar (valor positivo).
     */
    public void takeDamage(int amount) {
        if (!active) return;

        health -= amount;
        flashTimer = FLASH_DURATION;

        if (health <= 0) {
            health = 0;
            active = false;
            onDeath();
        }
    }

    /**
     * Llamado automáticamente cuando la salud llega a cero.
     *
     * <p>Las subclases implementan aquí su lógica de muerte:
     * reproducir sonido, lanzar partículas, sumar puntuación, etc.</p>
     */
    protected abstract void onDeath();

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve los puntos de vida actuales.
     *
     * @return Salud actual de la entidad.
     */
    public int getHealth() { return health; }

    /**
     * Devuelve los puntos de vida máximos.
     *
     * @return Salud máxima de la entidad.
     */
    public int getMaxHealth() { return maxHealth; }

    /**
     * Indica si la entidad sigue con vida.
     *
     * @return {@code true} si la salud es mayor que cero y está activa.
     */
    public boolean isAlive() { return active && health > 0; }
}
