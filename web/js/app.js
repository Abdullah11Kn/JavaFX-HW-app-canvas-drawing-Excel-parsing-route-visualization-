// Main Application Controller
import { MapCanvasRenderer } from './canvas-renderer.js';
import {
    ExcelScheduleRepository,
    ScheduleService,
    RoutePlanningService,
    DistanceCalculator
} from './services.js';
import { DayOfWeek } from './models.js';

class TermScheduleApp {
    constructor() {
        this.repository = null;
        this.scheduleService = null;
        this.routePlanningService = null;
        this.canvasRenderer = null;
        this.selectedFile = null;

        this.initializeUI();
        this.initializeCanvas();
    }

    initializeUI() {
        // Get DOM elements
        this.fileInput = document.getElementById('excelFile');
        this.fileNameSpan = document.getElementById('fileName');
        this.loadBtn = document.getElementById('loadBtn');
        this.crnInput = document.getElementById('crnInput');
        this.daySelect = document.getElementById('daySelect');
        this.visualizeBtn = document.getElementById('visualizeBtn');
        this.summaryArea = document.getElementById('summaryArea');

        // Modal elements
        this.modal = document.getElementById('messageModal');
        this.modalTitle = document.getElementById('modalTitle');
        this.modalMessage = document.getElementById('modalMessage');
        this.closeModal = document.querySelector('.close');

        // Event listeners
        this.fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        this.loadBtn.addEventListener('click', () => this.handleLoadExcel());
        this.visualizeBtn.addEventListener('click', () => this.handleVisualize());
        this.closeModal.addEventListener('click', () => this.hideModal());
        window.addEventListener('click', (e) => {
            if (e.target === this.modal) this.hideModal();
        });
        window.addEventListener('resize', () => {
            if (this.canvasRenderer) this.canvasRenderer.redraw();
        });

        // Disable visualize button initially
        this.visualizeBtn.disabled = true;
    }

    initializeCanvas() {
        const canvas = document.getElementById('mapCanvas');
        this.canvasRenderer = new MapCanvasRenderer(canvas);
    }

    handleFileSelect(event) {
        const files = event.target.files;
        if (files.length > 0) {
            this.selectedFile = files[0];
            this.fileNameSpan.textContent = files[0].name;
        } else {
            this.selectedFile = null;
            this.fileNameSpan.textContent = 'No file chosen';
        }
    }

    async handleLoadExcel() {
        if (!this.selectedFile) {
            this.showModal('Error', 'Please select an Excel file first.');
            return;
        }

        try {
            this.loadBtn.disabled = true;
            this.loadBtn.innerHTML = '<span class="loading"></span> Loading...';

            // Initialize repository
            this.repository = new ExcelScheduleRepository();
            await this.repository.initialize();

            // Load Excel file
            await this.repository.loadFromFile(this.selectedFile);

            // Initialize services
            const distanceCalculator = new DistanceCalculator();
            await distanceCalculator.calibrate(this.repository.getBuildingRegistry());

            this.scheduleService = new ScheduleService(this.repository);
            this.routePlanningService = new RoutePlanningService(distanceCalculator);

            // Enable visualize button
            this.visualizeBtn.disabled = false;

            this.showModal('Success', 'Excel file loaded successfully! You can now enter CRNs and visualize routes.');

            this.loadBtn.disabled = false;
            this.loadBtn.textContent = 'Load Excel';
        } catch (error) {
            console.error('Error loading Excel:', error);
            this.showModal('Error', `Failed to load Excel file: ${error.message}`);
            this.loadBtn.disabled = false;
            this.loadBtn.textContent = 'Load Excel';
        }
    }

    async handleVisualize() {
        if (!this.scheduleService) {
            this.showModal('Error', 'Please load an Excel file first.');
            return;
        }

        const crnText = this.crnInput.value.trim();
        if (!crnText) {
            this.showModal('Error', 'Please enter at least one CRN.');
            return;
        }

        try {
            this.visualizeBtn.disabled = true;
            this.visualizeBtn.innerHTML = '<span class="loading"></span> Visualizing...';

            // Parse CRNs - normalize them the same way as Excel parsing
            const crns = crnText.split(',')
                .map(crn => crn.trim())
                .map(crn => {
                    // Handle numeric strings consistently
                    const num = parseFloat(crn);
                    if (!isNaN(num)) {
                        return Math.floor(num).toString();
                    }
                    return crn;
                })
                .filter(crn => crn);

            console.log('Searching for CRNs:', crns);

            if (crns.length === 0) {
                this.showModal('Error', 'No valid CRNs entered.');
                this.visualizeBtn.disabled = false;
                this.visualizeBtn.textContent = 'Visualize Route';
                return;
            }

            // Get offerings
            const offerings = this.scheduleService.getOfferingsByCrns(crns);

            console.log(`Found ${offerings.length} offerings for ${crns.length} CRNs`);

            // Check for missing CRNs
            const foundCrns = offerings.map(o => o.crn);
            const missingCrns = crns.filter(crn => !foundCrns.includes(crn));

            if (missingCrns.length > 0) {
                console.warn('Missing CRNs:', missingCrns);
                this.showModal(
                    'Warning',
                    `The following CRNs were not found: ${missingCrns.join(', ')}. Continuing with found courses.`
                );
            }

            if (offerings.length === 0) {
                this.showModal('Error', 'No courses found for the entered CRNs.');
                this.visualizeBtn.disabled = false;
                this.visualizeBtn.textContent = 'Visualize Route';
                return;
            }

            // Get selected day
            const selectedDay = this.daySelect.value;

            // Build itinerary
            const itinerary = this.scheduleService.getDailyItineraryFromOfferings(offerings, selectedDay);

            if (itinerary.entries.length === 0) {
                this.showModal(
                    'Info',
                    `No classes found for ${selectedDay}. The selected courses may not have classes on this day.`
                );
                this.clearVisualization();
                this.visualizeBtn.disabled = false;
                this.visualizeBtn.textContent = 'Visualize Route';
                return;
            }

            // Build visualization
            const visualizationModel = this.routePlanningService.buildVisualization(itinerary);

            // Update canvas
            this.canvasRenderer.setVisualizationModel(visualizationModel);

            // Update summary
            this.updateSummary(visualizationModel);

            this.visualizeBtn.disabled = false;
            this.visualizeBtn.textContent = 'Visualize Route';
        } catch (error) {
            console.error('Error visualizing route:', error);
            this.showModal('Error', `Failed to visualize route: ${error.message}`);
            this.visualizeBtn.disabled = false;
            this.visualizeBtn.textContent = 'Visualize Route';
        }
    }

    updateSummary(model) {
        const lines = model.summaryLines;
        let html = '';

        lines.forEach((line, index) => {
            if (index === 0) {
                html += `<p><strong>${line}</strong></p>`;
            } else {
                html += `<p>${line}</p>`;
            }
        });

        this.summaryArea.innerHTML = html;
    }

    clearVisualization() {
        this.summaryArea.innerHTML = '<p class="info-text">No classes scheduled for the selected day.</p>';
        this.canvasRenderer.clear();
    }

    showModal(title, message) {
        this.modalTitle.textContent = title;
        this.modalMessage.textContent = message;
        this.modal.style.display = 'block';
    }

    hideModal() {
        this.modal.style.display = 'none';
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new TermScheduleApp();
});
