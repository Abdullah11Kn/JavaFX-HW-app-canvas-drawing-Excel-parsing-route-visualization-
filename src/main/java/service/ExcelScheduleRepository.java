package service;

import domain.*;
import infra.BuildingRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ExcelScheduleRepository implements ScheduleRepository {
    private final Path filePath;
    private final BuildingRegistry buildingRegistry;
    private final Map<String, Course> courseCache = new HashMap<>();
    private final Map<String, Instructor> instructorCache = new HashMap<>();
    private volatile TermSchedule cache;

    /** Convenience constructor that accepts a string file path. */
    public ExcelScheduleRepository(String filePath, BuildingRegistry buildingRegistry) {
        this(Path.of(Objects.requireNonNull(filePath, "filePath")), buildingRegistry);
    }

    /** Primary constructor storing the Excel path and shared building registry. */
    public ExcelScheduleRepository(Path filePath, BuildingRegistry buildingRegistry) {
        if (buildingRegistry == null) {
            throw new IllegalArgumentException("Building registry is required");
        }
        this.filePath = filePath;
        this.buildingRegistry = buildingRegistry;
    }

    @Override
    /** Returns the cached term schedule or loads it from disk on first access. */
    public TermSchedule getTermSchedule() {
        TermSchedule result = cache;
        if (result == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = new TermSchedule(loadOfferings());
                }
                result = cache;
            }
        }
        return result;
    }

    /** Reads the workbook and constructs course offerings with their sessions. */
    private Collection<CourseOffering> loadOfferings() {
        List<CourseOffering> offerings = new ArrayList<>();
        Map<String, CourseOffering> offeringsByCrn = new HashMap<>();

        try (InputStream input = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String crn = readStringCell(row, 1);
                if (crn == null || crn.isBlank()) {
                    continue;
                }

                String courseCode = readStringCell(row, 2);
                String department = readStringCell(row, 3);
                String section = readStringCell(row, 4);
                String title = readStringCell(row, 5);
                String modalityToken = readStringCell(row, 6);
                String daysToken = readStringCell(row, 7);
                LocalTime startTime = readTime(row.getCell(8));
                LocalTime endTime = readTime(row.getCell(9));
                String buildingCode = readStringCell(row, 10);
                String roomCode = readStringCell(row, 11);
                String instructorName = readStringCell(row, 12);

                Course course = getOrCreateCourse(courseCode, title, department);
                Instructor instructor = getOrCreateInstructor(instructorName);
                DeliveryMode deliveryMode = mapDeliveryMode(modalityToken);
                ActivityType activityType = mapActivityType(modalityToken);

                TimeSlot timeSlot = createTimeSlot(startTime, endTime);
                Room room = createRoom(buildingCode, roomCode);
                if (timeSlot == null || room == null) {
                    continue;
                }

                CourseOffering offering = offeringsByCrn.computeIfAbsent(crn, key -> {
                    CourseOffering created = new CourseOffering(crn, section, deliveryMode, course, instructor);
                    offerings.add(created);
                    return created;
                });
                offering.updateInstructorIfMissing(instructor);

                for (DayOfWeek day : parseDays(daysToken)) {
                    MeetingSession session = new MeetingSession(day, timeSlot, activityType, room);
                    offering.addSession(session);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, ex);
        }

        return offerings;
    }

    /** Creates a Room object from the building and room code cells. */
    private Room createRoom(String buildingCode, String roomCode) {
        if (buildingCode == null || buildingCode.isBlank()) {
            return null;
        }
        Building building = buildingRegistry.getOrCreate(buildingCode.trim());
        int floor = parseFloor(roomCode);
        String normalizedRoom = (roomCode == null || roomCode.isBlank()) ? "Unknown" : roomCode.trim();
        return new Room(normalizedRoom, floor, building);
    }

    /** Attempts to infer the floor number from the room string. */
    private int parseFloor(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return 0;
        }
        for (char ch : roomCode.toCharArray()) {
            if (Character.isDigit(ch)) {
                return Character.digit(ch, 10);
            }
        }
        return 0;
    }

    /** Builds a TimeSlot when both start/end values parse correctly. */
    private TimeSlot createTimeSlot(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return null;
        }
        try {
            return new TimeSlot(start, end);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Reuses or creates Course objects so duplicates share references. */
    /** Reuses or constructs a Course entity keyed by course code. */
    private Course getOrCreateCourse(String code, String title, String department) {
        String normalizedCode = (code == null || code.isBlank()) ? "UNKNOWN" : code.trim();
        return courseCache.computeIfAbsent(normalizedCode, key -> new Course(
                normalizedCode,
                title == null || title.isBlank() ? normalizedCode : title.trim(),
                department == null || department.isBlank() ? "N/A" : department.trim()
        ));
    }

    /** Reuses or constructs an Instructor entity keyed by name (when provided). */
    private Instructor getOrCreateInstructor(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.trim();
        return instructorCache.computeIfAbsent(normalized, key -> new Instructor(normalized, null));
    }

    /** Maps the modality token (LEC/LAB/INT) to the DeliveryMode enum. */
    private DeliveryMode mapDeliveryMode(String token) {
        if (token == null) {
            return DeliveryMode.OTHER;
        }
        return switch (token.trim().toUpperCase(Locale.ROOT)) {
            case "LEC", "LECT" -> DeliveryMode.LECTURE;
            case "LAB" -> DeliveryMode.LAB;
            case "COP", "INT" -> DeliveryMode.INTERNSHIP;
            default -> DeliveryMode.OTHER;
        };
    }

    /** Maps the modality token to the ActivityType used by meeting sessions. */
    private ActivityType mapActivityType(String token) {
        if (token == null) {
            return ActivityType.OTHER;
        }
        return switch (token.trim().toUpperCase(Locale.ROOT)) {
            case "LEC", "LECT" -> ActivityType.LECTURE;
            case "LAB" -> ActivityType.LAB;
            case "COP", "INT" -> ActivityType.INTERNSHIP;
            default -> ActivityType.OTHER;
        };
    }

    /** Expands a day token like "UT" into the corresponding DayOfWeek values. */
    private Iterable<DayOfWeek> parseDays(String token) {
        if (token == null) {
            return java.util.List.of();
        }
        java.util.List<DayOfWeek> days = new java.util.ArrayList<>();
        for (char ch : token.trim().toUpperCase(Locale.ROOT).toCharArray()) {
            DayOfWeek mapped = switch (ch) {
                case 'U' -> DayOfWeek.SUNDAY;
                case 'M' -> DayOfWeek.MONDAY;
                case 'T' -> DayOfWeek.TUESDAY;
                case 'W' -> DayOfWeek.WEDNESDAY;
                case 'R', 'H' -> DayOfWeek.THURSDAY;
                case 'F' -> DayOfWeek.FRIDAY;
                case 'S' -> DayOfWeek.SATURDAY;
                default -> null;
            };
            if (mapped != null) {
                days.add(mapped);
            }
        }
        return days;
    }

    /** Reads a cell as a trimmed string, handling numeric CRN values gracefully. */
    private String readStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            return value == null ? null : value.trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            long longValue = Math.round(numericValue);
            if (Math.abs(numericValue - longValue) < 0.0001) {
                return Long.toString(longValue);
            }
            return Double.toString(numericValue);
        }
        if (cell.getCellType() == CellType.BLANK) {
            return null;
        }
        return cell.toString().trim();
    }

    /** Interprets a time cell that may be formatted as Excel time or HHmm text. */
    private LocalTime readTime(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalTime();
            }
            double value = cell.getNumericCellValue();
            if (value >= 0 && value < 1) {
                long totalSeconds = Math.round(value * 24 * 60 * 60);
                int hours = (int) (totalSeconds / 3600);
                int minutes = (int) ((totalSeconds % 3600) / 60);
                return LocalTime.of(hours % 24, minutes % 60);
            }
            String formatted = String.format(Locale.ROOT, "%04d", (int) Math.round(value));
            return parseTimeString(formatted);
        }
        if (cell.getCellType() == CellType.STRING) {
            String raw = cell.getStringCellValue();
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String digits = raw.replace(":", "").trim();
            if (digits.length() == 3) {
                digits = "0" + digits;
            }
            return parseTimeString(digits);
        }
        return null;
    }

    /** Parses a four-character HHmm string into a LocalTime. */
    private LocalTime parseTimeString(String value) {
        if (value == null || value.length() != 4) {
            return null;
        }
        try {
            int hour = Integer.parseInt(value.substring(0, 2));
            int minute = Integer.parseInt(value.substring(2, 4));
            if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
                return null;
            }
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
