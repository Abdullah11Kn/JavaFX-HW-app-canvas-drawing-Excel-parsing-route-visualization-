package infra;

import domain.Building;
import domain.CampusCoordinate;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class CoordinateSeeder {
    private CoordinateSeeder() {
    }

    /** Reads buildings.csv and normalizes pixel coordinates against the provided map image. */
    public static void seed(BuildingRegistry registry, Image mapImage) {
        if (registry == null) {
            throw new IllegalArgumentException("Registry is required");
        }
        if (mapImage == null) {
            throw new IllegalArgumentException("Map image is required");
        }
        double width = mapImage.getWidth();
        double height = mapImage.getHeight();
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("Map image dimensions must be positive");
        }

        InputStream stream = CoordinateSeeder.class.getResourceAsStream("/buildings.csv");
        if (stream == null) {
            throw new IllegalStateException("buildings.csv resource not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue;
                }
                String code = parts[0].trim();
                String name = parts[1].trim();
                double pixelX = Double.parseDouble(parts[2].trim());
                double pixelY = Double.parseDouble(parts[3].trim());

                double normalizedX = Math.max(0, Math.min(1, pixelX / width));
                double normalizedY = Math.max(0, Math.min(1, pixelY / height));

                Building building = new Building(code, name, new CampusCoordinate(normalizedX, normalizedY));
                registry.register(building);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load building coordinates", ex);
        }
    }
}
