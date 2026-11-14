# ARNIX Code AI

Android-first AI coding workspace with an OpenAI-compatible chat connection and GitHub Actions debug builds.

## Current milestone
The first Android scaffold includes a Compose workspace, chat interface, FreeLLMAPI-compatible endpoint settings, model selection (including `auto`), model-list connectivity check, and a debug APK workflow.

This is not yet the complete autonomous coding agent. Project file operations, diff review, sandboxed command execution, test/repair loops, website preview, and GitHub project automation are planned next.

## Build
Open this repository in Android Studio, or download the debug APK artifact after the **Android Debug APK** workflow finishes in the Actions tab.

## Connect an AI endpoint
In the app settings, enter an OpenAI-compatible base URL ending in `/v1` (for example, your own FreeLLMAPI server) and your own API key. The app does not include public or embedded third-party keys.

## Security
The current prototype keeps the API key in screen memory only; it does not persist the key. Never commit API keys. A future credential-storage milestone will use Android Keystore-backed encryption. Do not expose a self-hosted API server to the public internet without HTTPS and authentication.
