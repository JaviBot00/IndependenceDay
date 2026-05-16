package com.hotguy.independenceday.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

/**
 * Gestor centralizado de assets del juego.
 *
 * <p>Encapsula el {@link AssetManager} de libGDX y expone todos los recursos
 * del juego (texturas, fuentes, sonidos, música) como campos públicos estáticos
 * una vez completada la carga. También genera por código los assets procedurales
 * como las balas y los escudos.</p>
 *
 * <p>Uso típico:</p>
 * <pre>
 *   Assets.load();
 *   // en el hilo de render:
 *   if (Assets.update()) {
 *       Assets.finishLoading();
 *   }
 * </pre>
 *
 * @author Javier Botella
 * @version 1.0
 */
public final class Assets {

    // ─────────────────────────────────────────────
    // ASSET MANAGER
    // ─────────────────────────────────────────────

    /** Instancia interna del gestor de assets de libGDX. */
    private static final AssetManager manager = new AssetManager();

    // ─────────────────────────────────────────────
    // ATLAS DE SPRITES
    // ─────────────────────────────────────────────

    /** Atlas que contiene todos los sprites del juego. */
    public static TextureAtlas atlas;

    // ─────────────────────────────────────────────
    // FUENTES
    // ─────────────────────────────────────────────

    /** Fuente pequeña para el HUD (puntuación, vidas, nivel). */
    public static BitmapFont fontHud;

    /** Fuente mediana para los menús. */
    public static BitmapFont fontMenu;

    /** Fuente grande para el título. */
    public static BitmapFont fontTitle;

    // ─────────────────────────────────────────────
    // TEXTURAS PROCEDURALES
    // ─────────────────────────────────────────────

    /** Textura de la bala del jugador, generada por código. */
    public static Texture bulletPlayerTexture;

    /** Textura de la bala enemiga, generada por código. */
    public static Texture bulletEnemyTexture;

    // ─────────────────────────────────────────────
    // AUDIO
    // ─────────────────────────────────────────────

    /** Música de fondo en bucle. */
    public static Music music;

    /** Sonido del disparo del jugador. */
    public static Sound sfxShoot;

    /** Sonido del disparo enemigo. */
    public static Sound sfxEnemyShoot;

    /** Sonido de explosión de enemigo. */
    public static Sound sfxExplosion;

    /** Sonido del OVNI al cruzar la pantalla. */
    public static Sound sfxUfo;

    /** Sonido de muerte del jugador. */
    public static Sound sfxPlayerDeath;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /** Constructor privado — clase de utilidad, no instanciable. */
    private Assets() {}

    // ─────────────────────────────────────────────
    // CARGA
    // ─────────────────────────────────────────────

    /**
     * Registra todos los assets en el {@link AssetManager} para su carga asíncrona.
     *
     * <p>Debe llamarse una única vez al inicio de la aplicación, antes de
     * entrar en el bucle de actualización.</p>
     */
    public static void load() {
        // Registrar loaders de FreeType para fuentes TTF
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class,
            new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf",
            new FreetypeFontLoader(resolver));

        // Atlas de sprites
        manager.load(Constants.ATLAS_PATH, TextureAtlas.class);

        // Fuente HUD
        FreetypeFontLoader.FreeTypeFontLoaderParameter hudParam =
            new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        hudParam.fontFileName = Constants.FONT_PATH;
        hudParam.fontParameters.size = Constants.FONT_SIZE_HUD;
        hudParam.fontParameters.color = com.badlogic.gdx.graphics.Color.WHITE;
        manager.load("fontHud.ttf", BitmapFont.class, hudParam);

        // Fuente menú
        FreetypeFontLoader.FreeTypeFontLoaderParameter menuParam =
            new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        menuParam.fontFileName = Constants.FONT_PATH;
        menuParam.fontParameters.size = Constants.FONT_SIZE_MENU;
        menuParam.fontParameters.color = Color.WHITE;
        manager.load("fontMenu.ttf", BitmapFont.class, menuParam);

        // Fuente título
        FreetypeFontLoader.FreeTypeFontLoaderParameter titleParam =
            new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        titleParam.fontFileName = Constants.FONT_PATH;
        titleParam.fontParameters.size = Constants.FONT_SIZE_TITLE;
        titleParam.fontParameters.color = Color.GREEN;
        manager.load("fontTitle.ttf", BitmapFont.class, titleParam);

        // Audio
        manager.load(Constants.MUSIC_PATH,            Music.class);
        manager.load(Constants.SFX_SHOOT_PATH,        Sound.class);
        manager.load(Constants.SFX_ENEMY_SHOOT_PATH,  Sound.class);
        manager.load(Constants.SFX_EXPLOSION_PATH,    Sound.class);
        manager.load(Constants.SFX_UFO_PATH,          Sound.class);
        manager.load(Constants.SFX_PLAYER_DEATH_PATH, Sound.class);
    }

    /**
     * Avanza la carga asíncrona de assets.
     *
     * <p>Debe llamarse cada frame desde la pantalla de carga.
     * Devuelve {@code true} cuando todos los assets han sido cargados.</p>
     *
     * @return {@code true} si la carga ha finalizado completamente.
     */
    public static boolean update() {
        return manager.update();
    }

    /**
     * Devuelve el progreso de carga como valor entre 0.0 y 1.0.
     *
     * @return Fracción de assets cargados.
     */
    public static float getProgress() {
        return manager.getProgress();
    }

    /**
     * Asigna los assets cargados a los campos estáticos públicos y genera
     * los assets procedurales.
     *
     * <p>Debe llamarse una única vez cuando {@link #update()} devuelve {@code true}.</p>
     */
    public static void finishLoading() {
        // Atlas
        atlas = manager.get(Constants.ATLAS_PATH, TextureAtlas.class);

        // Fuentes
        fontHud   = manager.get("fontHud.ttf",   BitmapFont.class);
        fontMenu  = manager.get("fontMenu.ttf",  BitmapFont.class);
        fontTitle = manager.get("fontTitle.ttf", BitmapFont.class);

        // Audio
        music          = manager.get(Constants.MUSIC_PATH,            Music.class);
        sfxShoot       = manager.get(Constants.SFX_SHOOT_PATH,        Sound.class);
        sfxEnemyShoot  = manager.get(Constants.SFX_ENEMY_SHOOT_PATH,  Sound.class);
        sfxExplosion   = manager.get(Constants.SFX_EXPLOSION_PATH,    Sound.class);
        sfxUfo         = manager.get(Constants.SFX_UFO_PATH,          Sound.class);
        sfxPlayerDeath = manager.get(Constants.SFX_PLAYER_DEATH_PATH, Sound.class);

        // Assets procedurales
        generateProceduralAssets();

        // Configurar música
        music.setLooping(true);
        music.setVolume(0.5f);
    }

    // ─────────────────────────────────────────────
    // ASSETS PROCEDURALES
    // ─────────────────────────────────────────────

    /**
     * Genera por código las texturas que no requieren archivo externo.
     *
     * <p>Crea las balas del jugador y de los enemigos usando {@link Pixmap},
     * con un degradado para darles un aspecto más moderno.</p>
     */
    private static void generateProceduralAssets() {
        bulletPlayerTexture = generateBulletTexture(
            new Color(0.4f, 1f, 0.4f, 1f),   // verde claro
            new Color(1f, 1f, 1f, 1f)          // blanco en el centro
        );

        bulletEnemyTexture = generateBulletTexture(
            new Color(1f, 0.3f, 0.3f, 1f),    // rojo
            new Color(1f, 0.8f, 0.2f, 1f)     // naranja en el centro
        );
    }

    /**
     * Genera una textura de bala con degradado vertical.
     *
     * @param outer Color del borde exterior de la bala.
     * @param inner Color del centro de la bala.
     * @return Textura lista para usar con {@link com.badlogic.gdx.graphics.g2d.SpriteBatch}.
     */
    private static Texture generateBulletTexture(Color outer, Color inner) {
        int w = (int) Constants.BULLET_WIDTH;
        int h = (int) Constants.BULLET_HEIGHT;

        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        for (int y = 0; y < h; y++) {
            // Interpolación lineal del color a lo largo del eje Y
            float t = (float) y / (h - 1);
            float r = outer.r + (inner.r - outer.r) * t;
            float g = outer.g + (inner.g - outer.g) * t;
            float b = outer.b + (inner.b - outer.b) * t;

            for (int x = 0; x < w; x++) {
                // Suavizar los bordes laterales
                float edgeFade = 1f - Math.abs((x - w * 0.5f) / (w * 0.5f));
                pixmap.setColor(r, g, b, edgeFade);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // ─────────────────────────────────────────────
    // DISPOSE
    // ─────────────────────────────────────────────

    /**
     * Libera todos los recursos de memoria.
     *
     * <p>Debe llamarse al cerrar la aplicación desde
     * {@link com.badlogic.gdx.ApplicationListener#dispose()}.</p>
     */
    public static void dispose() {
        manager.dispose();

        if (bulletPlayerTexture != null) bulletPlayerTexture.dispose();
        if (bulletEnemyTexture  != null) bulletEnemyTexture.dispose();
    }
}
