package com.hotguy.independenceday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hotguy.independenceday.SpaceInvadersGame;
import com.hotguy.independenceday.entities.BulletPool;
import com.hotguy.independenceday.entities.EnemyGrid;
import com.hotguy.independenceday.entities.Player;
import com.hotguy.independenceday.entities.Shield;
import com.hotguy.independenceday.entities.Ufo;
import com.hotguy.independenceday.managers.CollisionManager;
import com.hotguy.independenceday.managers.ScoreManager;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Pantalla principal de juego.
 *
 * <p>Coordina todas las entidades, managers y sistemas del juego:
 * jugador, grid de enemigos, escudos, OVNI, balas, colisiones,
 * puntuación, HUD y fondo parallax. Implementa los listeners de
 * {@link Player}, {@link EnemyGrid} y {@link Ufo} para reaccionar
 * a los eventos del juego sin acoplamiento directo.</p>
 *
 * <p>El flujo de cada frame es:</p>
 * <pre>
 *   1. Limpiar pantalla
 *   2. Actualizar entidades
 *   3. Detectar colisiones
 *   4. Comprobar condiciones de victoria/derrota
 *   5. Renderizar fondo
 *   6. Renderizar entidades
 *   7. Renderizar HUD
 * </pre>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class GameScreen extends ScreenAdapter
    implements Player.PlayerListener,
    EnemyGrid.EnemyGridListener,
    Ufo.UfoListener {

    // ─────────────────────────────────────────────
    // REFERENCIAS
    // ─────────────────────────────────────────────

    /** Referencia al juego principal para cambiar de pantalla y usar el batch. */
    private final SpaceInvadersGame game;

    // ─────────────────────────────────────────────
    // CÁMARA Y VIEWPORT
    // ─────────────────────────────────────────────

    /** Cámara ortográfica 2D fija para el mundo de juego. */
    private OrthographicCamera camera;

    /** Viewport que adapta el mundo virtual a cualquier resolución. */
    private FitViewport viewport;

    // ─────────────────────────────────────────────
    // ENTIDADES
    // ─────────────────────────────────────────────

    /** Nave del jugador. */
    private Player player;

    /** Batallón de enemigos. */
    private EnemyGrid enemyGrid;

    /** Pool de proyectiles activos. */
    private BulletPool bulletPool;

    /** Lista de escudos. */
    private Array<Shield> shields;

    /** OVNI. */
    private Ufo ufo;

    // ─────────────────────────────────────────────
    // MANAGERS
    // ─────────────────────────────────────────────

    /** Gestor de colisiones. */
    private CollisionManager collisionManager;

    /** Gestor de puntuación y nivel. */
    private final ScoreManager scoreManager;

    // ─────────────────────────────────────────────
    // HUD
    // ─────────────────────────────────────────────

    /** Stage del HUD con puntuación, vidas y nivel. */
    private Stage hudStage;

    /** Label de puntuación actual. */
    private Label scoreLabel;

    /** Label del high score. */
    private Label highScoreLabel;

    /** Label del nivel actual. */
    private Label levelLabel;

    /** Label de vidas restantes. */
    private Label livesLabel;

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
    // OVNI — TEMPORIZADOR
    // ─────────────────────────────────────────────

    /** Temporizador acumulado hasta la próxima aparición del OVNI. */
    private float ufoTimer;

    /** Intervalo actual hasta la próxima aparición del OVNI. */
    private float ufoInterval;

    // ─────────────────────────────────────────────
    // ESTADO DE JUEGO
    // ─────────────────────────────────────────────

    /** Estado actual del juego. */
    private GameState state;

    /** Temporizador de pausa entre estados (muerte, nivel completado, etc.). */
    private float stateTimer;

    /** Duración de la pausa entre la muerte y la respawn (segundos). */
    private static final float DEATH_PAUSE    = 1.5f;

    /** Duración de la pausa entre nivel completado y el siguiente (segundos). */
    private static final float NEXT_LEVEL_PAUSE = 2.0f;

    /**
     * Estados posibles de la pantalla de juego.
     */
    private enum GameState {
        /** Juego en curso. */
        PLAYING,
        /** Jugador acaba de morir, pausa antes de respawn. */
        PLAYER_DEAD,
        /** Todos los enemigos eliminados, pausa antes del siguiente nivel. */
        LEVEL_COMPLETE,
        /** Partida terminada, transición a GameOverScreen. */
        GAME_OVER
    }

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea la pantalla de juego con el gestor de puntuación indicado.
     *
     * <p>Al reiniciar la partida se reutiliza el mismo {@link ScoreManager}
     * reseteado; al avanzar de nivel se mantiene con la puntuación acumulada.</p>
     *
     * @param game         Instancia principal del juego.
     * @param scoreManager Gestor de puntuación de la partida actual.
     */
    public GameScreen(SpaceInvadersGame game, ScoreManager scoreManager) {
        this.game         = game;
        this.scoreManager = scoreManager;
    }

    // ─────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────

    /**
     * Inicializa la cámara, el HUD y todas las entidades del juego.
     */
    @Override
    public void show() {
        setupCamera();
        setupHud();
        initEntities();
        initStars();
        resetUfoTimer();

        state      = GameState.PLAYING;
        stateTimer = 0f;

        if (Assets.music != null && !Assets.music.isPlaying()) {
            Assets.music.play();
        }
    }

    /**
     * Actualiza y renderiza el juego cada frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);
        draw();
    }

    /**
     * Adapta la cámara y el HUD al nuevo tamaño de pantalla.
     *
     * @param width  Nuevo ancho en píxeles.
     * @param height Nuevo alto en píxeles.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudStage.getViewport().update(width, height, true);
    }

    /**
     * Pausa la música al minimizar la aplicación.
     */
    @Override
    public void pause() {
        if (Assets.music != null) Assets.music.pause();
    }

    /**
     * Reanuda la música al volver a la aplicación.
     */
    @Override
    public void resume() {
        if (Assets.music != null) Assets.music.play();
    }

    /**
     * Libera todos los recursos al salir de la pantalla.
     */
    @Override
    public void hide() {
        dispose();
    }

    /**
     * Libera los recursos propios de la pantalla.
     */
    @Override
    public void dispose() {
        if (hudStage != null) { hudStage.dispose(); hudStage = null; }
        if (bulletPool != null) bulletPool.dispose();
        if (shields != null) {
            for (Shield s : shields) s.dispose();
        }
    }

    // ─────────────────────────────────────────────
    // INICIALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Configura la cámara ortográfica y el viewport.
     */
    private void setupCamera() {
        camera   = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        camera.position.set(Constants.WORLD_WIDTH * 0.5f, Constants.WORLD_HEIGHT * 0.5f, 0);
        camera.update();
    }

    /**
     * Crea el Stage del HUD con Labels para puntuación, vidas y nivel.
     */
    private void setupHud() {
        hudStage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));

        Label.LabelStyle style = new Label.LabelStyle(Assets.fontHud, null);

        scoreLabel     = new Label("SCORE: 0",     style);
        highScoreLabel = new Label("HI: " + scoreManager.getHighScore(), style);
        levelLabel     = new Label("NIVEL: 1",     style);
        livesLabel     = new Label("VIDAS: " + Constants.PLAYER_LIVES, style);

        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().pad(10f);
        hud.add(scoreLabel).expandX().left();
        hud.add(highScoreLabel).expandX().center();
        hud.add(levelLabel).expandX().center();
        hud.add(livesLabel).expandX().right();

        hudStage.addActor(hud);
    }

    /**
     * Instancia y configura todas las entidades del nivel actual.
     */
    private void initEntities() {
        // Jugador
        player = new Player();
        player.setListener(this);

        // Pool de balas
        bulletPool = new BulletPool();

        // Calcular posición inicial del grid centrado horizontalmente
        float gridWidth = Constants.ENEMY_COLS * Constants.ENEMY_WIDTH
            + (Constants.ENEMY_COLS - 1) * Constants.ENEMY_H_SPACING;
        float gridStartX = (Constants.WORLD_WIDTH - gridWidth) * 0.5f;
        float gridStartY = Constants.WORLD_HEIGHT * 0.55f;

        // Grid de enemigos
        enemyGrid = new EnemyGrid(gridStartX, gridStartY);
        enemyGrid.setListener(this);

        // Escudos distribuidos uniformemente
        shields = new Array<>(Constants.SHIELD_COUNT);
        float shieldSpacing = Constants.WORLD_WIDTH / (Constants.SHIELD_COUNT + 1f);
        for (int i = 0; i < Constants.SHIELD_COUNT; i++) {
            float sx = shieldSpacing * (i + 1) - Constants.SHIELD_WIDTH * 0.5f;
            shields.add(new Shield(sx, Constants.SHIELD_Y));
        }

        // OVNI
        ufo = new Ufo();
        ufo.setListener(this);

        // Gestor de colisiones
        collisionManager = new CollisionManager(
            bulletPool, player, enemyGrid, shields, ufo);
    }

    /**
     * Genera las posiciones aleatorias iniciales de las estrellas parallax.
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

    // ─────────────────────────────────────────────
    // ACTUALIZACIÓN
    // ─────────────────────────────────────────────

    /**
     * Coordina la actualización de todos los sistemas según el estado actual.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void update(float delta) {
        updateStars(delta);

        switch (state) {
            case PLAYING:
                updatePlaying(delta);
                break;
            case PLAYER_DEAD:
            case LEVEL_COMPLETE:
                updateStateTimer(delta);
                break;
            case GAME_OVER:
                // La transición se gestiona desde onPlayerDead()
                break;
        }

        // Pausa manual
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    /**
     * Actualiza todas las entidades y sistemas durante el juego activo.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updatePlaying(float delta) {
        player.update(delta);
        enemyGrid.update(delta);
        bulletPool.update(delta);
        updateUfo(delta);
        collisionManager.update();
        checkWinCondition();
        updateHudLabels();
    }

    /**
     * Gestiona el temporizador de pausa entre estados.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateStateTimer(float delta) {
        stateTimer -= delta;
        if (stateTimer <= 0f) {
            if (state == GameState.PLAYER_DEAD) {
                respawnPlayer();
            } else if (state == GameState.LEVEL_COMPLETE) {
                loadNextLevel();
            }
        }
    }

    /**
     * Gestiona el temporizador de aparición del OVNI.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    private void updateUfo(float delta) {
        if (ufo.isActive()) {
            ufo.update(delta);
        } else {
            ufoTimer -= delta;
            if (ufoTimer <= 0f) {
                ufo.spawn();
                resetUfoTimer();
            }
        }
    }

    /**
     * Desplaza las estrellas parallax hacia abajo cada frame.
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
     * Actualiza los Labels del HUD con los valores actuales.
     */
    private void updateHudLabels() {
        scoreLabel.setText("SCORE: " + scoreManager.getScore());
        highScoreLabel.setText("HI: " + scoreManager.getHighScore());
        levelLabel.setText("NIVEL: " + scoreManager.getLevel());
        livesLabel.setText("VIDAS: " + player.getLives());
    }

    /**
     * Comprueba si todos los enemigos han sido eliminados para avanzar de nivel.
     */
    private void checkWinCondition() {
        if (enemyGrid.isCleared() && state == GameState.PLAYING) {
            state      = GameState.LEVEL_COMPLETE;
            stateTimer = NEXT_LEVEL_PAUSE;
        }
    }

    // ─────────────────────────────────────────────
    // NIVEL
    // ─────────────────────────────────────────────

    /**
     * Avanza al siguiente nivel incrementando la dificultad.
     */
    private void loadNextLevel() {
        scoreManager.nextLevel();
        bulletPool.clear();

        // Reposicionar escudos (se regeneran desde cero)
        for (Shield s : shields) s.dispose();
        shields.clear();
        float shieldSpacing = Constants.WORLD_WIDTH / (Constants.SHIELD_COUNT + 1f);
        for (int i = 0; i < Constants.SHIELD_COUNT; i++) {
            float sx = shieldSpacing * (i + 1) - Constants.SHIELD_WIDTH * 0.5f;
            shields.add(new Shield(sx, Constants.SHIELD_Y));
        }

        // Nuevo grid más rápido
        float gridWidth  = Constants.ENEMY_COLS * Constants.ENEMY_WIDTH
            + (Constants.ENEMY_COLS - 1) * Constants.ENEMY_H_SPACING;
        float gridStartX = (Constants.WORLD_WIDTH - gridWidth) * 0.5f;
        float gridStartY = Constants.WORLD_HEIGHT * 0.55f;
        enemyGrid = new EnemyGrid(gridStartX, gridStartY);
        enemyGrid.setListener(this);

        // Actualizar CollisionManager con las nuevas referencias
        collisionManager = new CollisionManager(
            bulletPool, player, enemyGrid, shields, ufo);

        state = GameState.PLAYING;
        updateHudLabels();
    }

    /**
     * Hace reaparecer al jugador tras una muerte si le quedan vidas.
     */
    private void respawnPlayer() {
        player.setActive(true);
        state = GameState.PLAYING;
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Coordina el renderizado de todos los elementos del frame.
     */
    private void draw() {
        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawStars(batch);
        drawEntities(batch);
        batch.end();

        hudStage.act();
        hudStage.draw();
    }

    /**
     * Dibuja las dos capas de estrellas parallax.
     *
     * @param batch Batch activo.
     */
    private void drawStars(SpriteBatch batch) {
        // Capa lenta: tenue
        batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        for (int i = 0; i < Constants.STARS_LAYER_SLOW; i++) {
            batch.draw(Assets.atlas.findRegion("pixel"),
                starsSlowX[i], starsSlowY[i], 1f, 1f);
        }
        // Capa rápida: brillante
        batch.setColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < Constants.STARS_LAYER_FAST; i++) {
            batch.draw(Assets.atlas.findRegion("pixel"),
                starsFastX[i], starsFastY[i], 2f, 2f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Dibuja todas las entidades del juego en el orden correcto.
     *
     * @param batch Batch activo.
     */
    private void drawEntities(SpriteBatch batch) {
        // Escudos
        for (Shield shield : shields) shield.render(batch);

        // Enemigos
        enemyGrid.render(batch);

        // OVNI
        if (ufo.isActive()) ufo.render(batch);

        // Balas
        bulletPool.render(batch);

        // Jugador
        player.render(batch);

        // Línea divisoria inferior (estética retro)
        batch.setColor(0.2f, 1f, 0.2f, 1f);
        batch.draw(Assets.atlas.findRegion("pixel"),
            0, Constants.PLAYER_Y - 4f,
            Constants.WORLD_WIDTH, 2f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ─────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────

    /**
     * Reinicia el temporizador del OVNI con un intervalo aleatorio.
     */
    private void resetUfoTimer() {
        ufoInterval = MathUtils.random(Constants.UFO_SPAWN_MIN, Constants.UFO_SPAWN_MAX);
        ufoTimer    = ufoInterval;
    }

    // ─────────────────────────────────────────────
    // PLAYER LISTENER
    // ─────────────────────────────────────────────

    /**
     * Dispara una bala del jugador desde el pool.
     *
     * @param x Posición X del centro del cañón.
     * @param y Posición Y del borde superior de la nave.
     */
    @Override
    public void onPlayerShoot(float x, float y) {
        bulletPool.fire(x, y, true);
    }

    /**
     * Actualiza el HUD al perder una vida.
     *
     * @param livesRemaining Vidas restantes tras la pérdida.
     */
    @Override
    public void onPlayerLostLife(int livesRemaining) {
        state      = GameState.PLAYER_DEAD;
        stateTimer = DEATH_PAUSE;
        updateHudLabels();
    }

    /**
     * Transiciona a la pantalla de game over al agotar las vidas.
     */
    @Override
    public void onPlayerDead() {
        state = GameState.GAME_OVER;
        scoreManager.saveHighScore();
        game.setScreen(new GameOverScreen(game, scoreManager));
    }

    // ─────────────────────────────────────────────
    // ENEMY GRID LISTENER
    // ─────────────────────────────────────────────

    /**
     * Suma los puntos de los enemigos muertos a la puntuación.
     *
     * @param count     Número de enemigos muertos.
     * @param scoreEach Puntos de cada enemigo.
     */
    @Override
    public void onEnemiesDied(int count, int scoreEach) {
        scoreManager.addScore(count * scoreEach);
        updateHudLabels();
    }

    /**
     * Genera una bala enemiga desde el pool.
     *
     * @param x Posición X del centro inferior del enemigo.
     * @param y Posición Y del borde inferior del enemigo.
     */
    @Override
    public void onEnemyShoot(float x, float y) {
        bulletPool.fire(x, y, false);
    }

    /**
     * Fuerza el game over si el grid alcanza la línea del jugador.
     */
    @Override
    public void onGridReachedBottom() {
        onPlayerDead();
    }

    // ─────────────────────────────────────────────
    // UFO LISTENER
    // ─────────────────────────────────────────────

    /**
     * Suma los puntos del OVNI destruido a la puntuación.
     *
     * @param score Puntos obtenidos por destruir el OVNI.
     */
    @Override
    public void onUfoDestroyed(int score) {
        scoreManager.addScore(score);
        updateHudLabels();
    }
}
