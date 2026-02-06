# 📱 TastyPlaces - Gestión de Lugares Favoritos

Aplicación Android nativa desarrollada en Kotlin para gestionar lugares gastronómicos favoritos (restaurantes, cafeterías, bares y panaderías) con integración de mapas y geolocalización.

---

## 🎯 Características Principales

- 🗺️ **Visualización en Google Maps** con marcadores interactivos por categoría
- ⭐ **Sistema de favoritos** para filtrar lugares especiales
- 📍 **Geolocalización GPS** para obtener coordenadas automáticamente
- 📤 **Compartir lugares** mediante cualquier app
- 🗺️ **Ver en mapa** desde cada card individual
- 💾 **Exportación de datos** a formato JSON
- 🌍 **Multiidioma** (Inglés, Español, Euskera)
- 🌓 **Modo claro y oscuro** adaptativo
- 📱 **Diseño responsivo** para vertical, horizontal y tablets
- ✏️ **CRUD completo** con SQLite (crear, leer, actualizar, eliminar)

---

## 🛠️ Tecnologías

- **Lenguaje:** Kotlin 100%
- **Base de datos:** SQLite (persistencia local)
- **Mapas:** Google Maps SDK for Android
- **Geolocalización:** FusedLocationProviderClient
- **UI:** Material Design 3, ConstraintLayout, RecyclerView
- **Exportación:** JSON (almacenamiento interno)
- **SDK mínimo:** API 24 (Android 7.0)
- **SDK objetivo:** API 34 (Android 14)

---

## ⚙️ Configuración de Google Maps API Key

Para que la aplicación funcione correctamente, debes configurar tu propia API Key de Google Maps:

### 1. Obtener la API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto nuevo o selecciona uno existente
3. Habilita **"Maps SDK for Android"**
4. Ve a **Credenciales** → **Crear credenciales** → **Clave de API**
5. Copia la API Key generada

### 2. Añadir la API Key en `local.properties`

Edita el archivo **`local.properties`** en la raíz del proyecto:

```properties
sdk.dir=/ruta/a/tu/Android/SDK
MAPS_API_KEY=TU_API_KEY_AQUÍ
```

**Ejemplo:**
```properties
sdk.dir=/Users/usuario/Library/Android/sdk
MAPS_API_KEY=AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**⚠️ Importante:** El archivo `local.properties` ya está en `.gitignore` por defecto, por lo que tu API Key no se subirá al repositorio.

---

## 📂 Estructura del Proyecto

```
app/src/main/java/es/didaktikapp/repasoapp/
├── activities/              # Pantallas
│   ├── MainActivity.kt      # Lista de lugares
│   ├── FormActivity.kt      # Crear/editar lugares
│   ├── MapActivity.kt       # Visualización de mapa
│   └── ActivityWithMenus.kt # Clase base
├── adapters/                # RecyclerView
│   ├── LugarAdapter.kt
│   └── LugarViewHolder.kt
├── database/                # Persistencia
│   └── LugaresSQLiteHelper.kt # CRUD SQLite + migraciones
├── models/                  # Modelos de datos
│   ├── Lugar.kt             # Data class principal
│   └── Categoria.kt         # Enum de categorías
└── utils/                   # Utilidades
    ├── ExportUtils.kt       # Exportación JSON
    └── LocaleHelper.kt      # Gestión de idioma
```

---

## 🗄️ Base de Datos

### Tabla `lugares`

| Columna | Tipo | Descripción |
|---------|------|-------------|
| `id` | INTEGER PRIMARY KEY | Identificador único |
| `nombre` | TEXT NOT NULL | Nombre del lugar |
| `descripcion` | TEXT | Descripción detallada |
| `latitud` | REAL NOT NULL | Coordenada latitud |
| `longitud` | REAL NOT NULL | Coordenada longitud |
| `categoria` | TEXT NOT NULL | Código categoría (RESTAURANT, CAFE, BAR, BAKERY) |
| `fecha_creacion` | INTEGER NOT NULL | Timestamp en milisegundos |
| `rating` | REAL DEFAULT 0.0 | Valoración 0.0 - 5.0 |
| `es_favorito` | INTEGER DEFAULT 0 | Estado favorito (0=no, 1=sí) |
| `tipo_cocina` | TEXT | Especialidad del lugar |

---

## 🌍 Internacionalización

Soporte completo para 3 idiomas:

- 🇬🇧 **Inglés** (por defecto) - `values/strings.xml`
- 🇪🇸 **Español** - `values-es/strings.xml`
- 🏴󐁥󐁳󐁰󐁶󐁿 **Euskera** - `values-eu/strings.xml`

**Total:** 348 strings traducidos (116 por idioma)

---

## 📱 Funcionalidades Destacadas

### Gestión de Lugares
- ✅ Crear, editar y eliminar lugares (CRUD completo)
- ✅ Validación de coordenadas (acepta formato decimal con punto o coma)
- ✅ Categorización con colores e iconos personalizados
- ✅ Rating con estrellas (0-5)

### Mapas y Geolocalización
- ✅ Marcadores en Google Maps con colores por categoría
- ✅ Ver ubicación específica desde cada card
- ✅ Long click en mapa para crear lugares con coordenadas
- ✅ Obtener ubicación GPS actual
- ✅ Ubicación por defecto en Vitoria-Gasteiz

### Interfaz y UX
- ✅ Diseño Material Design 3
- ✅ Adaptación automática a modo claro/oscuro
- ✅ Layouts optimizados para vertical y horizontal
- ✅ Soporte para tablets (sw600dp, sw720dp)
- ✅ Sistema de dimensiones adaptativas

### Datos
- ✅ Exportación a JSON (almacenamiento interno)
- ✅ Migración segura de base de datos entre versiones
- ✅ Persistencia de favoritos y preferencias

---

## 📝 Uso de la Aplicación

1. **Añadir lugar:** Pulsa el FAB (+) en la pantalla principal
2. **Usar GPS:** En el formulario, pulsa "Usar mi ubicación"
3. **Ver en mapa:** Pulsa el icono 🗺️ en cualquier card
4. **Marcar favorito:** Pulsa el corazón ❤️ en el card
5. **Eliminar:** Mantén presionado el card → Confirmar
6. **Compartir:** Pulsa el icono 📤 para compartir
7. **Exportar datos:** Menú (⋮) → Exportar

---

## 👨‍💻 Autora

**Wara Pacheco**

- **Asignatura:** Programación Multimedia y Dispositivos Móviles
- **Fecha:** Febrero 2026
