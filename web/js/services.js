// Services - Business Logic Layer
import {
    Building, CampusCoordinate, Room, Instructor, Course,
    TimeSlot, MeetingSession, CourseOffering, ItineraryEntry,
    DailyItinerary, TermSchedule, RouteSegment, RoutePath,
    RouteVisualizationModel, DayOfWeek, ActivityType, DeliveryMode
} from './models.js';

// BuildingRegistry - Manages building data
export class BuildingRegistry {
    constructor() {
        this.buildings = new Map();
        this.initialized = false;
    }

    async initialize() {
        if (this.initialized) return;

        try {
            const response = await fetch('assets/buildings.csv');
            const csvText = await response.text();
            const lines = csvText.trim().split('\n');

            // Get image dimensions
            const img = new Image();
            img.src = 'assets/map.png';
            await new Promise((resolve) => {
                img.onload = resolve;
            });

            const imageWidth = img.width;
            const imageHeight = img.height;

            // Parse CSV (skip header if present)
            const dataLines = lines[0].includes('code') ? lines.slice(1) : lines;

            dataLines.forEach(line => {
                const parts = line.split(',').map(s => s.trim().replace(/"/g, ''));
                if (parts.length >= 4) {
                    const code = parts[0];
                    const name = parts[1];
                    const pixelX = parseFloat(parts[2]);
                    const pixelY = parseFloat(parts[3]);

                    const location = CampusCoordinate.fromPixels(
                        pixelX, pixelY, imageWidth, imageHeight
                    );

                    const building = new Building(code, name, location, [location]);
                    this.buildings.set(code, building);
                }
            });

            this.initialized = true;
        } catch (error) {
            console.error('Error loading buildings:', error);
            throw new Error('Failed to load building data');
        }
    }

    getBuilding(code) {
        if (!this.buildings.has(code)) {
            // Create placeholder building at center
            const centerLocation = new CampusCoordinate(0.5, 0.5);
            const building = new Building(code, `Building ${code}`, centerLocation, [centerLocation]);
            this.buildings.set(code, building);
        }
        return this.buildings.get(code);
    }

    getAllBuildings() {
        return Array.from(this.buildings.values());
    }
}

// DistanceCalculator
export class DistanceCalculator {
    constructor() {
        this.calibrationScale = 900.0; // Default meters per normalized unit
    }

    async calibrate(buildingRegistry) {
        try {
            const building59 = buildingRegistry.getBuilding('59');
            const building11 = buildingRegistry.getBuilding('11');

            if (building59 && building11) {
                const knownDistance = 350.0; // meters
                const dx = building59.location.x - building11.location.x;
                const dy = building59.location.y - building11.location.y;
                const normalizedDistance = Math.sqrt(dx * dx + dy * dy);

                if (normalizedDistance > 0) {
                    this.calibrationScale = knownDistance / normalizedDistance;
                }
            }
        } catch (error) {
            console.warn('Calibration failed, using default scale');
        }
    }

    calculate(from, to) {
        const fromLoc = from.getPrimaryEntrance();
        const toLoc = to.getPrimaryEntrance();

        const dx = toLoc.x - fromLoc.x;
        const dy = toLoc.y - fromLoc.y;
        const normalizedDistance = Math.sqrt(dx * dx + dy * dy);

        return normalizedDistance * this.calibrationScale;
    }
}

// ExcelScheduleRepository
export class ExcelScheduleRepository {
    constructor() {
        this.termSchedule = null;
        this.buildingRegistry = new BuildingRegistry();
    }

    async initialize() {
        await this.buildingRegistry.initialize();
    }

    parseDayOfWeek(dayCode) {
        const mapping = {
            'U': DayOfWeek.SUNDAY,
            'M': DayOfWeek.MONDAY,
            'T': DayOfWeek.TUESDAY,
            'W': DayOfWeek.WEDNESDAY,
            'R': DayOfWeek.THURSDAY,
            'H': DayOfWeek.THURSDAY,
            'F': DayOfWeek.FRIDAY,
            'S': DayOfWeek.SATURDAY
        };
        return mapping[dayCode.toUpperCase()] || null;
    }

    parseActivityType(modality) {
        const upper = (modality || '').toUpperCase();
        if (upper.includes('LEC')) return ActivityType.LECTURE;
        if (upper.includes('LAB')) return ActivityType.LAB;
        if (upper.includes('INT')) return ActivityType.INTERNSHIP;
        return ActivityType.OTHER;
    }

    parseDeliveryMode(modality) {
        const upper = (modality || '').toUpperCase();
        if (upper.includes('LEC')) return DeliveryMode.LECTURE;
        if (upper.includes('LAB')) return DeliveryMode.LAB;
        if (upper.includes('INT')) return DeliveryMode.INTERNSHIP;
        return DeliveryMode.OTHER;
    }

    parseTimeSlot(timeString) {
        if (!timeString || timeString.trim() === '') return null;

        try {
            const parts = timeString.split('-');
            if (parts.length !== 2) return null;

            const startTime = parts[0].trim();
            const endTime = parts[1].trim();

            return new TimeSlot(startTime, endTime);
        } catch (error) {
            return null;
        }
    }

    parseRoom(buildingCode, roomCode) {
        if (!buildingCode || buildingCode.trim() === '') return null;

        const building = this.buildingRegistry.getBuilding(buildingCode.trim());
        const floor = roomCode ? parseInt(roomCode.toString().charAt(0)) || 0 : 0;
        const identifier = roomCode ? roomCode.toString() : '';

        return new Room(building, floor, identifier);
    }

    async loadFromFile(file) {
        try {
            const data = await file.arrayBuffer();
            const workbook = XLSX.read(data, { type: 'array' });
            const sheetName = workbook.SheetNames[0];
            const sheet = workbook.Sheets[sheetName];
            const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });

            // Find header row
            const headerRow = rows.find(row =>
                row.some(cell => cell && cell.toString().toUpperCase().includes('CRN'))
            );

            if (!headerRow) {
                throw new Error('Could not find header row with CRN column');
            }

            const headerIndex = rows.indexOf(headerRow);
            const headers = headerRow.map(h => h.toString().toLowerCase());

            // Find column indices
            const crnIdx = headers.findIndex(h => h.includes('crn'));
            const courseIdx = headers.findIndex(h => h.includes('course') && !h.includes('title'));
            const titleIdx = headers.findIndex(h => h.includes('title'));
            const modalityIdx = headers.findIndex(h => h.includes('modality'));
            const daysIdx = headers.findIndex(h => h.includes('days'));
            const timeIdx = headers.findIndex(h => h.includes('time'));
            const buildingIdx = headers.findIndex(h => h.includes('building') || h.includes('bldg'));
            const roomIdx = headers.findIndex(h => h.includes('room'));
            const instructorIdx = headers.findIndex(h => h.includes('instructor'));

            const offerings = [];
            const courseCache = new Map();
            const instructorCache = new Map();

            // Process data rows
            for (let i = headerIndex + 1; i < rows.length; i++) {
                const row = rows[i];
                if (!row || row.length === 0) continue;

                const crnValue = row[crnIdx];
                if (!crnValue) continue;

                // Handle numeric CRNs properly (remove decimals, convert from scientific notation)
                let crn;
                if (typeof crnValue === 'number') {
                    // Convert number to integer string (removes .0 and handles scientific notation)
                    crn = Math.floor(crnValue).toString();
                } else {
                    crn = crnValue.toString().trim();
                }

                // Remove any remaining decimal points and trailing zeros
                crn = crn.replace(/\.0+$/, '');
                const courseCode = row[courseIdx] ? row[courseIdx].toString().trim() : '';
                const courseTitle = row[titleIdx] ? row[titleIdx].toString().trim() : '';
                const modality = row[modalityIdx] ? row[modalityIdx].toString().trim() : '';
                const daysString = row[daysIdx] ? row[daysIdx].toString().trim() : '';
                const timeString = row[timeIdx] ? row[timeIdx].toString().trim() : '';
                const buildingCode = row[buildingIdx] ? row[buildingIdx].toString().trim() : '';
                const roomCode = row[roomIdx] ? row[roomIdx].toString().trim() : '';
                const instructorName = row[instructorIdx] ? row[instructorIdx].toString().trim() : '';

                // Get or create course
                let course = courseCache.get(courseCode);
                if (!course) {
                    course = new Course(courseCode, courseTitle);
                    courseCache.set(courseCode, course);
                }

                // Get or create instructor
                let instructor = null;
                if (instructorName) {
                    instructor = instructorCache.get(instructorName);
                    if (!instructor) {
                        const nameParts = instructorName.split(' ');
                        const firstName = nameParts[0] || '';
                        const lastName = nameParts.slice(1).join(' ') || '';
                        instructor = new Instructor(firstName, lastName);
                        instructorCache.set(instructorName, instructor);
                    }
                }

                // Parse delivery mode and activity type
                const deliveryMode = this.parseDeliveryMode(modality);
                const activityType = this.parseActivityType(modality);

                // Parse meeting sessions
                const meetingSessions = [];
                const timeSlot = this.parseTimeSlot(timeString);
                const room = this.parseRoom(buildingCode, roomCode);

                if (timeSlot && room && daysString) {
                    for (const dayChar of daysString) {
                        const dayOfWeek = this.parseDayOfWeek(dayChar);
                        if (dayOfWeek) {
                            const session = new MeetingSession(dayOfWeek, timeSlot, room, activityType);
                            meetingSessions.push(session);
                        }
                    }
                }

                const instructors = instructor ? [instructor] : [];
                const offering = new CourseOffering(crn, course, deliveryMode, meetingSessions, instructors);
                offerings.push(offering);
            }

            this.termSchedule = new TermSchedule(offerings);

            // Debug: Log all loaded CRNs
            console.log(`Loaded ${offerings.length} course offerings with CRNs:`,
                Array.from(this.termSchedule.crnMap.keys()).sort().join(', '));

            return this.termSchedule;
        } catch (error) {
            console.error('Error parsing Excel:', error);
            throw new Error(`Failed to parse Excel file: ${error.message}`);
        }
    }

    getTermSchedule() {
        return this.termSchedule;
    }

    getBuildingRegistry() {
        return this.buildingRegistry;
    }
}

// ScheduleService
export class ScheduleService {
    constructor(repository) {
        this.repository = repository;
    }

    getOfferingsByCrns(crns) {
        const schedule = this.repository.getTermSchedule();
        if (!schedule) return [];

        return schedule.findAllByCrns(crns);
    }

    getDailyItineraryFromOfferings(offerings, dayOfWeek) {
        const entries = [];

        offerings.forEach(offering => {
            const sessions = offering.getSessionsForDay(dayOfWeek);
            sessions.forEach(session => {
                entries.push(new ItineraryEntry(offering, session));
            });
        });

        return new DailyItinerary(dayOfWeek, entries);
    }
}

// RoutePlanningService
export class RoutePlanningService {
    constructor(distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    buildVisualization(itinerary) {
        const routePath = this.buildRoutePath(itinerary);
        const courses = itinerary.entries.map(e => e.courseOffering);
        const summaryLines = this.buildSummary(itinerary, routePath);

        return new RouteVisualizationModel(courses, routePath, summaryLines);
    }

    buildRoutePath(itinerary) {
        const entries = itinerary.getOrderedEntries();
        if (entries.length === 0) {
            return new RoutePath([], []);
        }

        const buildings = [];
        const segments = [];

        for (let i = 0; i < entries.length; i++) {
            const entry = entries[i];
            const building = entry.meetingSession.room.building;
            buildings.push(building);

            if (i > 0) {
                const prevBuilding = buildings[i - 1];
                const distance = this.distanceCalculator.calculate(prevBuilding, building);
                segments.push(new RouteSegment(prevBuilding, building, distance));
            }
        }

        return new RoutePath(buildings, segments);
    }

    buildSummary(itinerary, routePath) {
        const lines = [];

        lines.push(`Selected Day: ${itinerary.dayOfWeek}`);
        lines.push(`Number of Courses: ${itinerary.entries.length}`);

        if (itinerary.entries.length > 0) {
            const courseList = itinerary.entries
                .map(e => e.courseOffering.course.code)
                .join(', ');
            lines.push(`Courses: ${courseList}`);

            const uniqueBuildings = new Set(routePath.buildings.map(b => b.code));
            lines.push(`Buildings Visited: ${uniqueBuildings.size}`);

            const totalDistance = routePath.getTotalDistance();
            lines.push(`Total Distance: ${totalDistance.toFixed(0)} meters`);
        }

        return lines;
    }
}
