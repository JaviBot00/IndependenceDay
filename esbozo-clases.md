# Esbozo de clases a usar en el proyecto

## Clases de libGDX que usaremos y para qué

### Core / Application

| Clase libGDX | Uso |
|---|---|
| `Game` | Clase principal, gestiona el ciclo de vida y cambio de pantallas |
| `Screen` | Interfaz que implementan todas las pantallas |
| `ScreenAdapter` | Implementación base de `Screen` para no sobreescribir todo |

---

### Rendering

| Clase libGDX | Uso |
|---|---|
| `SpriteBatch` | Renderizar todas las texturas del juego |
| `ShapeRenderer` | Debug de hitboxes y formas simples |
| `OrthographicCamera` | Cámara 2D fija para el juego |
| `Viewport` / `FitViewport` | Adaptar la resolución a cualquier pantalla |
| `ShaderProgram` | Shader CRT scanlines |
| `ParticleEffect` / `ParticleEmitter` | Explosiones y efectos de partículas |

---

### Texturas e imágenes

| Clase libGDX | Uso |
|---|---|
| `Texture` | Texturas individuales |
| `TextureRegion` | Recortar sprites de un spritesheet |
| `TextureAtlas` | Atlas de sprites empaquetados |
| `Pixmap` | Generar escudos destruibles píxel a píxel |
| `Animation` | Animar los aliens (2 frames) y explosiones |

---

### UI y texto

| Clase libGDX | Uso |
|---|---|
| `Stage` | Contenedor de Actors para UI (HUD, menús) |
| `Actor` | Aquí viene tu pregunta — lo explico abajo |
| `Label` | Texto de puntuación, vidas, etc. |
| `Image` (Actor) | Elementos visuales del HUD |
| `Table` | Layout del HUD y menús |
| `FreeTypeFontGenerator` | Generar fuente TTF pixel-art en runtime |
| `BitmapFont` | Fuente resultante para renderizar texto |
| `Skin` | Estilos visuales para widgets UI |

---

### Input

| Clase libGDX | Uso |
|---|---|
| `InputProcessor` | Interfaz para capturar input |
| `InputMultiplexer` | Combinar input de Stage (UI) + juego |
| `Gdx.input` | Teclado en desktop, touch en Android |

---

### Audio

| Clase libGDX | Uso |
|---|---|
| `Sound` | Efectos cortos (disparo, explosión, etc.) |
| `Music` | Música de fondo en bucle |

---

### Física y colisiones

| Clase libGDX | Uso |
|---|---|
| `Rectangle` | Hitboxes simples (más que suficiente para este juego) |
| `Intersector` | Detectar colisiones entre rectángulos |

---

### Utilidades

| Clase libGDX | Uso |
|---|---|
| `AssetManager` | Carga asíncrona de todos los assets |
| `Array<T>` | Lista optimizada de libGDX (sin garbage) |
| `Pool<T>` | Object pooling para balas (evitar GC) |
| `MathUtils` | Aleatorios, clamp, interpolaciones |
| `Interpolation` | Tweening para animaciones suaves (transiciones, etc.) |
| `TimeUtils` | Timers para aparición del OVNI, cadencia de disparo |
| `Preferences` | Guardar high score localmente |
