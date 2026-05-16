package com.hotguy.independenceday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.hotguy.independenceday.SpaceInvadersGame;
import com.hotguy.independenceday.utils.Assets;
import com.hotguy.independenceday.utils.Constants;

/**
 * Pantalla de carga mostrada al iniciar la aplicación.
 *
 * <p>Avanza la carga asíncrona del {@link Assets} cada frame y muestra
 * una barra de progreso minimalista con estética retro. Cuando la carga
 * finaliza, navega automáticamente a {@link MenuScreen}.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class LoadingScreen extends ScreenAdapter {

    // ─────────────────────────────────────────────
    // REFERENCIAS
    // ─────────────────────────────────────────────

    /** Referencia al juego principal para cambiar de pantalla. */
    private final SpaceInvadersGame game;

    // ─────────────────────────────────────────────
    // RENDERING
    // ─────────────────────────────────────────────

    /** Renderizador de formas para dibujar la barra de progreso. */
    private ShapeRenderer shapeRenderer;

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────

    /** Progreso visual suavizado mediante interpolación. */
    private float smoothProgress;

    // ─────────────────────────────────────────────
    // LAYOUT
    // ─────────────────────────────────────────────

    /** Ancho de la barra de progreso en píxeles. */
    private static final float BAR_WIDTH  = 400f;

    /** Alto de la barra de progreso en píxeles. */
    private static final float BAR_HEIGHT = 18f;

    /** Margen exterior del borde de la barra. */
    private static final float BAR_BORDER = 3f;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea la pantalla de carga.
     *
     * @param game Instancia principal del juego.
     */
    public LoadingScreen(SpaceInvadersGame game) {
        this.game = game;
    }

    // ─────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────

    /**
     * Inicializa el {@link ShapeRenderer} al mostrar la pantalla.
     */
    @Override
    public void show() {
        shapeRenderer  = new ShapeRenderer();
        smoothProgress = 0f;
    }

    /**
     * Actualiza la carga y renderiza la barra de progreso cada frame.
     *
     * <p>El progreso visual se suaviza con una interpolación lineal
     * para evitar saltos bruscos en la barra.</p>
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void render(float delta) {
        // Limpiar pantalla con negro
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Suavizar progreso visualmente
        float targetProgress = Assets.getProgress();
        smoothProgress += (targetProgress - smoothProgress) * 0.1f;

        drawProgressBar();

        // Avanzar carga asíncrona
        if (Assets.update()) {
            Assets.finishLoading();
            game.setScreen(new MenuScreen(game));
        }
    }

    /**
     * Libera el {@link ShapeRenderer} al salir de la pantalla.
     */
    @Override
    public void hide() {
        dispose();
    }

    /**
     * Libera los recursos propios de esta pantalla.
     */
    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
    }

    // ─────────────────────────────────────────────
    // RENDERIZADO
    // ─────────────────────────────────────────────

    /**
     * Dibuja la barra de progreso centrada en la pantalla.
     *
     * <p>Compuesta por tres capas: fondo oscuro, relleno verde
     * proporcional al progreso y borde blanco exterior.</p>
     */
    private void drawProgressBar() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float barX = (screenW - BAR_WIDTH)  * 0.5f;
        float barY = (screenH - BAR_HEIGHT) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo de la barra
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);

        // Relleno proporcional al progreso
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, BAR_WIDTH * smoothProgress, BAR_HEIGHT);

        shapeRenderer.end();

        // Borde exterior
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
            barX    - BAR_BORDER,
            barY    - BAR_BORDER,
            BAR_WIDTH  + BAR_BORDER * 2,
            BAR_HEIGHT + BAR_BORDER * 2
        );
        shapeRenderer.end();
    }
}
