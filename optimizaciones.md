# Optimizaciones adicionales que se me ocurren

## 1. Pool generalizado — no solo para balas

Si el pool funciona para balas, puede funcionar para más cosas:

- **Pool de explosiones** — las partículas de explosión se reutilizan igual que las balas. Cada vez que muere un enemigo no se crea un `ParticleEffect` nuevo, se recicla uno del pool.
- **Pool de textos flotantes** — el número de puntuación que flota al matar un enemigo (+10, +20...) también es un objeto temporal reutilizable.

---

## 2. Spatial hashing para colisiones

Ahora mismo el `CollisionManager` haría algo así:

```cmd
por cada bala → comprobar contra cada enemigo vivo = O(n × m)
```

Con **spatial hashing** dividimos la pantalla en celdas y solo comprobamos colisiones entre objetos en la misma celda. Para Space Invaders con pocos objetos no es crítico, pero con muchas balas enemigas simultáneas sí se nota. Implementación sencilla con un `HashMap<Integer, Array<Collidable>>`.

---

## 3. Dirty flag en EnemyGrid

El bloque de enemigos se mueve en bloque. En vez de recalcular la posición de cada `Enemy` cada frame, usamos un **dirty flag**:

```cmd
EnemyGrid mueve su posición base (un solo float x, y)
    ↓
Solo cuando dirty = true, cada Enemy recalcula su posición absoluta
    ↓
dirty se activa solo cuando el grid se mueve de verdad
```

Así 55 enemigos no recalculan su posición 60 veces por segundo, solo cuando el grid se desplaza.

---

## 4. Cache del enemigo más bajo por columna

El juego original solo permite disparar al enemigo más bajo de cada columna. En vez de buscar ese enemigo cada vez que toca disparar:

```cmd
EnemyGrid mantiene un array bottomEnemies[11]
    ↓
Solo se actualiza cuando muere un Enemy de esa columna
    ↓
El disparo enemigo es O(1) en vez de O(n)
```

---

## 5. EnemyRow con estado agregado

En vez de que `EnemyGrid` itere sobre todos los `Enemy` para saber si una fila está vacía:

```cmd
EnemyRow mantiene un contador aliveCount
    ↓
Cada vez que muere un Enemy, decrementa aliveCount
    ↓
EnemyGrid consulta aliveCount de cada EnemyRow = O(filas) en vez de O(filas × columnas)
```

---

## 6. Separar lógica de renderizado en EnemyGrid

`EnemyGrid` solo gestiona lógica. El renderizado lo delega completamente a cada `EnemyRow`, y cada `EnemyRow` al `Enemy`. Así si en el futuro quisieras añadir un shader o efecto especial por fila, solo tocas `EnemyRow.render()` sin tocar la lógica del grid.

---

## 7. Animación compartida por fila

Todos los enemigos de una misma fila comparten el mismo frame de animación. En vez de que cada `Enemy` tenga su propio `Animation` y su propio `stateTime`:

```cmd
EnemyRow tiene un único stateTime compartido
    ↓
Todos los Enemy de esa fila consultan ese stateTime para renderizar
    ↓
55 objetos Animation → 5 objetos Animation (uno por fila)
```

---

## Resumen visual del modelo final

```cmd
EnemyGrid
│  · posición base (x, y)
│  · dirty flag
│  · velocidad actual
│  · bottomEnemies[11] (cache)
│
├── EnemyRow  ×5
│    · EnemyType (enum)
│    · stateTime compartido
│    · aliveCount
│    · Array<Enemy>
│
└── Enemy
     · offset relativo al grid (no posición absoluta)
     · isAlive
     · onDeath()
```
