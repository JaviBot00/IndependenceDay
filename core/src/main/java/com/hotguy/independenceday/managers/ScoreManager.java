package com.hotguy.independenceday.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.hotguy.independenceday.utils.Constants;

/**
 * Gestiona la puntuación, el nivel y el high score de la partida.
 *
 * <p>Centraliza toda la lógica relacionada con los puntos para que
 * ninguna entidad tenga que conocer el sistema de puntuación directamente.
 * El high score se persiste entre sesiones mediante {@link Preferences}
 * de libGDX, que funciona tanto en desktop como en Android.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class ScoreManager {

    // ─────────────────────────────────────────────
    // PUNTUACIÓN
    // ─────────────────────────────────────────────

    /** Puntuación acumulada en la partida actual. */
    private int score;

    /** Máxima puntuación registrada entre todas las partidas. */
    private int highScore;

    // ─────────────────────────────────────────────
    // NIVEL
    // ─────────────────────────────────────────────

    /** Nivel actual de la partida (empieza en 1). */
    private int level;

    // ─────────────────────────────────────────────
    // PERSISTENCIA
    // ─────────────────────────────────────────────

    /** Preferencias de libGDX para persistir el high score. */
    private final Preferences prefs;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el gestor de puntuación y carga el high score guardado.
     */
    public ScoreManager() {
        prefs     = Gdx.app.getPreferences(Constants.PREFS_NAME);
        highScore = prefs.getInteger(Constants.PREFS_HIGHSCORE, 0);
        score     = 0;
        level     = 1;
    }

    // ─────────────────────────────────────────────
    // PUNTUACIÓN
    // ─────────────────────────────────────────────

    /**
     * Añade puntos a la puntuación actual y actualiza el high score
     * si se supera el récord previo.
     *
     * @param points Puntos a añadir (valor positivo).
     */
    public void addScore(int points) {
        score += points;
        if (score > highScore) {
            highScore = score;
        }
    }

    /**
     * Reinicia la puntuación y el nivel para una nueva partida.
     *
     * <p>No reinicia el high score, que persiste entre partidas.</p>
     */
    public void reset() {
        score = 0;
        level = 1;
    }

    /**
     * Avanza al siguiente nivel incrementando el contador.
     */
    public void nextLevel() {
        level++;
    }

    // ─────────────────────────────────────────────
    // PERSISTENCIA
    // ─────────────────────────────────────────────

    /**
     * Guarda el high score actual en las preferencias del dispositivo.
     *
     * <p>Debe llamarse al finalizar la partida o al cerrar la aplicación
     * para no perder el récord.</p>
     */
    public void saveHighScore() {
        prefs.putInteger(Constants.PREFS_HIGHSCORE, highScore);
        prefs.flush();
        Gdx.app.log("ScoreManager", "High score guardado: " + highScore);
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    /**
     * Devuelve la puntuación actual de la partida.
     *
     * @return Puntuación acumulada.
     */
    public int getScore() { return score; }

    /**
     * Devuelve el high score histórico.
     *
     * @return Máxima puntuación registrada.
     */
    public int getHighScore() { return highScore; }

    /**
     * Devuelve el nivel actual de la partida.
     *
     * @return Nivel actual (empieza en 1).
     */
    public int getLevel() { return level; }

    /**
     * Indica si la puntuación actual supera el high score previo.
     *
     * @return {@code true} si se está batiendo el récord en esta partida.
     */
    public boolean isNewHighScore() { return score >= highScore && score > 0; }
}
