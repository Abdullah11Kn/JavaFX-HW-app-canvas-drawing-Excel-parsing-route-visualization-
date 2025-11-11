package domain;

public class Instructor {
    private final String name;
    private final String email;

    /** Normalizes faculty contact details from the spreadsheet. */
    public Instructor(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Instructor name is required");
        }
        this.name = name.trim();
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    /** Returns the instructor's display name. */
    public String getName() {
        return name;
    }

    /** Returns the email if provided, otherwise null. */
    public String getEmail() {
        return email;
    }

    @Override
    /** Outputs a friendly string combining name and email when available. */
    public String toString() {
        return email == null ? name : name + " (" + email + ")";
    }
}
