package ui;

import domain.Building;
import domain.CampusCoordinate;
import domain.RoutePath;
import domain.RouteSegment;
import domain.RouteVisualizationModel;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class MapCanvasPane extends Pane {

    private static final Color[] ROUTE_COLORS = {
            Color.DARKGREEN,
            Color.CRIMSON,
            Color.ROYALBLUE,
            Color.DARKORANGE,
            Color.MEDIUMPURPLE,
            Color.TEAL,
            Color.DARKGOLDENROD
    };
    private static final double ROUTE_THICKNESS = 3.5;
    private static final double SEGMENT_OFFSET = 8.0;
    private static final double LABEL_OFFSET = 20.0;

    private final Canvas canvas;
    private Image backgroundImage;
    private RouteVisualizationModel visualizationModel;

    /** Creates the canvas container and hooks size listeners for redraws. */
    public MapCanvasPane() {
        canvas = new Canvas();
        getChildren().add(canvas);

        widthProperty().addListener((obs, oldV, newV) -> redraw());
        heightProperty().addListener((obs, oldV, newV) -> redraw());
    }

    /** Updates the map image and triggers a repaint. */
    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
        redraw();
    }

    /** Supplies the visualization data to be rendered on the canvas. */
    public void setVisualizationModel(RouteVisualizationModel model) {
        this.visualizationModel = model;
        redraw();
    }

    /** Clears any existing route rendering from the canvas. */
    public void clearVisualization() {
        this.visualizationModel = null;
        redraw();
    }

    /** Draws the map background, route segments, and start/end labels. */
    private void redraw() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        canvas.setWidth(width);
        canvas.setHeight(height);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double drawOffsetX = 0;
        double drawOffsetY = 0;
        double drawWidth = width;
        double drawHeight = height;

        if (backgroundImage != null) {
            double imageWidth = backgroundImage.getWidth();
            double imageHeight = backgroundImage.getHeight();
            double scale = Math.min(width / imageWidth, height / imageHeight);
            drawWidth = imageWidth * scale;
            drawHeight = imageHeight * scale;
            drawOffsetX = (width - drawWidth) / 2.0;
            drawOffsetY = (height - drawHeight) / 2.0;

            gc.drawImage(backgroundImage, drawOffsetX, drawOffsetY, drawWidth, drawHeight);
        }

        if (visualizationModel == null) {
            return;
        }

        RoutePath routePath = visualizationModel.getRoutePath();
        if (routePath == null) {
            return;
        }

        List<RouteSegment> segments = routePath.getSegments();
        gc.setLineWidth(ROUTE_THICKNESS);

        Map<String, Integer> totalByEdge = new HashMap<>();
        for (RouteSegment segment : segments) {
            totalByEdge.merge(normalizedEdgeKey(segment), 1, Integer::sum);
        }
        Map<String, Integer> indexByEdge = new HashMap<>();

        for (int i = 0; i < segments.size(); i++) {
            RouteSegment segment = segments.get(i);
            Point2D start = toCanvasPoint(segment.getFrom().getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);
            Point2D end = toCanvasPoint(segment.getTo().getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);

            String edgeKey = normalizedEdgeKey(segment);
            int total = totalByEdge.getOrDefault(edgeKey, 1);
            int index = indexByEdge.getOrDefault(edgeKey, 0);
            indexByEdge.put(edgeKey, index + 1);

            double offsetAmount = (total > 1) ? (index - (total - 1) / 2.0) * SEGMENT_OFFSET : 0.0;

            boolean canonical = segment.getFrom().getCode().compareToIgnoreCase(segment.getTo().getCode()) <= 0;
            Point2D baseStart = canonical ? start : end;
            Point2D baseEnd = canonical ? end : start;
            Point2D offsetVector = computePerpendicular(baseStart, baseEnd).multiply(offsetAmount);

            Point2D shiftedStart = start.add(offsetVector);
            Point2D shiftedEnd = end.add(offsetVector);

            Color color = ROUTE_COLORS[i % ROUTE_COLORS.length];
            gc.setStroke(color);
            gc.strokeLine(shiftedStart.getX(), shiftedStart.getY(), shiftedEnd.getX(), shiftedEnd.getY());
            if (i > 0) {
                drawArrowTail(gc, shiftedStart, color);
            }
            drawArrowHead(gc, shiftedStart, shiftedEnd, color);
        }

        List<Building> orderedBuildings = routePath.getOrderedBuildings();
        if (!orderedBuildings.isEmpty()) {
            Point2D startPoint = toCanvasPoint(orderedBuildings.get(0).getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);
            if (!segments.isEmpty()) {
                Point2D nextPoint = toCanvasPoint(segments.get(0).getTo().getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);
                Point2D dir = computeDirection(nextPoint, startPoint).multiply(LABEL_OFFSET);
                startPoint = startPoint.add(dir);
            } else {
                startPoint = startPoint.add(0, -LABEL_OFFSET);
            }
            drawLabel(gc, "START", startPoint, Color.WHITE, Color.BLACK);
        }
        if (orderedBuildings.size() > 1) {
            Point2D endPoint = toCanvasPoint(orderedBuildings.get(orderedBuildings.size() - 1).getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);
            if (!segments.isEmpty()) {
                RouteSegment lastSegment = segments.get(segments.size() - 1);
                Point2D prevPoint = toCanvasPoint(lastSegment.getFrom().getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight);
                Point2D dir = computeDirection(prevPoint, endPoint).multiply(LABEL_OFFSET);
                endPoint = endPoint.add(dir);
            } else {
                endPoint = endPoint.add(0, LABEL_OFFSET);
            }
            drawLabel(gc, "END", endPoint, Color.WHITE, Color.BLACK);
        } else if (orderedBuildings.size() == 1) {
            Point2D endPoint = toCanvasPoint(orderedBuildings.get(0).getLocation(), drawOffsetX, drawOffsetY, drawWidth, drawHeight)
                    .add(0, LABEL_OFFSET);
            drawLabel(gc, "END", endPoint, Color.WHITE, Color.BLACK);
        }
    }

    /** Produces a canonical key for a pair of buildings so offsets align. */
    private String normalizedEdgeKey(RouteSegment segment) {
        String a = segment.getFrom().getCode().toLowerCase();
        String b = segment.getTo().getCode().toLowerCase();
        return (a.compareTo(b) <= 0) ? a + "->" + b : b + "->" + a;
    }

    /** Calculates a unit perpendicular vector for offsetting overlapping lines. */
    private Point2D computePerpendicular(Point2D start, Point2D end) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double length = Math.hypot(dx, dy);
        if (length < 1e-6) {
            return new Point2D(0, 0);
        }
        double px = -dy / length;
        double py = dx / length;
        return new Point2D(px, py);
    }

    /** Returns the unit direction vector from one point to another. */
    private Point2D computeDirection(Point2D from, Point2D to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double length = Math.hypot(dx, dy);
        if (length < 1e-6) {
            return new Point2D(0, 0);
        }
        return new Point2D(dx / length, dy / length);
    }

    /** Converts normalized campus coordinates into canvas pixel positions. */
    private Point2D toCanvasPoint(CampusCoordinate coordinate,
                                   double offsetX,
                                   double offsetY,
                                   double drawWidth,
                                   double drawHeight) {
        if (coordinate == null) {
            return new Point2D(offsetX, offsetY);
        }
        double x = offsetX + coordinate.getX() * drawWidth;
        double y = offsetY + coordinate.getY() * drawHeight;
        return new Point2D(x, y);
    }

    /** Draws a small circle at the beginning of a segment to show direction. */
    private void drawArrowTail(GraphicsContext gc, Point2D point, Color color) {
        double radius = Math.max(4, ROUTE_THICKNESS * 0.9);
        gc.setFill(Color.WHITE);
        gc.fillOval(point.getX() - radius, point.getY() - radius, radius * 2, radius * 2);
        gc.setStroke(color.darker());
        gc.setLineWidth(1.5);
        gc.strokeOval(point.getX() - radius, point.getY() - radius, radius * 2, radius * 2);
        gc.setLineWidth(ROUTE_THICKNESS);
        gc.setStroke(color);
    }

    /** Renders a filled arrowhead at the segment's destination. */
    private void drawArrowHead(GraphicsContext gc, Point2D startPt, Point2D endPt, Color color) {
        double dx = endPt.getX() - startPt.getX();
        double dy = endPt.getY() - startPt.getY();
        double length = Math.hypot(dx, dy);
        if (length < 1e-6) {
            return;
        }

        double unitX = dx / length;
        double unitY = dy / length;
        double headLength = Math.max(18, ROUTE_THICKNESS * 4);
        double headWidth = Math.max(8, ROUTE_THICKNESS * 2.5);

        double baseX = endPt.getX() - unitX * headLength;
        double baseY = endPt.getY() - unitY * headLength;

        double leftX = baseX + (-unitY) * headWidth;
        double leftY = baseY + unitX * headWidth;
        double rightX = baseX - (-unitY) * headWidth;
        double rightY = baseY - unitX * headWidth;

        gc.setFill(color);
        gc.fillPolygon(new double[]{endPt.getX(), leftX, rightX}, new double[]{endPt.getY(), leftY, rightY}, 3);
    }

    /** Draws a callout with background and border anchored near the point. */
    private void drawLabel(GraphicsContext gc, String text, Point2D point, Color background, Color foreground) {
        Font font = Font.font(gc.getFont().getFamily(), FontWeight.BOLD, 13);
        gc.setFont(font);
        Text measurement = new Text(text);
        measurement.setFont(font);
        double textWidth = measurement.getLayoutBounds().getWidth();
        double textHeight = measurement.getLayoutBounds().getHeight();
        double padding = 4;

        double rectX = point.getX() - textWidth / 2 - padding;
        double rectY = point.getY() - textHeight - padding * 2;
        double rectWidth = textWidth + padding * 2;
        double rectHeight = textHeight + padding * 2;

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        if (rectX < 0) { rectX = 0; }
        if (rectX + rectWidth > canvasWidth) { rectX = canvasWidth - rectWidth; }
        if (rectY < 0) { rectY = point.getY() + padding; }
        if (rectY + rectHeight > canvasHeight) { rectY = canvasHeight - rectHeight; }

        gc.setFill(background.deriveColor(0, 1, 1, 0.85));
        gc.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 6, 6);
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRoundRect(rectX, rectY, rectWidth, rectHeight, 6, 6);

        gc.setFill(foreground);
        gc.fillText(text, rectX + padding, rectY + rectHeight - padding);
    }
}
