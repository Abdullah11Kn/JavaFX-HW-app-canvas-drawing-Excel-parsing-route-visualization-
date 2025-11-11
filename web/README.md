# Term Schedule Visualizer - Web Version

A modern web application that visualizes academic course schedules and calculates optimal routes between campus buildings. This is a web port of the original JavaFX desktop application.

## Features

- **Excel Schedule Parsing**: Upload and parse course schedule Excel files (.xlsx)
- **Route Visualization**: Visual representation of daily class routes on an interactive campus map
- **Distance Calculation**: Automatic calculation of walking distances between buildings
- **Interactive Canvas**: HTML5 Canvas-based rendering with directional arrows and labels
- **Responsive Design**: Works on desktop and mobile devices
- **Multi-Day Support**: View schedules for any day of the week

## Technologies Used

- **HTML5**: Semantic markup and structure
- **CSS3**: Modern responsive design with gradients and animations
- **JavaScript (ES6+)**: Modular architecture with classes and async/await
- **SheetJS (xlsx)**: Excel file parsing
- **HTML5 Canvas API**: Route visualization and rendering

## Project Structure

```
web/
├── index.html              # Main HTML file
├── css/
│   └── styles.css          # Application styles
├── js/
│   ├── models.js           # Domain models (Building, Course, etc.)
│   ├── services.js         # Business logic (Excel parsing, route planning)
│   ├── canvas-renderer.js  # Canvas visualization
│   └── app.js              # Main application controller
└── assets/
    ├── map.png             # Campus map image
    └── buildings.csv       # Building coordinates data
```

## How to Use

### Running Locally

1. **Clone or download this repository**

2. **Serve the web directory using a local web server**

   The application requires a web server due to ES6 module imports and CORS policies.

   **Option 1: Using Python**
   ```bash
   cd web
   python3 -m http.server 8000
   ```

   **Option 2: Using Node.js (http-server)**
   ```bash
   npm install -g http-server
   cd web
   http-server -p 8000
   ```

   **Option 3: Using PHP**
   ```bash
   cd web
   php -S localhost:8000
   ```

3. **Open your browser and navigate to:**
   ```
   http://localhost:8000
   ```

### Using the Application

1. **Load Schedule**
   - Click "Choose Excel File" to select your course schedule Excel file
   - Click "Load Excel" to parse the schedule
   - Wait for the success message

2. **Visualize Route**
   - Enter CRNs (Course Reference Numbers) separated by commas
     - Example: `12345, 12346, 12347`
   - Select a day of the week from the dropdown
   - Click "Visualize Route"
   - View the route on the canvas and summary information in the left panel

3. **Interpreting the Visualization**
   - **Colored lines**: Routes between buildings
   - **Arrows**: Direction of travel
   - **START label**: First building of the day (green)
   - **END label**: Last building of the day (red)
   - **Summary panel**: Shows course count, buildings visited, and total walking distance

## Excel File Format

The application expects an Excel file (.xlsx) with the following columns:

| Column | Description | Example |
|--------|-------------|---------|
| CRN | Course Reference Number | 12345 |
| Course | Course code | CS101 |
| Title | Course title | Introduction to Programming |
| Modality | Delivery mode (LEC/LAB/INT) | LEC |
| Days | Day codes (M/T/W/R/F/S/U) | MWF |
| Time | Time range | 09:00-10:15 |
| Building | Building code | 11 |
| Room | Room number | 201 |
| Instructor | Instructor name | John Doe |

**Day Codes:**
- M = Monday
- T = Tuesday
- W = Wednesday
- R/H = Thursday
- F = Friday
- S = Saturday
- U = Sunday

## Building Data

Building coordinates are stored in `assets/buildings.csv` with the format:
```csv
code, name, x_pixel, y_pixel
```

The application automatically normalizes pixel coordinates based on the map image dimensions.

## Browser Compatibility

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

Modern browsers with ES6 module support are required.

## Deployment

### GitHub Pages

1. Push the `web` directory to your repository
2. Go to Settings > Pages
3. Select the branch and `/web` folder
4. Your site will be published at `https://yourusername.github.io/repo-name/`

### Netlify

1. Drag and drop the `web` folder to Netlify
2. Or connect your Git repository and set build directory to `web`

### Vercel

1. Import your repository
2. Set the root directory to `web`
3. Deploy

## Customization

### Changing Colors

Edit the color array in `js/canvas-renderer.js`:
```javascript
this.colors = [
    '#228B22', // Change these hex codes
    '#DC143C',
    // ... add more colors
];
```

### Adding Buildings

Update `assets/buildings.csv`:
1. Add a new row with building code, name, and pixel coordinates
2. Coordinates should match positions on the map image

### Customizing Map

Replace `assets/map.png` with your own campus map image. Ensure building coordinates in CSV match the new map.

## Known Limitations

- Requires modern browser with ES6 support
- Must be served via HTTP/HTTPS (cannot run from file://)
- Excel files must follow the expected format
- Route calculation uses Euclidean distance (straight line)

## Troubleshooting

**"Failed to load building data"**
- Ensure `assets/buildings.csv` exists and is properly formatted
- Check browser console for detailed error messages

**"Failed to parse Excel file"**
- Verify Excel file format matches expected structure
- Ensure column headers include required fields (CRN, Course, etc.)

**Blank canvas or no visualization**
- Check that `assets/map.png` exists
- Verify CRNs exist in the loaded schedule
- Ensure selected day has scheduled classes

**Module import errors**
- Make sure you're accessing the app through a web server (not file://)
- Check that all JavaScript files are in the correct locations

## Credits

Web version created as a modern port of the JavaFX Term Schedule Visualizer application.

## License

This project is provided as-is for educational purposes.
