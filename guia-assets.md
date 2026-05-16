# Guía de Assets para Independence Day

## Assets que PROPORCIONAS TÚ

Estos necesitan calidad artística y es mejor que los hagas/descargues tú:

| Asset | Formato | Notas |
|---|---|---|
| `player.png` | PNG transparente | La nave del jugador |
| `enemy_a.png` | PNG transparente | Alien fila inferior (2 frames de animación) |
| `enemy_b.png` | PNG transparente | Alien fila media (2 frames) |
| `enemy_c.png` | PNG transparente | Alien fila superior (2 frames) |
| `ufo.png` | PNG transparente | El OVNI |
| `explosion.png` | PNG transparente | Spritesheet de explosión |
| `font.ttf` | TTF | Fuente pixel-art retro (hay gratuitas en Google Fonts como *Press Start 2P*) |
| `music.mp3` | MP3/OGG | Música de fondo ambient espacial |
| `sfx_shoot.wav` | WAV/OGG | Sonido disparo jugador |
| `sfx_enemy_shoot.wav` | WAV/OGG | Sonido disparo enemigo |
| `sfx_explosion.wav` | WAV/OGG | Explosión enemigo |
| `sfx_ufo.wav` | WAV/OGG | Sonido OVNI cruzando |
| `sfx_player_death.wav` | WAV/OGG | Muerte del jugador |

---

## Assets que GENERAMOS POR CÓDIGO

Estos los creo yo programáticamente con `Pixmap` porque su naturaleza lo permite o incluso lo hace mejor:

| Asset | Cómo | Por qué |
|---|---|---|
| **Escudos/bunkers** | `Pixmap` píxel a píxel | Necesitamos destrucción píxel a píxel, imposible con PNG estático |
| **Balas** | `Pixmap` rectángulo simple | Son primitivas geométricas, no merece la pena un PNG |
| **Fondo de estrellas** | Código puro (`ShapeRenderer` o `Pixmap`) | Parallax dinámico con varias capas de velocidad |
| **Flash de impacto** | `ShaderProgram` GLSL | Efecto de blanco al recibir daño, se hace con shader |
| **Shader CRT** | `ShaderProgram` GLSL | Las scanlines se generan matemáticamente |
| **Partículas de explosión** | `ParticleEffect` + archivo `.p` | Lo configuramos con el editor de partículas de libGDX |

---

## Resumen visual

```cmd
Tú proporcionas          Nosotros generamos
─────────────────        ──────────────────
Sprites de entidades     Escudos (Pixmap)
Fuente TTF               Balas (Pixmap)
Música y sonidos         Estrellas (código)
                         Shaders (GLSL)
                         Partículas (código)
```
