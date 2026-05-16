package com.hotguy.independenceday.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hotguy.independenceday.interfaces.Collidable;
import com.hotguy.independenceday.utils.Constants;

/**
 * Representa un escudo destruible píxel a píxel.
 *
 * <p>A diferencia del resto de entidades, el escudo no usa una textura
 * estática sino un {@link Pixmap} mutable que se va erosionando cada vez
 * que una bala lo impacta. Esto permite una destrucción local y precisa
 * en el punto exacto del impacto, replicando el comportamiento del
 * Space Invaders original con un toque visual moderno.</p>
 *
 * <p>La forma inicial del escudo es un arco clásico con una cavidad
 * inferior, generada completamente por código mediante {@link Pixmap}.</p>
 *
 * @author Javier Botella
 * @version 1.0
 */
public class Shield extends Entity {

    // ─────────────────────────────────────────────
    // PIXMAP Y TEXTURA
    // ─────────────────────────────────────────────

    /** Pixmap mutable que representa el estado actual del escudo. */
    private Pixmap pixmap;

    /** Textura generada desde el pixmap, actualizada tras cada impacto. */
    private Texture texture;

    // ─────────────────────────────────────────────
    // DESTRUCCIÓN
    // ─────────────────────────────────────────────

    /** Radio en píxeles del área destruida por cada impacto. */
    private static final int DESTROY_RADIUS = 6;

    /** Color verde del escudo. */
    private static final Color SHIELD_COLOR = new Color(0.2f, 1f, 0.2f, 1f);

    /** Número de píxeles sólidos restantes en el escudo. */
    private int solidPixels;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    /**
     * Crea un escudo en la posición indicada y genera su forma inicial.
     *
     * @param x Posición X del borde izquierdo del escudo.
     * @param y Posición Y del borde inferior del escudo.
     */
    public Shield(float x, float y) {
        super(x, y, Constants.SHIELD_WIDTH, Constants.SHIELD_HEIGHT);
        generatePixmap();
    }

    // ─────────────────────────────────────────────
    // GENERACIÓN PROCEDURAL
    // ─────────────────────────────────────────────

    /**
     * Genera la forma inicial del escudo mediante {@link Pixmap}.
     *
     * <p>El escudo tiene forma de arco: sólido en la parte superior
     * y con una cavidad semicircular en la parte inferior central,
     * replicando el diseño clásico del bunker de Space Invaders.</p>
     */
    private void generatePixmap() {
        int w = (int) Constants.SHIELD_WIDTH;
        int h = (int) Constants.SHIELD_HEIGHT;

        pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        solidPixels = 0;

        int cavityRadius  = w / 4;
        int cavityCenterX = w / 2;
        int cavityCenterY = 0; // base inferior

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                // Coordenadas relativas al centro de la cavidad inferior
                int dx = px - cavityCenterX;
                int dy = py - cavityCenterY;

                boolean inCavity = (dx * dx + dy * dy) < (cavityRadius * cavityRadius)
                    && py < h / 2;

                // Esquinas redondeadas en la parte superior
                boolean inCorner = isInRoundedCorner(px, py, w, h);

                if (!inCavity && !inCorner) {
                    int alpha = 255;
                    // Degradado sutil de brillo de arriba a abajo
                    float brightness = 0.6f + 0.4f * ((float) py / h);
                    pixmap.setColor(
                        SHIELD_COLOR.r * brightness,
                        SHIELD_COLOR.g * brightness,
                        SHIELD_COLOR.b * brightness,
                        alpha / 255f
                    );
                    pixmap.drawPixel(px, h - 1 - py); // invertir Y (Pixmap es top-down)
                    solidPixels++;
                } else {
                    pixmap.setColor(0, 0, 0, 0);
                    pixmap.drawPixel(px, h - 1 - py);
                }
            }
        }

        rebuildTexture();
    }

    /**
     * Determina si un píxel está en una esquina redondeada del escudo.
     *
     * @param px Posición X del píxel.
     * @param py Posición Y del píxel.
     * @param w  Ancho total del escudo.
     * @param h  Alto total del escudo.
     * @return {@code true} si el píxel pertenece a una esquina recortada.
     */
    private boolean isInRoundedCorner(int px, int py, int w, int h) {
        int r = 6; // radio de esquina
        // Esquina superior izquierda
        if (px < r && py > h - r) {
            int dx = px - r;
            int dy = py - (h - r);
            if (dx * dx + dy * dy > r * r) return true;
        }
        // Esquina superior derecha
        if (px > w - r && py > h - r) {
            int dx = px - (w - r);
            int dy = py - (h - r);
            if (dx * dx + dy * dy > r * r) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // UPDATABLE
    // ─────────────────────────────────────────────

    /**
     * El escudo no tiene lógica de movimiento; solo se actualiza
     * su estado activo en función de los píxeles restantes.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float delta) {
        if (solidPixels <= 0) active = false;
    }

    // ─────────────────────────────────────────────
    // RENDERABLE
    // ─────────────────────────────────────────────

    /**
     * Renderiza el escudo con su estado actual de destrucción.
     *
     * @param batch Batch activo con el que se realiza el renderizado.
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!active || texture == null) return;
        batch.draw(texture, x, y, width, height);
    }

    // ─────────────────────────────────────────────
    // COLLIDABLE
    // ─────────────────────────────────────────────

    /**
     * Destruye un área circular del escudo en el punto de impacto
     * cuando una bala colisiona con él.
     *
     * @param other El objeto con el que se ha producido la colisión.
     */
    @Override
    public void onCollision(Collidable other) {
        if (other instanceof Bullet) {
            Bullet bullet = (Bullet) other;
            // Calcular punto de impacto en coordenadas locales del pixmap
            int impactX = (int) (bullet.getX() + bullet.getWidth()  * 0.5f - x);
            int impactY = (int) (bullet.getY() - y);
            destroyArea(impactX, impactY);
        }
    }

    /**
     * Elimina todos los píxeles dentro del radio de destrucción
     * centrado en el punto de impacto.
     *
     * @param cx Centro X del impacto en coordenadas locales del pixmap.
     * @param cy Centro Y del impacto en coordenadas locales del pixmap.
     */
    private void destroyArea(int cx, int cy) {
        int w = (int) Constants.SHIELD_WIDTH;
        int h = (int) Constants.SHIELD_HEIGHT;

        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);

        for (int py = cy - DESTROY_RADIUS; py <= cy + DESTROY_RADIUS; py++) {
            for (int px = cx - DESTROY_RADIUS; px <= cx + DESTROY_RADIUS; px++) {
                if (px < 0 || px >= w || py < 0 || py >= h) continue;

                int dx = px - cx;
                int dy = py - cy;
                if (dx * dx + dy * dy > DESTROY_RADIUS * DESTROY_RADIUS) continue;

                // Comprobar si había píxel sólido antes de borrar
                int pixmapY = h - 1 - py;
                int pixel   = pixmap.getPixel(px, pixmapY);
                if ((pixel & 0xFF) > 0) solidPixels--;

                pixmap.drawPixel(px, pixmapY);
            }
        }

        rebuildTexture();
    }

    /**
     * Reconstruye la textura GPU a partir del pixmap actualizado.
     *
     * <p>Se llama tras cada impacto para reflejar la destrucción
     * en el siguiente frame renderizado.</p>
     */
    private void rebuildTexture() {
        if (texture != null) texture.dispose();
        texture = new Texture(pixmap);
    }

    // ─────────────────────────────────────────────
    // DISPOSE
    // ─────────────────────────────────────────────

    /**
     * Libera el pixmap y la textura del escudo de la memoria.
     */
    @Override
    public void dispose() {
        if (pixmap != null) pixmap.dispose();
        if (texture != null) texture.dispose();
    }
}
