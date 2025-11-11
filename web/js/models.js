// Domain Models - Converted from Java to JavaScript

// Enums
export const DayOfWeek = {
    SUNDAY: 'SUNDAY',
    MONDAY: 'MONDAY',
    TUESDAY: 'TUESDAY',
    WEDNESDAY: 'WEDNESDAY',
    THURSDAY: 'THURSDAY',
    FRIDAY: 'FRIDAY',
    SATURDAY: 'SATURDAY'
};

export const ActivityType = {
    LECTURE: 'LECTURE',
    LAB: 'LAB',
    INTERNSHIP: 'INTERNSHIP',
    OTHER: 'OTHER'
};

export const DeliveryMode = {
    LECTURE: 'LECTURE',
    LAB: 'LAB',
    INTERNSHIP: 'INTERNSHIP',
    OTHER: 'OTHER'
};

// CampusCoordinate
export class CampusCoordinate {
    constructor(x, y) {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            throw new Error(`Coordinates must be in range [0,1]. Got x=${x}, y=${y}`);
        }
        this.x = x;
        this.y = y;
    }

    static fromPixels(pixelX, pixelY, imageWidth, imageHeight) {
        return new CampusCoordinate(
            pixelX / imageWidth,
            pixelY / imageHeight
        );
    }
}

// Building
export class Building {
    constructor(code, name, location, entrancePoints = []) {
        this.code = code;
        this.name = name;
        this.location = location; // CampusCoordinate
        this.entrancePoints = entrancePoints;
    }

    getPrimaryEntrance() {
        return this.entrancePoints.length > 0 ? this.entrancePoints[0] : this.location;
    }
}

// Room
export class Room {
    constructor(building, floor, identifier) {
        this.building = building;
        this.floor = floor;
        this.identifier = identifier;
    }
}

// Instructor
export class Instructor {
    constructor(firstName, lastName, email = null) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    getFullName() {
        return `${this.firstName} ${this.lastName}`;
    }
}

// Course
export class Course {
    constructor(code, title, department = null) {
        this.code = code;
        this.title = title;
        this.department = department;
    }
}

// TimeSlot
export class TimeSlot {
    constructor(startTime, endTime) {
        this.startTime = startTime; // "HH:MM" format
        this.endTime = endTime;
    }

    overlaps(other) {
        return this.startTime < other.endTime && this.endTime > other.startTime;
    }

    compareTo(other) {
        return this.startTime.localeCompare(other.startTime);
    }
}

// MeetingSession
export class MeetingSession {
    constructor(dayOfWeek, timeSlot, room, activityType) {
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.room = room;
        this.activityType = activityType;
    }
}

// CourseOffering
export class CourseOffering {
    constructor(crn, course, deliveryMode, meetingSessions = [], instructors = []) {
        this.crn = crn;
        this.course = course;
        this.deliveryMode = deliveryMode;
        this.meetingSessions = meetingSessions;
        this.instructors = instructors;
    }

    getSessionsForDay(dayOfWeek) {
        return this.meetingSessions.filter(session => session.dayOfWeek === dayOfWeek);
    }
}

// ItineraryEntry
export class ItineraryEntry {
    constructor(courseOffering, meetingSession) {
        this.courseOffering = courseOffering;
        this.meetingSession = meetingSession;
    }
}

// DailyItinerary
export class DailyItinerary {
    constructor(dayOfWeek, entries = []) {
        this.dayOfWeek = dayOfWeek;
        this.entries = entries.sort((a, b) =>
            a.meetingSession.timeSlot.compareTo(b.meetingSession.timeSlot)
        );
    }

    getOrderedEntries() {
        return this.entries;
    }
}

// TermSchedule
export class TermSchedule {
    constructor(offerings = []) {
        this.offerings = offerings;
        this.crnMap = new Map();
        offerings.forEach(offering => {
            this.crnMap.set(offering.crn, offering);
        });
    }

    findByCrn(crn) {
        return this.crnMap.get(crn);
    }

    findAllByCrns(crns) {
        return crns.map(crn => this.findByCrn(crn)).filter(o => o !== undefined);
    }
}

// RouteSegment
export class RouteSegment {
    constructor(fromBuilding, toBuilding, distance) {
        this.fromBuilding = fromBuilding;
        this.toBuilding = toBuilding;
        this.distance = distance; // in meters
    }
}

// RoutePath
export class RoutePath {
    constructor(buildings = [], segments = []) {
        this.buildings = buildings;
        this.segments = segments;
    }

    getTotalDistance() {
        return this.segments.reduce((sum, seg) => sum + seg.distance, 0);
    }

    isEmpty() {
        return this.buildings.length === 0;
    }
}

// RouteVisualizationModel
export class RouteVisualizationModel {
    constructor(courses, routePath, summaryLines) {
        this.courses = courses; // Array of CourseOffering
        this.routePath = routePath;
        this.summaryLines = summaryLines; // Array of strings
    }
}
