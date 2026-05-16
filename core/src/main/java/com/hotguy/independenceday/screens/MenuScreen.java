package com.hotguy.independenceday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hotguy.independenceday.SpaceInvadersGame;
import com.hotguy.independenceday.managers.ScoreManager;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Pantalla de menú principal del juego.
 *
 * <p>Muestra el título, el high score y las instrucciones para comenzar.
 * Incluye un fondo de estrellas con efecto parallax de dos capas y una
 * fila de aliens animados desfilando por la pantalla como guiño al jugador.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class MenuScreen extends ScreenAdapter {

    // ─────────────────────────────────────────────
    // REFERENCIAS
    // ─────────────────────────────────────────────

    /** Referencia al juego principal para cambiar de pantalla. */
    private final SpaceInvadersGame game;

    // ─────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────

    /** Stage que contiene todos los widgets de la pantalla. */
    private Stage stage;

    /** Tabla raíz que organiza el layout. */
    private Table root;

    // ─────────────────────────────────────────────
    // PARALLAX
    // ─────────────────────────────────────────────

    /** Posiciones X de las estrellas de la capa lenta. */
    private float[] starsSlowX;

    /** Posiciones Y de las estrellas de la capa lenta. */
    private float[] starsSlowY;

    /** Posiciones X de las estrellas de la capa rápida. */
    private float[] starsFastX;

    /** Posiciones Y de las estrellas de la capa rápida. */
    private float[] starsFastY;

    // ─────────────────────────────────────────────
    // ALIENS DECORATIVOS
    // ─────────────────────────────────────────────

    /** Posición X del desfile de aliens decorativos. */
    private float alienParadeX;

    /** Posición Y del desfile de aliens decorativos. */
    private static final float ALIEN_PARADE_Y = 160f;

    /** Tiempo acumulado para la animación de los aliens decorativos. */
    private float alienStateTime;

    /** Número de aliens en el desfile decorativo. */
    private static final int ALIEN_PARADE_COUNT = 8;

    /** Separación entre aliens del desfile. */
    private static final float ALIEN_PARADE_SPACING = 60f;

    // ─────────────────────────────────────────────
    // FADE
    // ─────────────────────────────────────────────

    /** Alpha del fade de entrada (0 = negro, 1 = visible). */
    private float fadeAlpha;

    /** Duración del fade de entrada. */
    private static final float FADE_DURATION = Constants.TRANSITION_TIME;

    // ─────────────────────────────────────────────
    // PARPADEO DEL TEXTO "PULSE PARA JUGAR"
    // ─────────────────────────────────────────────

    /** Temporizador acumulado para el parpadeo del texto de inicio. */
    private float blinkTimer;

    /** Visibilidad actual del texto de inicio. */
    private boolean blinkVisible;

    /** Label del texto de inicio que parpadea. */
    private Label startLabel;

    // ─────────────────────────────────────────────
    // SCORE MANAGER
    // ─────────────────────────────────────────────

    /** Gestor de puntuación para mostrar el high score. */
    private final ScoreManager scoreManager;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea el menú principal con un ScoreManager propio.
     *
     * @param game Instancia principal del juego.
     */
    public MenuScreen(SpaceInvadersGame game) {
        this.game         = game;
        this.scoreManager = new ScoreManager();
    }

    // ─────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────

    /**
     * Inicializa el Stage, las estrellas y el desfile de aliens.
     */
    @Override
    public void show() {
        stage     = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        fadeAlpha = 0f;
        blinkTimer   = 0f;
        blinkVisible = true;

        initStars();
        initAlienParade();
        buildLayout();

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Actualiza y renderiza todos los elementos del menú cada frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateFade(delta);
        updateStars(delta);
        updateAlienParade(delta);
        updateBlink(delta);
        updateInput();

        drawBackground();
        drawAlienParade();

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
    // INICIALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Genera las posiciones aleatorias de las estrellas de ambas capas.
     */
    private void initStars() {
        starsSlowX = new float[Constants.STARS_LAYER_SLOW];
        starsSlowY = new float[Constants.STARS_LAYER_SLOW];
        starsFastX = new float[Constants.STARS_LAYER_FAST];
        starsFastY = new float[Constants.STARS_LAYER_FAST];

        for (int i = 0; i < Constants.STARS_LAYER_SLOW; i++) {
            starsSlowX[i] = MathUtils.random(0f, Constants.WORLD_WIDTH);
            starsSlowY[i] = MathUtils.random(0f, Constants.WORLD_HEIGHT);
        }
        for (int i = 0; i < Constants.STARS_LAYER_FAST; i++) {
            starsFastX[i] = MathUtils.random(0f, Constants.WORLD_WIDTH);
            starsFastY[i] = MathUtils.random(0f, Constants.WORLD_HEIGHT);
        }
    }

    /**
     * Inicializa la posición del desfile de aliens decorativos.
     */
    private void initAlienParade() {
        alienParadeX  = Constants.WORLD_WIDTH;
        alienStateTime = 0f;
    }

    // ─────────────────────────────────────────────
    // LAYOUT
    // ─────────────────────────────────────────────

    /**
     * Construye el layout de la pantalla con Labels y Table de Scene2D.
     */
    private void buildLayout() {
        root = new Table();
        root.setFillParent(true);
        root.center().top().padTop(60f);

        // Título
        Label.LabelStyle titleStyle = new Label.LabelStyle(Assets.fontTitle, null);
        Label titleLabel = new Label("SPACE INVADERS", titleStyle);
        titleLabel.setAlignment(Align.center);

        // Subtítulo
        Label.LabelStyle hudStyle = new Label.LabelStyle(Assets.fontHud, null);
        Label subLabel = new Label("INDEPENDENCE DAY", hudStyle);
        subLabel.setAlignment(Align.center);

        // High score
        Label highScoreLabel = new Label(
            "RECORD: " + scoreManager.getHighScore(), hudStyle);
        highScoreLabel.setAlignment(Align.center);

        // Tabla de puntuaciones por tipo
        Label scoreTableLabel = new Label(
            "= PUNTUACIONES =\n" +
                "  ENEMIGO A  ...  " + Constants.SCORE_ENEMY_A + " PTS\n" +
                "  ENEMIGO B  ...  " + Constants.SCORE_ENEMY_B + " PTS\n" +
                "  ENEMIGO C  ...  " + Constants.SCORE_ENEMY_C + " PTS\n" +
                "  OVNI       ...  ??? PTS",
            hudStyle);
        scoreTableLabel.setAlignment(Align.center);

        // Texto de inicio (parpadeante)
        Label.LabelStyle menuStyle = new Label.LabelStyle(Assets.fontMenu, null);
        startLabel = new Label("PULSE ESPACIO PARA JUGAR", menuStyle);
        startLabel.setAlignment(Align.center);

        // Ensamblar
        root.add(titleLabel).padBottom(8).row();
        root.add(subLabel).padBottom(40).row();
        root.add(highScoreLabel).padBottom(40).row();
        root.add(scoreTableLabel).padBottom(50).row();
        root.add(startLabel).row();

        stage.addActor(root);
    }

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Desplaza las estrellas verticalmente hacia abajo para simular
     * el movimiento de la nave en el espacio.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateStars(float delta) {
        for (int i = 0; i < Constants.STARS_LAYER_SLOW; i++) {
            starsSlowY[i] -= Constants.STARS_SPEED_SLOW * delta;
            if (starsSlowY[i] < 0) {
                starsSlowY[i] = Constants.WORLD_HEIGHT;
                starsSlowX[i] = MathUtils.random(0f, Constants.WORLD_WIDTH);
            }
        }
        for (int i = 0; i < Constants.STARS_LAYER_FAST; i++) {
            starsFastY[i] -= Constants.STARS_SPEED_FAST * delta;
            if (starsFastY[i] < 0) {
                starsFastY[i] = Constants.WORLD_HEIGHT;
                starsFastX[i] = MathUtils.random(0f, Constants.WORLD_WIDTH);
            }
        }
    }

    /**
     * Mueve el desfile de aliens de derecha a izquierda.
     * Cuando sale por el borde izquierdo reaparece por la derecha.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateAlienParade(float delta) {
        alienStateTime += delta;
        alienParadeX   -= Constants.ENEMY_SPEED_INIT * delta;
        float totalWidth = ALIEN_PARADE_COUNT * ALIEN_PARADE_SPACING;
        if (alienParadeX + totalWidth < 0) {
            alienParadeX = Constants.WORLD_WIDTH;
        }
    }

    /**
     * Alterna la visibilidad del texto de inicio cada 0.5 segundos.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateBlink(float delta) {
        blinkTimer += delta;
        if (blinkTimer >= 0.5f) {
            blinkTimer   = 0f;
            blinkVisible = !blinkVisible;
            startLabel.setVisible(blinkVisible);
        }
    }

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
     * Comprueba el input para iniciar la partida.
     */
    private void updateInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.justTouched()) {
            ScoreManager newScore = new ScoreManager();
            game.setScreen(new GameScreen(game, newScore));
        }
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Dibuja el fondo negro con las estrellas de las dos capas parallax.
     */
    private void drawBackground() {
        SpriteBatch batch = game.batch;
        batch.begin();

        // Capa lenta: estrellas pequeñas y tenues
        batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        for (int i = 0; i < Constants.STARS_LAYER_SLOW; i++) {
            batch.draw(Assets.atlas.findRegion("pixel"),
                starsSlowX[i], starsSlowY[i], 1f, 1f);
        }

        // Capa rápida: estrellas más brillantes y algo mayores
        batch.setColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < Constants.STARS_LAYER_FAST; i++) {
            batch.draw(Assets.atlas.findRegion("pixel"),
                starsFastX[i], starsFastY[i], 2f, 2f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /**
     * Dibuja el desfile de aliens animados en la parte inferior del menú.
     */
    private void drawAlienParade() {
        SpriteBatch batch = game.batch;
        batch.begin();

        com.badlogic.gdx.graphics.g2d.TextureRegion frame =
            new com.badlogic.gdx.graphics.g2d.Animation<>(
                Constants.ENEMY_ANIM_FRAME,
                new Array<>(new com.badlogic.gdx.graphics.g2d.TextureRegion[]{
                    Assets.atlas.findRegion("enemy_b", 0),
                    Assets.atlas.findRegion("enemy_b", 1)
                })
            ).getKeyFrame(alienStateTime, true);

        for (int i = 0; i < ALIEN_PARADE_COUNT; i++) {
            float ax = alienParadeX + i * ALIEN_PARADE_SPACING;
            batch.draw(frame, ax, ALIEN_PARADE_Y,
                Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT);
        }

        batch.end();
    }

    /**
     * Dibuja el overlay negro del fade de entrada.
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
}
