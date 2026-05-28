# 3D Model Viewer — Jetpack Compose + SceneView

A Android 3D Model Viewer built using Jetpack Compose and SceneView (Filament Renderer). The application allows users to load, display, move, resize, rotate, zoom, and manage multiple `.glb` 3D models simultaneously on a single screen.

## Features

* Single Activity Architecture
* Built completely using Jetpack Compose
* Load multiple `.glb` 3D models dynamically
* Display up to 5 models simultaneously
* Draggable model containers
* Resizable model containers using pinch gestures
* Separate interaction modes:

  * Normal Mode → Move and resize container
  * Interaction Mode → Rotate and zoom 3D model
* Close/remove models dynamically
* Smooth gesture handling
* Optimized for low-end Android devices
* Uses SceneView + Google Filament for efficient 3D rendering

## Tech Stack

* Kotlin
* Jetpack Compose
* SceneView
* Google Filament Renderer
* MVVM Architecture

## Performance Optimizations

* Uses a single `SceneView` and multiple `ModelNode`s to reduce GPU memory usage
* Lightweight `.glb` assets for faster loading
* Async model loading to avoid UI blocking
* Gesture handling optimized with Compose pointer input
* Stable Compose state management to minimize recompositions
* GPU resource cleanup on model removal

## Project Structure

* `MainActivity` → Hosts single SceneView and overlay UI
* `ModelContainer` → Handles gestures and model interaction
* `ModelViewModel` → Maintains model state
* `ModelItem` → Stores per-model properties

## Supported Interactions

### Normal Mode

* Drag model container
* Resize model container

### Interaction Mode

* Rotate 3D model
* Zoom 3D model

## Assets

Sample `.glb` models are stored in:

```text
app/src/main/assets/models/
```

## Libraries Used

* SceneView
* Filament
* AndroidX Compose
* Material 3

## Minimum Requirements

* Android 8.0 (API 26)+
* OpenGL ES 3.0 supported device

## Author

Vikas Vig
Android Developer
