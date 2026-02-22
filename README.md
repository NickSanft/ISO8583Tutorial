# ISO8583 Tutorial - Interactive Learning Platform

A complete Ktor web application designed to teach ISO8583 financial messaging through interactive tutorials and a live message simulator.

## Overview

This application provides:
- **Comprehensive Tutorials**: Learn about MTI, Bitmaps, and Data Fields
- **Interactive Simulator**: Build and send real ISO8583 messages
- **Mock Payment Host**: See how transactions are approved/declined
- **Visual Breakdown**: Understand hex encoding and bitmap structures

## Technical Stack

- **Framework**: Ktor 3.4.0
- **Language**: Kotlin 2.3.0
- **ISO8583 Library**: JPOS 3.0.1
- **Templating**: Ktor HTML DSL
- **Build Tool**: Gradle with Kotlin DSL

## Project Structure

```
src/main/kotlin/com/divora/
├── Application.kt              # Main application entry point
├── Routing.kt                  # Route configuration
├── iso8583/
│   ├── BitmapHelper.kt        # Bitmap generation and parsing
│   ├── ISO8583Message.kt      # Message building and parsing
│   └── MockHostService.kt     # Payment processor simulation
├── routes/
│   ├── TutorialRoutes.kt      # Tutorial page routes
│   └── SimulatorRoutes.kt     # Simulator and processing routes
└── views/
    ├── CommonLayout.kt        # Shared HTML layout and CSS
    ├── TutorialPages.kt       # Tutorial content pages
    └── SimulatorPages.kt      # Interactive simulator pages
```

## Features

### Tutorial Pages
1. **Introduction**: Overview of ISO8583 standard
2. **MTI (Message Type Indicator)**: 4-digit message classification
3. **Bitmap**: Visual hex-to-binary conversion examples
4. **Data Fields**: Common field descriptions and formats

### Interactive Simulator
- Build 0200 (Authorization Request) messages
- Input fields: Processing Code, Amount, STAN, Terminal ID, Merchant ID
- Real-time hex message generation
- Visual field breakdown

### Mock Host Logic
- Processes authorization requests
- **Approval Rule**: Amounts ending in "00" (e.g., $10.00) → Approved (Code 39='00')
- **Decline Rule**: All other amounts → Declined (Code 39='05')
- Returns 0210 (Authorization Response) with echoed fields

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
|-----------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

