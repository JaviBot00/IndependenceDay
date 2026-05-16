package com.hotguy.independenceday.entities;

/**
 * Enumera los tres tipos de enemigos del juego, cada uno asociado
 * a una puntuación, una clave de atlas y las filas que ocupa.
 *
 * <p>El tipo determina la skin del sprite, la animación y los puntos
 * que otorga al jugador al ser destruido. Las filas superiores
 * son más valiosas, siguiendo la mecánica del Space Invaders original.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public enum EnemyType {

    /**
     * Enemigo de las dos filas inferiores.
     * El más común y el de menor valor.
     */
    A(10, "enemy_a"),

    /**
     * Enemigo de las dos filas centrales.
     * Valor intermedio.
     */
    B(20, "enemy_b"),

    /**
     * Enemigo de la fila superior.
     * El más valioso y el primero en aparecer visualmente.
     */
    C(30, "enemy_c");

    // ─────────────────────────────────────────────
    // ATRIBUTOS
    // ─────────────────────────────────────────────

    /** Puntos que otorga al jugador al ser destruido. */
    public final int score;

    /**
     * Clave del sprite en el atlas de texturas.
     * Los frames de animación se buscan como {@code atlasKey}_0 y {@code atlasKey}_1.
     */
    public final String atlasKey;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Define un tipo de enemigo con su puntuación y clave de atlas.
     *
     * @param score     Puntos otorgados al destruir este tipo de enemigo.
     * @param atlasKey  Clave base del sprite en el atlas de texturas.
     */
    EnemyType(int score, String atlasKey) {
        this.score    = score;
        this.atlasKey = atlasKey;
    }
}
