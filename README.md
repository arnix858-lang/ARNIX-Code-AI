# ARNIX Code AI

Android-first AI coding workspace with a project editor, FreeLLMAPI-compatible chat, and GitHub Actions builds.

## Status
Initial scaffold. The app currently provides a coding workspace and OpenAI-compatible chat connection. Autonomous file tools, sandboxed execution, previews, and GitHub project operations are staged for subsequent milestones.

## Build
Open this repository in Android Studio, or run the GitHub Actions workflow **Android Debug APK** from the Actions tab.

## Connect an AI endpoint
In the app settings, enter an OpenAI-compatible base URL (for example, your own FreeLLMAPI server ending in `/v1`) and your own API key. The app does not include public or embedded third-party keys.

## Security
Never commit API keys. Local credentials are stored with Android encrypted preferences. Do not expose a self-hosted API server to the public internet without HTTPS and authentication.
