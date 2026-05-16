package com.hotguy.independenceday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hotguy.independenceday.SpaceInvadersGame;
import com.hotguy.independenceday.managers.ScoreManager;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Pantalla de fin de partida.
 *
 * <p>Muestra la puntuación final, el high score y si se ha batido
 * el récord. Permite al jugador volver al menú o reiniciar la partida
 * directamente. Incluye animación de entrada con interpolación.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class GameOverScreen extends ScreenAdapter {

    // ─────────────────────────────────────────────
    // REFERENCIAS
    // ─────────────────────────────────────────────

    /** Referencia al juego principal para cambiar de pantalla. */
    private final SpaceInvadersGame game;

    /** Gestor de puntuación con los datos de la partida finalizada. */
    private final ScoreManager scoreManager;

    // ─────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────

    /** Stage que contiene todos los widgets de la pantalla. */
    private Stage stage;

    /** Tabla raíz que organiza el layout. */
    private Table root;

    // ─────────────────────────────────────────────
    // ANIMACIÓN DE ENTRADA
    // ─────────────────────────────────────────────

    /** Temporizador de la animación de entrada. */
    private float animTimer;

    /** Duración total de la animación de entrada (segundos). */
    private static final float ANIM_DURATION = 0.6f;

    // ─────────────────────────────────────────────
    // FADE
    // ─────────────────────────────────────────────

    /** Opacidad actual del fade de transición (0 = negro, 1 = visible). */
    private float fadeAlpha;

    /** Duración del fade de entrada (segundos). */
    private static final float FADE_DURATION = Constants.TRANSITION_TIME;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea la pantalla de game over con los datos de la partida.
     *
     * @param game         Instancia principal del juego.
     * @param scoreManager Gestor con la puntuación de la partida finalizada.
     */
    public GameOverScreen(SpaceInvadersGame game, ScoreManager scoreManager) {
        this.game         = game;
        this.scoreManager = scoreManager;
    }

    // ─────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────

    /**
     * Inicializa el Stage, construye el layout y guarda el high score.
     */
    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        scoreManager.saveHighScore();

        animTimer = 0f;
        fadeAlpha = 0f;

        buildLayout();
    }

    /**
     * Actualiza la animación, el fade y el input cada frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animTimer += delta;
        updateFade(delta);
        updateInput();

        // Animar posición Y de la tabla con interpolación elástica
        float progress = Math.min(animTimer / ANIM_DURATION, 1f);
        float targetY  = Constants.WORLD_HEIGHT * 0.5f;
        float startY   = Constants.WORLD_HEIGHT * 1.2f;
        root.setY(startY + (targetY - startY) * Interpolation.swingOut.apply(progress));

        stage.act(delta);
        stage.draw();

        drawFade();
    }

    /**
     * Adapta el viewport al nuevo tamaño de pantalla.
     *
     * @param width  Nuevo ancho en píxeles.
     * @param height Nuevo alto en píxeles.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Libera el Stage al salir de la pantalla.
     */
    @Override
    public void hide() {
        dispose();
    }

    /**
     * Libera los recursos de la pantalla.
     */
    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }

    // ─────────────────────────────────────────────
    // LAYOUT
    // ─────────────────────────────────────────────

    /**
     * Construye el layout de la pantalla con Labels y Table de Scene2D.
     */
    private void buildLayout() {
        root = new Table();
        root.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        root.setPosition(0, Constants.WORLD_HEIGHT * 1.2f); // empieza fuera
        root.center();

        // Título
        Label.LabelStyle titleStyle = new Label.LabelStyle(Assets.fontTitle, null);
        Label titleLabel = new Label("GAME OVER", titleStyle);
        titleLabel.setAlignment(Align.center);

        // Puntuación
        Label.LabelStyle hudStyle = new Label.LabelStyle(Assets.fontHud, null);
        Label scoreLabel = new Label(
            "PUNTUACION: " + scoreManager.getScore(), hudStyle);
        scoreLabel.setAlignment(Align.center);

        // High score
        String highScoreText = scoreManager.isNewHighScore()
            ? "NUEVO RECORD: " + scoreManager.getHighScore()
            : "RECORD: "       + scoreManager.getHighScore();
        Label highScoreLabel = new Label(highScoreText, hudStyle);
        highScoreLabel.setAlignment(Align.center);

        // Nivel alcanzado
        Label levelLabel = new Label(
            "NIVEL: " + scoreManager.getLevel(), hudStyle);
        levelLabel.setAlignment(Align.center);

        // Instrucciones
        Label.LabelStyle menuStyle = new Label.LabelStyle(Assets.fontMenu, null);
        Label retryLabel = new Label("[ESPACIO] REINTENTAR", menuStyle);
        retryLabel.setAlignment(Align.center);
        Label menuLabel = new Label("[ESC] MENU", menuStyle);
        menuLabel.setAlignment(Align.center);

        // Ensamblar tabla
        root.add(titleLabel).padBottom(40).row();
        root.add(scoreLabel).padBottom(16).row();
        root.add(highScoreLabel).padBottom(16).row();
        root.add(levelLabel).padBottom(40).row();
        root.add(retryLabel).padBottom(12).row();
        root.add(menuLabel).row();

        stage.addActor(root);
    }

    // ─────────────────────────────────────────────
    // INPUT
    // ─────────────────────────────────────────────

    /**
     * Comprueba el input para reiniciar o volver al menú.
     */
    private void updateInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.justTouched()) {
            transitionTo(new GameScreen(game, scoreManager));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(new MenuScreen(game));
        }
    }

    // ─────────────────────────────────────────────
    // FADE
    // ─────────────────────────────────────────────

    /**
     * Incrementa el alpha del fade de entrada cada frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateFade(float delta) {
        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(fadeAlpha + delta / FADE_DURATION, 1f);
            stage.getRoot().setColor(1f, 1f, 1f, fadeAlpha);
        }
    }

    /**
     * Dibuja un rectángulo negro semitransparente sobre el stage
     * durante el fade de entrada.
     */
    private void drawFade() {
        if (fadeAlpha >= 1f) return;
        SpriteBatch batch = game.batch;
        batch.begin();
        batch.setColor(0f, 0f, 0f, 1f - fadeAlpha);
        batch.draw(Assets.atlas.findRegion("pixel"),
            0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /**
     * Navega a la siguiente pantalla.
     *
     * @param screen Pantalla de destino.
     */
    private void transitionTo(com.badlogic.gdx.Screen screen) {
        game.setScreen(screen);
    }
}
