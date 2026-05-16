package com.hotguy.independenceday.utils;

/**
 * Constantes globales del juego Space Invaders.
 *
 * <p>Centraliza todos los valores numéricos, cadenas y configuraciones
 * para facilitar el ajuste del juego sin tocar la lógica.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public final class Constants {

    /** Constructor privado — clase de utilidad, no instanciable. */
    private Constants() {}

    // ─────────────────────────────────────────────
    // PANTALLA
    // ─────────────────────────────────────────────

    /** Ancho virtual del mundo de juego en píxeles. */
    public static final float WORLD_WIDTH  = 800f;

    /** Alto virtual del mundo de juego en píxeles. */
    public static final float WORLD_HEIGHT = 600f;

    /** Título de la ventana en desktop. */
    public static final String WINDOW_TITLE = "Space Invaders";

    // ─────────────────────────────────────────────
    // JUGADOR
    // ─────────────────────────────────────────────

    /** Ancho del sprite del jugador en píxeles. */
    public static final float PLAYER_WIDTH  = 48f;

    /** Alto del sprite del jugador en píxeles. */
    public static final float PLAYER_HEIGHT = 32f;

    /** Velocidad de movimiento horizontal del jugador (píxeles/segundo). */
    public static final float PLAYER_SPEED  = 250f;

    /** Posición Y fija del jugador desde el borde inferior. */
    public static final float PLAYER_Y      = 40f;

    /** Número de vidas iniciales del jugador. */
    public static final int   PLAYER_LIVES  = 3;

    /** Tiempo mínimo entre disparos del jugador (segundos). */
    public static final float PLAYER_SHOOT_COOLDOWN = 0.5f;

    // ─────────────────────────────────────────────
    // ENEMIGOS
    // ─────────────────────────────────────────────

    /** Número de columnas de la cuadrícula de enemigos. */
    public static final int   ENEMY_COLS         = 11;

    /** Número de filas de la cuadrícula de enemigos. */
    public static final int   ENEMY_ROWS         = 5;

    /** Ancho de cada enemigo en píxeles. */
    public static final float ENEMY_WIDTH        = 36f;

    /** Alto de cada enemigo en píxeles. */
    public static final float ENEMY_HEIGHT       = 28f;

    /** Separación horizontal entre enemigos. */
    public static final float ENEMY_H_SPACING    = 16f;

    /** Separación vertical entre filas de enemigos. */
    public static final float ENEMY_V_SPACING    = 16f;

    /** Velocidad horizontal inicial del bloque de enemigos (píxeles/segundo). */
    public static final float ENEMY_SPEED_INIT   = 40f;

    /** Incremento de velocidad cada vez que muere un enemigo. */
    public static final float ENEMY_SPEED_STEP   = 2f;

    /** Píxeles que desciende el bloque al tocar un borde lateral. */
    public static final float ENEMY_DROP         = 20f;

    /** Duración de cada frame de animación de los enemigos (segundos). */
    public static final float ENEMY_ANIM_FRAME   = 0.5f;

    /** Intervalo mínimo entre disparos enemigos (segundos). */
    public static final float ENEMY_SHOOT_MIN    = 0.8f;

    /** Intervalo máximo entre disparos enemigos (segundos). */
    public static final float ENEMY_SHOOT_MAX    = 2.5f;

    // ─────────────────────────────────────────────
    // PUNTUACIÓN POR TIPO DE ENEMIGO
    // ─────────────────────────────────────────────

    /** Puntos por matar un enemigo de la fila inferior (tipo A). */
    public static final int SCORE_ENEMY_A = 10;

    /** Puntos por matar un enemigo de las filas medias (tipo B). */
    public static final int SCORE_ENEMY_B = 20;

    /** Puntos por matar un enemigo de las filas superiores (tipo C). */
    public static final int SCORE_ENEMY_C = 30;

    /** Puntos base por destruir el OVNI (valor aleatorio múltiplo de este). */
    public static final int SCORE_UFO_BASE = 50;

    // ─────────────────────────────────────────────
    // OVNI (UFO)
    // ─────────────────────────────────────────────

    /** Ancho del sprite del OVNI. */
    public static final float UFO_WIDTH   = 52f;

    /** Alto del sprite del OVNI. */
    public static final float UFO_HEIGHT  = 24f;

    /** Velocidad horizontal del OVNI (píxeles/segundo). */
    public static final float UFO_SPEED   = 150f;

    /** Posición Y del OVNI desde el borde superior. */
    public static final float UFO_Y       = 540f;

    /** Intervalo mínimo entre apariciones del OVNI (segundos). */
    public static final float UFO_SPAWN_MIN = 15f;

    /** Intervalo máximo entre apariciones del OVNI (segundos). */
    public static final float UFO_SPAWN_MAX = 30f;

    // ─────────────────────────────────────────────
    // BALAS
    // ─────────────────────────────────────────────

    /** Ancho de las balas en píxeles. */
    public static final float BULLET_WIDTH  = 4f;

    /** Alto de las balas en píxeles. */
    public static final float BULLET_HEIGHT = 14f;

    /** Velocidad de la bala del jugador (píxeles/segundo). */
    public static final float BULLET_PLAYER_SPEED = 420f;

    /** Velocidad de las balas enemigas (píxeles/segundo). */
    public static final float BULLET_ENEMY_SPEED  = 220f;

    // ─────────────────────────────────────────────
    // ESCUDOS
    // ─────────────────────────────────────────────

    /** Número de escudos en la pantalla. */
    public static final int   SHIELD_COUNT  = 4;

    /** Ancho de cada escudo en píxeles. */
    public static final float SHIELD_WIDTH  = 64f;

    /** Alto de cada escudo en píxeles. */
    public static final float SHIELD_HEIGHT = 48f;

    /** Posición Y de los escudos desde el borde inferior. */
    public static final float SHIELD_Y      = 100f;

    // ─────────────────────────────────────────────
    // PARALLAX / FONDO
    // ─────────────────────────────────────────────

    /** Número de estrellas en la capa lenta del parallax. */
    public static final int   STARS_LAYER_SLOW  = 60;

    /** Número de estrellas en la capa rápida del parallax. */
    public static final int   STARS_LAYER_FAST  = 30;

    /** Velocidad de la capa lenta de estrellas (píxeles/segundo). */
    public static final float STARS_SPEED_SLOW  = 20f;

    /** Velocidad de la capa rápida de estrellas (píxeles/segundo). */
    public static final float STARS_SPEED_FAST  = 50f;

    // ─────────────────────────────────────────────
    // UI / HUD
    // ─────────────────────────────────────────────

    /** Tamaño de la fuente principal del HUD. */
    public static final int   FONT_SIZE_HUD     = 16;

    /** Tamaño de la fuente del menú principal. */
    public static final int   FONT_SIZE_MENU    = 24;

    /** Tamaño de la fuente del título. */
    public static final int   FONT_SIZE_TITLE   = 40;

    /** Duración del fade in/out entre pantallas (segundos). */
    public static final float TRANSITION_TIME   = 0.4f;

    // ─────────────────────────────────────────────
    // RUTAS DE ASSETS
    // ─────────────────────────────────────────────

    /** Ruta de la fuente TTF pixel-art. */
    public static final String FONT_PATH             = "fonts/PressStart2P.ttf";

    /** Ruta del atlas de sprites. */
    public static final String ATLAS_PATH            = "sprites/sprites.atlas";

    /** Ruta de la música de fondo. */
    public static final String MUSIC_PATH            = "audio/music.ogg";

    public static final String SFX_SHOOT_PATH        = "audio/sfx_shoot.wav";
    public static final String SFX_ENEMY_SHOOT_PATH  = "audio/sfx_enemy_shoot.wav";
    public static final String SFX_EXPLOSION_PATH    = "audio/sfx_explosion.wav";
    public static final String SFX_UFO_PATH          = "audio/sfx_ufo.wav";
    public static final String SFX_PLAYER_DEATH_PATH = "audio/sfx_player_death.wav";

    // ─────────────────────────────────────────────
    // PREFERENCIAS
    // ─────────────────────────────────────────────

    /** Nombre del archivo de preferencias para guardar el high score. */
    public static final String PREFS_NAME      = "spaceinvaders_prefs";

    /** Clave del high score en las preferencias. */
    public static final String PREFS_HIGHSCORE = "highscore";
}
