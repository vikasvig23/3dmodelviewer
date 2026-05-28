# 3D Model Viewer — Jetpack Compose + SceneView

A lightweight Android 3D Model Viewer built using Jetpack Compose and SceneView, which internally uses the Google Filament rendering engine. The application allows users to load, display, move, resize, rotate, zoom, and manage multiple `.glb` 3D models simultaneously on a single screen.

## 3D Library Used

This project uses:

* SceneView for Android 3D scene management
* Google Filament as the rendering engine

### Why SceneView?

SceneView was chosen because it provides a modern and Compose-friendly abstraction over Filament while still offering good rendering performance and easier integration into Android applications. Compared to building directly on top of OpenGL or Filament APIs, SceneView significantly reduces boilerplate and simplifies model loading, camera handling, and gesture interaction.

Filament was selected because it is optimized for real-time physically based rendering (PBR) and performs efficiently on mobile GPUs.

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

## Tech Stack

* Kotlin
* Jetpack Compose
* SceneView
* Google Filament Renderer
* MVVM Architecture

## Performance Optimizations

The following optimizations were applied to improve rendering performance and responsiveness:

* A single shared `SceneView` is used with multiple `ModelNode`s to reduce GPU and memory overhead
* Lightweight `.glb` assets are used for faster loading and lower memory usage
* Models are loaded asynchronously to avoid blocking the UI thread
* Compose state handling was kept stable to minimize unnecessary recompositions
* Gesture handling uses efficient Compose pointer input APIs
* GPU resources are cleaned up when models are removed

These optimizations help maintain smooth interaction even on lower-end Android devices.

## Trade-offs Made

Some trade-offs were made to keep the implementation simple and performant:

* The app limits rendering to 5 simultaneous models to avoid excessive GPU usage
* Advanced rendering effects such as shadows, reflections, and post-processing were not enabled to maintain stable frame rates
* A shared scene architecture improves performance but reduces isolation between models
* Gesture interactions were simplified to prioritize responsiveness over advanced editing precision

## What I Would Improve With More Time

Given more time, the following improvements could be added:

* Better lighting and environment controls
* Model caching system for faster reloads
* Support for animations inside `.glb` models
* Improved gesture precision and inertia
* Better memory management for large assets
* Full-screen immersive interaction mode
* Unit and UI testing coverage
* FPS monitoring and profiling tools

## Known Bugs / Limitations

* Very large `.glb` files may cause frame drops on low-end devices
* Some complex models may load slowly depending on polygon count and textures
* Multi-touch gestures can occasionally conflict during rapid interactions
* No persistence currently exists for saving model positions between app launches
* Rendering quality may vary depending on device GPU capabilities

## Project Structure

* `MainActivity` → Hosts SceneView and overlay UI
* `ModelContainer` → Handles gestures and model interaction
* `ModelViewModel` → Maintains model state
* `ModelItem` → Stores per-model properties

## Assets

Sample `.glb` models are stored in:

```text
app/src/main/assets/models/
```

## Minimum Requirements

* Android 8.0 (API 26)+
* OpenGL ES 3.0 supported device
