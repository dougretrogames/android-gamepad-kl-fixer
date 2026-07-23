# 🎮 Android Gamepad KL Fixer

[![minSdk](https://img.shields.io/badge/minSdk-21%20(Android%205.0)-brightgreen)](#)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-blue)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Uma aplicação Android em Kotlin para **diagnosticar, gerar e instalar arquivos `.kl` (KeyLayout)** para gamepads e joysticks USB. Requer **acesso root** para instalar arquivos em `/system/usr/keylayout/`.

---

## 📋 Sumário
- [Visão Geral](#visão-geral)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Telas](#telas)
- [Formato de Arquivo KL](#formato-de-arquivo-kl)
- [Operações Root](#operações-root)
- [Requisitos](#requisitos)
- [Instruções de Compilação](#instruções-de-compilação)
- [Executando Testes](#executando-testes)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Licença](#licença)

---

## Visão Geral

O Android mapeia as entradas de gamepads usando arquivos KeyLayout (`.kl`) armazenados em:
```
/system/usr/keylayout/Vendor_XXXX_Product_XXXX.kl
```
Quando um gamepad possui mapeamentos incorretos ou ausentes, os botões podem estar trocados, sem resposta ou reportar códigos de tecla errados. Este app:

1. **Detecta** todos os gamepads conectados via API `InputDevice`, extraindo o Vendor ID e Product ID.
2. **Captura** dados em tempo real de `KeyEvent` e `MotionEvent` do dispositivo físico.
3. **Gera** um arquivo `.kl` sintaticamente correto a partir dos dados capturados.
4. **Instala** o arquivo em `/system/usr/keylayout/` usando root (`su`) — com backup/restauração automáticos.

---

## Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| 🔍 Escaneamento de Dispositivos | Lista todos os dispositivos `SOURCE_GAMEPAD` e `SOURCE_JOYSTICK` conectados |
| 🪪 Diagnóstico de Dispositivo | Mostra Nome, Vendor ID (hex/decimal), Product ID, descritor, máscara de bits de fontes |
| ⌨️ Captura de KeyEvent | Captura `keyCode`, `scanCode`, ação (DOWN/UP), ID do dispositivo em tempo real |
| 🕹️ Captura de MotionEvent | Mostra todos os valores de eixos (X, Y, Z, RZ, HAT, gatilhos, etc.) ao vivo |
| 📄 Geração de KL | Gera `Vendor_XXXX_Product_XXXX.kl` a partir de mapeamentos padrão ou eventos capturados |
| 💾 Salvamento Local | Salva o `.kl` gerado no armazenamento privado do app (`filesDir/keylayout/`) |
| 🔐 Verificação de Root | Verifica a disponibilidade do `su` via comando `id` (uid=0) |
| ⬆️ Instalação no Sistema | Remonta `/system` como rw, copia o arquivo, define permissões `644 root:root`, remonta como ro |
| 🔄 Backup e Restauração | Faz backup do `.kl` existente como `.kl.bak` antes de sobrescrever, restauração em um toque |

---

## Arquitetura

O projeto segue o padrão **MVVM** com uma separação clara de responsabilidades:

```
app/
├── model/           # Classes de dados: GamepadDevice, KeyEventRecord, MotionEventRecord
├── device/          # DeviceScanner   — envolve a API InputDevice
├── kl/              # KlFileGenerator — gera o conteúdo do .kl
│                    # KlFileStorage   — salva/carrega o .kl do armazenamento privado
├── root/            # RootManager     — todas as operações su (check, backup, install, restore)
└── ui/
    ├── MainActivity         → lista de dispositivos (RecyclerView + FAB)
    ├── DeviceDetailActivity → diagnóstico + prévia do KL + botões de ação
    ├── TestInputActivity    → captura de KeyEvent/MotionEvent ao vivo
    ├── adapter/             → adaptadores de RecyclerView (DeviceAdapter, KeyEventAdapter)
    └── viewmodel/           → DeviceListViewModel, DeviceDetailViewModel, TestInputViewModel
```

Todas as operações de I/O (comandos root, escrita de arquivos) rodam no `Dispatchers.IO` via coroutines.

---

## Telas

### 1. Tela Principal — Lista de Dispositivos
- Mostra todos os gamepads conectados com nome, IDs de Fabricante/Produto e nome de arquivo KL esperado.
- FAB e item de menu para reescanear dispositivos.
- Mensagem de estado vazio quando nenhum dispositivo está conectado.

### 2. Detalhes do Dispositivo
- Diagnóstico completo: nome, Vendor ID (0x045E / 1118), Product ID, descritor, fontes.
- Prévia ao vivo do arquivo `.kl` gerado (monoespaçado rolável).
- Botões:
  - **Test Input** → abre tela de captura.
  - **Check Root** → verifica acesso su.
  - **Save KL (local)** → salva no armazenamento do app.
  - **Install to /system** → instalação com root (habilitado apenas após confirmação do root).
  - **Restore Backup** → restaura `.kl.bak` (habilitado apenas após confirmação do root).

### 3. Testar Entrada
- Conecte seu gamepad e pressione botões/mova os analógicos.
- Valores de eixos exibidos ao vivo (X, Y, Z, RZ, HAT_X, HAT_Y, LTRIGGER, RTRIGGER, etc.).
- Log de eventos de tecla mostra o nome do keyCode, scanCode e ação (UP/DOWN) para cada pressão.
- **Generate KL** usa os eventos capturados para produzir um mapeamento mais preciso.

---

## Formato de Arquivo KL

```
# Arquivo KeyLayout gerado pelo GamepadKLFixer
# Dispositivo: Xbox Wireless Controller
# Vendor ID : 045E (1118)
# Product ID: 028E (654)

# ----- Key Mappings -----
key 0x130   BUTTON_A   WAKE
key 0x131   BUTTON_B   WAKE
key 0x133   BUTTON_X   WAKE
key 0x134   BUTTON_Y   WAKE
key 0x136   BUTTON_L1  WAKE
key 0x137   BUTTON_R1  WAKE
key 0x13a   BUTTON_SELECT WAKE
key 0x13b   BUTTON_START  WAKE
key 0x13d   BUTTON_THUMBL WAKE
key 0x13e   BUTTON_THUMBR WAKE

# ----- Axis Mappings -----
axis ABS_X        X
axis ABS_Y        Y
axis ABS_Z        Z
axis ABS_RZ       RZ
axis ABS_HAT0X    HAT_X
axis ABS_HAT0Y    HAT_Y
axis ABS_BRAKE    LTRIGGER
axis ABS_GAS      RTRIGGER
```

Referência: [Android Key Layout Files](https://source.android.com/docs/core/interaction/input/key-layout-files)

---

## Operações Root

> ⚠️ Operações root funcionam apenas em **dispositivos com root** que possuam o binário superusuário (`su`) disponível.

| Operação | Comando executado (como root) |
|---|---|
| Verificar root | `id` (verifica se `uid=0`) |
| Listar arquivos KL | `ls -la /system/usr/keylayout/*.kl` |
| Backup | `cp Vendor_X.kl Vendor_X.kl.bak` |
| Instalar | `mount -o remount,rw /system` → `cp` → `chmod 644` → `chown root:root` → `mount -o remount,ro /system` |
| Restaurar | `mount rw` → `cp .bak` → `mount ro` |
| Remover | `mount rw` → `rm -f` → `mount ro` |

Todos os comandos são enviados para um único processo `su` para minimizar diálogos de permissão.

---

## Requisitos

- Android **5.0+** (minSdk 21)
- Kotlin 1.9.x
- Android Gradle Plugin 8.4.x
- Gradle 8.7
- **Acesso Root necessário** para operações de instalação/restauração no sistema.
- Gamepad / Joystick USB ou Bluetooth para detecção de dispositivo.

---

## Instruções de Compilação

```bash
git clone https://github.com/dougretrogames/android-gamepad-kl-fixer.git
cd android-gamepad-kl-fixer

# Build de Debug
./gradlew assembleDebug

# Instalar no dispositivo conectado
./gradlew installDebug

# Build de Release
./gradlew assembleRelease
```

O APK estará em `app/build/outputs/apk/debug/app-debug.apk`.

---

## Executando Testes

```bash
# Testes unitários (JVM, sem necessidade de dispositivo)
./gradlew test

# Testes instrumentados (requer dispositivo/emulador conectado)
./gradlew connectedAndroidTest
```

Os testes unitários cobrem:
- `KlFileGeneratorTest` — geração de conteúdo, validação, formatação hex.
- `GamepadDeviceTest` — correção do modelo de dados, formato do nome de arquivo KL.
- `KeyEventRecordTest` — mapeamento de nomes de ações.
- `RootManagerTest` — constantes e classe de dados de resultado.

---

## Estrutura do Projeto

```
android-gamepad-kl-fixer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dougretrogames/gamepadklfixer/
│   │   │   │   ├── model/          GamepadDevice, KeyEventRecord, MotionEventRecord
│   │   │   │   ├── device/         DeviceScanner
│   │   │   │   ├── kl/             KlFileGenerator, KlFileStorage
│   │   │   │   ├── root/           RootManager
│   │   │   │   └── ui/             Activities, Adapters, ViewModels
│   │   │   ├── res/
│   │   │   │   ├── layout/         Layouts XML para todas as telas e itens de lista
│   │   │   │   ├── menu/           menu_main.xml
│   │   │   │   └── values/         strings, cores, temas
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   Testes unitários JVM
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml          Catálogo de versões
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Licença

Este projeto está licenciado sob a **Licença MIT**. Veja o arquivo [LICENSE](LICENSE) para detalhes.

---
---

# 🎮 Android Gamepad KL Fixer (English)

[![minSdk](https://img.shields.io/badge/minSdk-21%20(Android%205.0)-brightgreen)](#)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-blue)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Kotlin Android application to **diagnose, generate, and install `.kl` (KeyLayout) files** for USB gamepads and joysticks. Requires **root access** to install files into `/system/usr/keylayout/`.

---

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Screens](#screens)
- [KL File Format](#kl-file-format)
- [Root Operations](#root-operations)
- [Requirements](#requirements)
- [Build Instructions](#build-instructions)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [License](#license)

---

## Overview

Android maps gamepad inputs using KeyLayout (`.kl`) files stored at:
```
/system/usr/keylayout/Vendor_XXXX_Product_XXXX.kl
```
When a gamepad has incorrect or missing mappings, buttons may be swapped, unresponsive, or report wrong keycodes. This app:

1. **Detects** all connected gamepads via `InputDevice` API, extracting Vendor ID and Product ID
2. **Captures** live `KeyEvent` and `MotionEvent` data from the physical device
3. **Generates** a syntactically correct `.kl` file from the captured data
4. **Installs** the file to `/system/usr/keylayout/` using root (`su`) — with automatic backup/restore

---

## Features

| Feature | Description |
|---|---|
| 🔍 Device Scanning | Lists all connected `SOURCE_GAMEPAD` and `SOURCE_JOYSTICK` devices |
| 🪪 Device Diagnostics | Shows Name, Vendor ID (hex/decimal), Product ID, descriptor, sources bitmask |
| ⌨️ KeyEvent Capture | Captures `keyCode`, `scanCode`, action (DOWN/UP), device ID in real time |
| 🕹️ MotionEvent Capture | Shows all axis values (X, Y, Z, RZ, HAT, triggers, etc.) live |
| 📄 KL Generation | Generates `Vendor_XXXX_Product_XXXX.kl` from default mappings or captured events |
| 💾 Local Save | Saves generated `.kl` to app private storage (`filesDir/keylayout/`) |
| 🔐 Root Check | Verifies `su` availability via `id` command (uid=0) |
| ⬆️ System Install | Remounts `/system` rw, copies file, sets permissions `644 root:root`, remounts ro |
| 🔄 Backup & Restore | Backs up existing `.kl` as `.kl.bak` before overwriting, one-tap restore |

---

## Architecture

The project follows **MVVM** with a clean separation of concerns:

```
app/
├── model/           # Data classes: GamepadDevice, KeyEventRecord, MotionEventRecord
├── device/          # DeviceScanner   — wraps InputDevice API
├── kl/              # KlFileGenerator — generates .kl content
│                    # KlFileStorage   — saves/loads .kl from private storage
├── root/            # RootManager     — all su operations (check, backup, install, restore)
└── ui/
    ├── MainActivity         → device list (RecyclerView + FAB)
    ├── DeviceDetailActivity → diagnostics + KL preview + action buttons
    ├── TestInputActivity    → live KeyEvent/MotionEvent capture
    ├── adapter/             → RecyclerView adapters (DeviceAdapter, KeyEventAdapter)
    └── viewmodel/           → DeviceListViewModel, DeviceDetailViewModel, TestInputViewModel
```

All I/O-bound operations (root commands, file writes) run on `Dispatchers.IO` via coroutines.

---

## Screens

### 1. Main Screen — Device List
- Shows all connected gamepads with name, Vendor/Product IDs, and expected KL filename
- FAB and menu item to rescan devices
- Empty state message when no device is connected

### 2. Device Detail
- Full diagnostics: name, Vendor ID (0x045E / 1118), Product ID, descriptor, sources
- Live preview of the generated `.kl` file (monospace scrollable)
- Buttons:
  - **Test Input** → opens capture screen
  - **Check Root** → verifies su access
  - **Save KL (local)** → saves to app storage
  - **Install to /system** → root install (enabled only after root confirmed)
  - **Restore Backup** → restores `.kl.bak` (enabled only after root confirmed)

### 3. Test Input
- Connect your gamepad and press buttons/move sticks
- Axis values displayed live (X, Y, Z, RZ, HAT_X, HAT_Y, LTRIGGER, RTRIGGER, etc.)
- Key event log shows keyCode name, scanCode, and action (UP/DOWN) for each press
- **Generate KL** uses captured events to produce a more accurate mapping

---

## KL File Format

```
# KeyLayout file generated by GamepadKLFixer
# Device: Xbox Wireless Controller
# Vendor ID : 045E (1118)
# Product ID: 028E (654)

# ----- Key Mappings -----
key 0x130   BUTTON_A   WAKE
key 0x131   BUTTON_B   WAKE
key 0x133   BUTTON_X   WAKE
key 0x134   BUTTON_Y   WAKE
key 0x136   BUTTON_L1  WAKE
key 0x137   BUTTON_R1  WAKE
key 0x13a   BUTTON_SELECT WAKE
key 0x13b   BUTTON_START  WAKE
key 0x13d   BUTTON_THUMBL WAKE
key 0x13e   BUTTON_THUMBR WAKE

# ----- Axis Mappings -----
axis ABS_X        X
axis ABS_Y        Y
axis ABS_Z        Z
axis ABS_RZ       RZ
axis ABS_HAT0X    HAT_X
axis ABS_HAT0Y    HAT_Y
axis ABS_BRAKE    LTRIGGER
axis ABS_GAS      RTRIGGER
```

Reference: [Android Key Layout Files](https://source.android.com/docs/core/interaction/input/key-layout-files)

---

## Root Operations

> ⚠️ Root operations only work on **rooted devices** with a superuser binary (`su`) available.

| Operation | Command executed (as root) |
|---|---|
| Check root | `id` (checks for `uid=0`) |
| List KL files | `ls -la /system/usr/keylayout/*.kl` |
| Backup | `cp Vendor_X.kl Vendor_X.kl.bak` |
| Install | `mount -o remount,rw /system` → `cp` → `chmod 644` → `chown root:root` → `mount -o remount,ro /system` |
| Restore | `mount rw` → `cp .bak` → `mount ro` |
| Remove | `mount rw` → `rm -f` → `mount ro` |

All commands are piped to a single `su` process to minimize prompt dialogs.

---

## Requirements

- Android **5.0+** (minSdk 21)
- Kotlin 1.9.x
- Android Gradle Plugin 8.4.x
- Gradle 8.7
- **Root access required** for system install/restore operations
- USB or Bluetooth gamepad / joystick for device detection

---

## Build Instructions

```bash
git clone https://github.com/dougretrogames/android-gamepad-kl-fixer.git
cd android-gamepad-kl-fixer

# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Release build
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Running Tests

```bash
# Unit tests (JVM, no device needed)
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

Unit tests cover:
- `KlFileGeneratorTest` — content generation, validation, hex formatting
- `GamepadDeviceTest` — data model correctness, KL filename format
- `KeyEventRecordTest` — action name mapping
- `RootManagerTest` — constants and result data class

---

## Project Structure

```
android-gamepad-kl-fixer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dougretrogames/gamepadklfixer/
│   │   │   │   ├── model/          GamepadDevice, KeyEventRecord, MotionEventRecord
│   │   │   │   ├── device/         DeviceScanner
│   │   │   │   ├── kl/             KlFileGenerator, KlFileStorage
│   │   │   │   ├── root/           RootManager
│   │   │   │   └── ui/             Activities, Adapters, ViewModels
│   │   │   ├── res/
│   │   │   │   ├── layout/         XML layouts for all screens and list items
│   │   │   │   ├── menu/           menu_main.xml
│   │   │   │   └── values/         strings, colors, themes
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   JVM unit tests
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml          Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
