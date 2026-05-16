package com.hotguy.independenceday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hotguy.independenceday.screens.LoadingScreen;
import com.hotguy.independenceday.utils.Assets;

/**
 * Clase principal de la aplicación Space Invaders.
 *
 * <p>Extiende {@link Game} para aprovechar el sistema de pantallas de libGDX.
 * Es el punto de entrada de la aplicación y se encarga de inicializar los
 * recursos compartidos entre pantallas, como el {@link SpriteBatch}.</p>
 *
 * <p>El flujo de pantallas es el siguiente:</p>
 * <pre>
 *   LoadingScreen → MenuScreen → GameScreen → GameOverScreen
 *                                    ↑__________________|
 * </pre>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class SpaceInvadersGame extends Game {

    /**
     * Batch compartido entre todas las pantallas para minimizar
     * el número de draw calls y evitar crear/destruir el objeto
     * en cada cambio de pantalla.
     */
    public SpriteBatch batch;

    // ─────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────

    /**
     * Inicializa los recursos compartidos y navega a la pantalla de carga.
     *
     * <p>Se ejecuta una única vez al arrancar la aplicación.</p>
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        Assets.load();
        setScreen(new LoadingScreen(this));
    }

    /**
     * Delega el renderizado a la pantalla activa.
     *
     * <p>libGDX llama a este método cada frame. La clase {@link Game}
     * se encarga de llamar a {@code screen.render(delta)} internamente.</p>
     */
    @Override
    public void render() {
        super.render();
    }

    /**
     * Libera todos los recursos compartidos al cerrar la aplicación.
     *
     * <p>Llama primero a {@code super.dispose()} para que la pantalla
     * activa también libere sus recursos.</p>
     */
    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        Assets.dispose();
        Gdx.app.log("SpaceInvadersGame", "Recursos liberados correctamente.");
    }
}
