# EggMonsters 🥚👾

Tamagotchi moderno para Android con incubación real (clics + pasos).

## Cómo abrir

1. Android Studio → **File → Open** → carpeta `EggMonsters`
2. Sincroniza Gradle
3. Ejecuta en móvil API 26+ (recomendado para pasos reales)

### Permisos
- Reconocimiento de actividad (pasos)
- Notificaciones (avisos de cuidados)

---

## Contenido completo

### 1. 6 Huevos / Criaturas
Pyrodrake 🔥 · Aqualing 💧 · Florafox 🌿 · Bytebit ⚡ · Umbra 🌑 · Lumina ✨

### 2. Incubación
1000 clics + 1000 pasos reales simultáneos

### 3. Cuidados
Hambre, Felicidad, Energía, Limpieza, Afecto  
Comer · Jugar · Limpiar · Dormir · Acariciar

### 4. Minijuegos (aleatorio al pulsar Jugar)
- **Reacción**: toca el objetivo lo más rápido posible (10 rondas)
- **Memoria**: encuentra las 6 parejas de elementos

### 5. Misiones diarias
Alimentar, jugar, acariciar, llegar a 90 de felicidad…  
Recompensas de XP y afecto. Se reinician cada día.

### 6. Cosméticos / Skins
Cada elemento tiene 3 formas (Bebé / Adolescente / Adulto).  
Se desbloquean al evolucionar. Puedes cambiar la apariencia en el selector de skins.

### 7. Muerte y reanimación
Si hambre y felicidad llegan a 0 → la criatura muere.  
Hasta **2 reanimaciones** con el Huevo de Segunda Oportunidad.  
Después solo queda empezar de nuevo.

### 8. Persistencia
DataStore: guarda todo (stats, misiones, skins, vida/muerte).  
Al reabrir aplica el decay del tiempo offline.

### 9. Notificaciones + sonidos + vibración

---

## Estructura

```
data/          EggType, Creature, GameState, GameRepository, DailyMission
viewmodel/     GameViewModel
util/          NotificationHelper, SoundHelper
ui/screens/    EggSelection, Incubation, Pet, MiniGame, Memory,
               Death, Missions, SkinPicker
```

minSdk 26 · targetSdk 35 · Kotlin + Compose + Material 3
