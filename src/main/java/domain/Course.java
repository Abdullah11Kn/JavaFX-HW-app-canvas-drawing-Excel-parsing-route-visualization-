package domain;

public class Course {
    private final String code;
    private final String title;
    private final String department;

    /** Validates and stores the core catalog details for a course. */
    public Course(String code, String title, String department) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Course code is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title is required");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department is required");
        }
        this.code = code.trim();
        this.title = title.trim();
        this.department = department.trim();
    }

    /** Returns the catalog code (e.g., SWE 316). */
    public String getCode() {
        return code;
    }

    /** Returns the human-friendly course title. */
    public String getTitle() {
        return title;
    }

    /** Exposes the owning department acronym. */
    public String getDepartment() {
        return department;
    }

    @Override
    /** Formats the course for debugging or dropdowns. */
    public String toString() {
        return code + " - " + title;
    }
}
