# Term Schedule Visualizer

A comprehensive application for visualizing academic course schedules and optimizing campus routes. Available in both **JavaFX (Desktop)** and **Web** versions.

## Overview

This application parses course schedule data from Excel files and visualizes optimal walking routes between campus buildings on an interactive map. It calculates distances, displays directional paths, and provides summary statistics for daily class schedules.

## Features

- Parse Excel course schedule files
- Calculate optimal routes between buildings based on class times
- Visualize routes on campus map with directional arrows
- Display walking distances and summary statistics
- Support for multiple days and course combinations
- Interactive canvas rendering

## Available Versions

### 1. JavaFX Desktop Application

**Location:** `/src`

A full-featured desktop application built with JavaFX 21 and Apache POI.

**Requirements:**
- Java 17+
- Gradle

**Running:**
```bash
./gradlew run
```

**Building:**
```bash
./gradlew build
```

### 2. Web Application

**Location:** `/web`

A modern web application with the same functionality, built using HTML5, CSS3, and JavaScript.

**Requirements:**
- Modern web browser (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+)
- Local web server (Python, Node.js, or PHP)

**Running:**
```bash
cd web
python3 -m http.server 8000
# Open http://localhost:8000 in your browser
```

See `/web/README.md` for detailed web version documentation.

## Quick Start

1. **Choose your version** (Desktop or Web)
2. **Prepare your schedule** as an Excel file with required columns (CRN, Course, Days, Time, Building, etc.)
3. **Load the schedule** into the application
4. **Enter CRNs** for courses you want to visualize
5. **Select a day** of the week
6. **View the route** visualization and walking distance

## Project Structure

```
.
├── src/                    # JavaFX desktop application
│   └── main/
│       ├── java/          # Java source code
│       └── resources/     # Map image and building data
├── web/                   # Web application
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── assets/
├── build.gradle.kts       # Gradle build configuration
└── README.md             # This file
```

## Technologies

**Desktop Version:**
- JavaFX 21.0.1
- Apache POI 5.2.5
- Java 17
- Gradle

**Web Version:**
- HTML5 Canvas API
- ES6+ JavaScript Modules
- SheetJS (xlsx)
- Responsive CSS3

## License

Educational project for course scheduling and route visualization.