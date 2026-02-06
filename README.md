# 📱 TastyPlaces - Gestión de Lugares Favoritos

Aplicación Android nativa desarrollada en **Kotlin** para gestionar lugares gastronómicos favoritos (restaurantes, cafeterías, bares y panaderías) con integración de Google Maps y geolocalización GPS.

> **Aplicación educativa** desarrollada como proyecto de la asignatura Programación Multimedia y Dispositivos Móviles

---

## 📋 Tabla de Contenidos

- [Características](#-características-principales)
- [Tecnologías](#️-tecnologías)
- [Requisitos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#️-configuración-de-google-maps-api-key)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Base de Datos](#️-base-de-datos)
- [Uso](#-uso-de-la-aplicación)
- [Permisos](#-permisos-necesarios)
- [Autora](#-autora)

---

## 🎯 Características Principales

- 🗺️ **Visualización en Google Maps** con marcadores interactivos personalizados por categoría
- ⭐ **Sistema de favoritos** para filtrar y destacar lugares especiales
- 📍 **Geolocalización GPS** para obtener coordenadas automáticamente
- 📤 **Compartir lugares** mediante cualquier app instalada (WhatsApp, Email, etc.)
- 🗺️ **Ver en mapa** directamente desde cada card individual
- 💾 **Exportación de datos** a formato JSON en almacenamiento interno
- 🌍 **Multiidioma** con soporte completo para Inglés, Español y Euskera
- 🌓 **Modo claro y oscuro** con adaptación automática
- 📱 **Diseño responsivo** optimizado para orientación vertical, horizontal y tablets
- ✏️ **CRUD completo** con SQLite (crear, leer, actualizar, eliminar)
- ⚡ **Validación de formularios** con mensajes de error contextuales
- 🎨 **Material Design 3** con componentes modernos y animaciones fluidas

---

## 🛠️ Tecnologías

| Categoría | Tecnología | Versión |
|-----------|------------|---------|
| **Lenguaje** | Kotlin | 100% |
| **Base de datos** | SQLite | Nativa |
| **Mapas** | Google Maps SDK for Android | Latest |
| **Geolocalización** | FusedLocationProviderClient | Google Play Services |
| **UI** | Material Design 3, ConstraintLayout, RecyclerView | Latest |
| **Exportación** | JSON (Kotlinx Serialization) | - |
| **SDK mínimo** | API 24 (Android 7.0 Nougat) | - |
| **SDK objetivo** | API 34 (Android 14) | - |
| **Build Tools** | Gradle (KTS), Android Studio | Latest |

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Android Studio** (Hedgehog o superior recomendado)
- **JDK 17** o superior
- **Android SDK** con API Level 24-34
- **Google Play Services** instalados en el emulador/dispositivo
- **Cuenta de Google Cloud** (para obtener API Key de Maps)

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/TastyPlacesApp.git
cd TastyPlacesApp
```

### 2. Abrir en Android Studio

1. Abre Android Studio
2. Selecciona **File → Open**
3. Navega hasta la carpeta del proyecto
4. Espera a que Gradle sincronice las dependencias

### 3. Configurar API Key (Ver sección siguiente)

### 4. Ejecutar la Aplicación

1. Conecta un dispositivo Android o inicia un emulador
2. Asegúrate de que el dispositivo tenga Google Play Services
3. Presiona **Run** (▶️) o usa `Shift + F10`

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

**⚠️ Importante:**
- El archivo `local.properties` ya está en `.gitignore` por defecto, por lo que tu API Key no se subirá al repositorio.
- **NO** añadas la API Key directamente en el `AndroidManifest.xml`
- Gradle inyecta automáticamente la clave desde `local.properties` durante el proceso de build

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

- **Inglés** (por defecto) - `values/strings.xml`
- **Español** - `values-es/strings.xml`
- **Euskera** - `values-eu/strings.xml`

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

### Pantalla Principal (MainActivity)

| Acción | Cómo hacerlo |
|--------|--------------|
| **Añadir lugar** | Pulsa el botón flotante **+** (FAB) en la esquina inferior derecha |
| **Ver en mapa** | Pulsa el icono 🗺️ en cualquier card de lugar |
| **Marcar favorito** | Pulsa el corazón ❤️ en el card (cambia de color al activarse) |
| **Editar lugar** | Pulsa sobre el card completo para abrir el formulario de edición |
| **Eliminar lugar** | Mantén presionado el card → Confirmar en el diálogo |
| **Compartir lugar** | Pulsa el icono 📤 para compartir mediante apps instaladas |
| **Filtrar favoritos** | Menú (⋮) → Mostrar solo favoritos |
| **Exportar datos** | Menú (⋮) → Exportar lugares a JSON |
| **Cambiar idioma** | Menú (⋮) → Seleccionar idioma (ES/EN/EU) |

### Formulario (FormActivity)

| Acción | Cómo hacerlo |
|--------|--------------|
| **Usar ubicación GPS** | Pulsa el botón "Usar mi ubicación actual" |
| **Seleccionar categoría** | Usa el Spinner para elegir entre Restaurant, Café, Bar o Bakery |
| **Asignar rating** | Desliza la barra de estrellas (0-5 estrellas) |
| **Guardar lugar** | Pulsa el botón "Guardar" (valida campos automáticamente) |
| **Cancelar** | Usa el botón Atrás o el botón "Cancelar" |

### Mapa (MapActivity)

| Acción | Cómo hacerlo |
|--------|--------------|
| **Ver todos los lugares** | Se muestran automáticamente al abrir el mapa |
| **Crear lugar en mapa** | Mantén presionado (long click) en una ubicación |
| **Distinguir categorías** | Cada categoría tiene un color de marcador diferente |
| **Ver detalles** | Pulsa un marcador para ver el título del lugar |

---

## 🔐 Permisos Necesarios

La aplicación requiere los siguientes permisos:

```xml
<!-- Requerido para mapas y exportación -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Requerido para geolocalización GPS -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Requerido solo en Android 7-9 para exportar archivos -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

**Nota:** Los permisos de ubicación se solicitan en tiempo de ejecución (Runtime Permissions) siguiendo las mejores prácticas de Android.

---

## 🎨 Capturas de Pantalla

> **Nota:** Añade capturas de pantalla en una carpeta `/screenshots` y enlázalas aquí para mostrar visualmente la aplicación.

---

## 🚧 Mejoras Futuras

- [ ] Importación de datos desde JSON
- [ ] Integración con Google Places API para autocompletado
- [ ] Sistema de búsqueda y filtrado avanzado
- [ ] Fotos de lugares con cámara o galería
- [ ] Sincronización en la nube (Firebase)
- [ ] Rutas y navegación GPS hacia lugares
- [ ] Modo sin conexión con caché de mapas
- [ ] Compartir colecciones de lugares
- [ ] Widget de inicio con lugares favoritos

---

## 🐛 Solución de Problemas

### El mapa no se muestra

- ✅ Verifica que la API Key esté correctamente configurada en `local.properties`
- ✅ Asegúrate de haber habilitado **Maps SDK for Android** en Google Cloud Console
- ✅ Revisa que el dispositivo/emulador tenga Google Play Services instalado
- ✅ Limpia y reconstruye el proyecto: **Build → Clean Project → Rebuild Project**

### Error de permisos de ubicación

- ✅ Acepta los permisos de ubicación cuando la app los solicite
- ✅ Verifica en Ajustes del dispositivo que los permisos estén activados
- ✅ En emulador: **Extended Controls (⋯) → Location** para simular GPS

### Gradle sync failed

- ✅ Actualiza Android Studio a la última versión
- ✅ Verifica tu conexión a Internet
- ✅ Ejecuta: **File → Invalidate Caches → Invalidate and Restart**

---

## 👨‍💻 Autora

**Wara Pacheco**

- **Asignatura:** Programación Multimedia y Dispositivos Móviles
- **Curso:** 2025-2026
- **Fecha:** Febrero 2026

---

<div align="center">

Hecho con ❤️ para Amaia

</div>
