// Canvas Renderer - Visualization Layer
export class MapCanvasRenderer {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.mapImage = null;
        this.visualizationModel = null;
        this.colors = [
            '#228B22', // DARKGREEN
            '#DC143C', // CRIMSON
            '#4169E1', // ROYALBLUE
            '#FF8C00', // DARKORANGE
            '#9370DB', // MEDIUMPURPLE
            '#008080', // TEAL
            '#B8860B'  // DARKGOLDENROD
        ];
        this.mapLoaded = false;
        this.loadMap();
    }

    async loadMap() {
        return new Promise((resolve, reject) => {
            this.mapImage = new Image();
            this.mapImage.onload = () => {
                this.mapLoaded = true;
                this.redraw();
                resolve();
            };
            this.mapImage.onerror = reject;
            this.mapImage.src = 'assets/map.png';
        });
    }

    setVisualizationModel(model) {
        this.visualizationModel = model;
        this.redraw();
    }

    redraw() {
        if (!this.mapLoaded) return;

        // Set canvas size
        const container = this.canvas.parentElement;
        const maxWidth = container.clientWidth - 40;
        const maxHeight = container.clientHeight - 40;

        const aspectRatio = this.mapImage.width / this.mapImage.height;
        let canvasWidth = maxWidth;
        let canvasHeight = maxWidth / aspectRatio;

        if (canvasHeight > maxHeight) {
            canvasHeight = maxHeight;
            canvasWidth = maxHeight * aspectRatio;
        }

        this.canvas.width = canvasWidth;
        this.canvas.height = canvasHeight;

        // Clear canvas
        this.ctx.clearRect(0, 0, canvasWidth, canvasHeight);

        // Draw map background
        this.ctx.drawImage(this.mapImage, 0, 0, canvasWidth, canvasHeight);

        // Draw route if available
        if (this.visualizationModel && !this.visualizationModel.routePath.isEmpty()) {
            this.drawRoute(this.visualizationModel.routePath);
        }
    }

    drawRoute(routePath) {
        const segments = routePath.segments;
        if (segments.length === 0) return;

        const canvasWidth = this.canvas.width;
        const canvasHeight = this.canvas.height;

        // Draw segments with arrows
        segments.forEach((segment, index) => {
            const color = this.colors[index % this.colors.length];
            this.drawSegment(segment, color, canvasWidth, canvasHeight, index);
        });

        // Draw START label
        if (routePath.buildings.length > 0) {
            const firstBuilding = routePath.buildings[0];
            this.drawLabel('START', firstBuilding, canvasWidth, canvasHeight, '#228B22');
        }

        // Draw END label
        if (routePath.buildings.length > 1) {
            const lastBuilding = routePath.buildings[routePath.buildings.length - 1];
            this.drawLabel('END', lastBuilding, canvasWidth, canvasHeight, '#DC143C');
        }
    }

    drawSegment(segment, color, canvasWidth, canvasHeight, segmentIndex) {
        const fromLoc = segment.fromBuilding.getPrimaryEntrance();
        const toLoc = segment.toBuilding.getPrimaryEntrance();

        // Convert normalized coordinates to canvas coordinates
        let x1 = fromLoc.x * canvasWidth;
        let y1 = fromLoc.y * canvasHeight;
        let x2 = toLoc.x * canvasWidth;
        let y2 = toLoc.y * canvasHeight;

        // Apply offset for overlapping routes
        const offset = (segmentIndex % 3 - 1) * 8;
        if (offset !== 0) {
            const dx = x2 - x1;
            const dy = y2 - y1;
            const length = Math.sqrt(dx * dx + dy * dy);
            const perpX = -dy / length * offset;
            const perpY = dx / length * offset;

            x1 += perpX;
            y1 += perpY;
            x2 += perpX;
            y2 += perpY;
        }

        // Draw line
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 3;
        this.ctx.beginPath();
        this.ctx.moveTo(x1, y1);
        this.ctx.lineTo(x2, y2);
        this.ctx.stroke();

        // Draw start circle
        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.arc(x1, y1, 6, 0, 2 * Math.PI);
        this.ctx.fill();

        // Draw arrow head
        this.drawArrowHead(x1, y1, x2, y2, color);
    }

    drawArrowHead(x1, y1, x2, y2, color) {
        const headLength = 15;
        const headWidth = 8;

        const dx = x2 - x1;
        const dy = y2 - y1;
        const angle = Math.atan2(dy, dx);

        // Arrow tip point
        const tipX = x2;
        const tipY = y2;

        // Arrow base points
        const baseX1 = tipX - headLength * Math.cos(angle - Math.PI / 6);
        const baseY1 = tipY - headLength * Math.sin(angle - Math.PI / 6);
        const baseX2 = tipX - headLength * Math.cos(angle + Math.PI / 6);
        const baseY2 = tipY - headLength * Math.sin(angle + Math.PI / 6);

        // Draw filled triangle
        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.moveTo(tipX, tipY);
        this.ctx.lineTo(baseX1, baseY1);
        this.ctx.lineTo(baseX2, baseY2);
        this.ctx.closePath();
        this.ctx.fill();
    }

    drawLabel(text, building, canvasWidth, canvasHeight, color) {
        const loc = building.getPrimaryEntrance();
        let x = loc.x * canvasWidth;
        let y = loc.y * canvasHeight;

        // Measure text
        this.ctx.font = 'bold 14px Arial';
        const metrics = this.ctx.measureText(text);
        const textWidth = metrics.width;
        const textHeight = 16;

        // Offset label to avoid overlap
        const offsetX = 15;
        const offsetY = -15;
        x += offsetX;
        y += offsetY;

        // Keep label within canvas bounds
        if (x + textWidth + 10 > canvasWidth) {
            x = canvasWidth - textWidth - 10;
        }
        if (x < 5) {
            x = 5;
        }
        if (y - textHeight < 5) {
            y = textHeight + 5;
        }
        if (y > canvasHeight - 5) {
            y = canvasHeight - 5;
        }

        // Draw background
        this.ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
        this.ctx.fillRect(x - 5, y - textHeight, textWidth + 10, textHeight + 6);

        // Draw border
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(x - 5, y - textHeight, textWidth + 10, textHeight + 6);

        // Draw text
        this.ctx.fillStyle = color;
        this.ctx.fillText(text, x, y);
    }

    clear() {
        this.visualizationModel = null;
        this.redraw();
    }
}
