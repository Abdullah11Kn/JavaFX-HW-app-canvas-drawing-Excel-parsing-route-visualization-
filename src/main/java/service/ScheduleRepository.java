package service;

import domain.TermSchedule;

public interface ScheduleRepository {
    /** Loads or returns the cached term schedule backing the application. */
    TermSchedule getTermSchedule();
}
