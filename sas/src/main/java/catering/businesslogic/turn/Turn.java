package catering.businesslogic.turn;

import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Represents a turn, which includes details such as date, start and end times, location, and associated service information.
 */
public class Turn {
    public enum Kind { KITCHEN, SERVICE }

    private int id;
    private Date date;
    private Time start;
    private Time end;
    private String location;
    private ServiceInfo service;
    private Kind kind = Kind.KITCHEN;
    private int ownerId;
    private int groupId;
    private int seriesId;
    private LocalDateTime availabilityDeadline;
    private final List<User> assignedUsers = new ArrayList<>();

    public Turn() {
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */

    /**
     * Returns a string representation of the turn, including its date, start and end times, and location.
     *
     * @return A string describing the turn.
     */
    @Override
    public String toString() {
        return "Date: " + date +
                ", Start: " + start +
                ", End: " + end +
                ", Location: " + location;
    }

    /**
     * Assigns a user to the turn.
     *
     * @param user The user to be assigned to the turn.
     */
    public void assign(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("A persisted user is required");
        }
        if (assignedUsers.stream().noneMatch(assigned -> assigned.getId() == user.getId())) {
            assignedUsers.add(user);
        }
    }

    public List<User> getAssignedUsers() {
        return Collections.unmodifiableList(assignedUsers);
    }

    public boolean overlaps(Turn other) {
        if (other == null) {
            throw new IllegalArgumentException("Turn is required");
        }
        return startDateTime().isBefore(other.endDateTime())
                && other.startDateTime().isBefore(endDateTime());
    }

    public long durationMinutes() {
        return Duration.between(startDateTime(), endDateTime()).toMinutes();
    }

    public LocalDateTime startsAt() {
        return startDateTime();
    }

    private LocalDateTime startDateTime() {
        if (date == null || start == null || end == null || start.equals(end)) {
            throw new IllegalArgumentException("Turn must have a date and a non-zero time range");
        }
        LocalDate day = new java.sql.Date(date.getTime()).toLocalDate();
        return LocalDateTime.of(day, start.toLocalTime());
    }

    private LocalDateTime endDateTime() {
        LocalDateTime startDateTime = startDateTime();
        LocalDateTime endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), end.toLocalTime());
        return endDateTime.isAfter(startDateTime) ? endDateTime : endDateTime.plusDays(1);
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStart() {
        return start;
    }

    public void setStart(Time start) {
        this.start = start;
    }

    public Time getEnd() {
        return end;
    }

    public void setEnd(Time end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ServiceInfo getService() {
        return service;
    }

    public void setService(ServiceInfo service) {
        this.service = service;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = java.util.Objects.requireNonNull(kind);
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId = seriesId;
    }

    public LocalDateTime getAvailabilityDeadline() {
        return availabilityDeadline;
    }

    public void setAvailabilityDeadline(LocalDateTime availabilityDeadline) {
        this.availabilityDeadline = availabilityDeadline;
    }

}
