package catering.businesslogic.turn;

import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class TurnManager {
    public enum Frequency { DAILY, WEEKLY, MONTHLY }

    private final TurnRepository repository;
    private final Supplier<User> currentUser;
    private final Clock clock;

    public TurnManager(TurnRepository repository, Supplier<User> currentUser) {
        this(repository, currentUser, Clock.systemDefaultZone());
    }

    public TurnManager(TurnRepository repository, Supplier<User> currentUser, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.currentUser = Objects.requireNonNull(currentUser);
        this.clock = Objects.requireNonNull(clock);
    }

    public Turn createTurn(ServiceInfo service) {
        if (service == null) {
            throw new IllegalArgumentException("Service is required");
        }
        return createTurn(service, service.getDate(), service.getTimeStart(), service.getTimeEnd(), "SEDE");
    }

    public Turn createTurn(ServiceInfo service, Date date, Time start, Time end, String location) {
        User organiser = requireOrganiser();
        if (service == null || service.getId() <= 0 || date == null || start == null || end == null
                || start.equals(end) || location == null || location.isBlank()) {
            throw new IllegalArgumentException("A persisted service, schedule and location are required");
        }
        Turn turn = new Turn();
        turn.setKind(Turn.Kind.KITCHEN);
        turn.setService(service);
        turn.setDate(date);
        turn.setStart(start);
        turn.setEnd(end);
        turn.setLocation(location);
        turn.setOwnerId(organiser.getId());
        requireFuture(turn);
        return repository.save(turn);
    }

    public Turn createKitchenTurn(Date date, Time start, Time end, String location,
                                  LocalDateTime availabilityDeadline) {
        User organiser = requireOrganiser();
        Turn turn = new Turn();
        turn.setKind(Turn.Kind.KITCHEN);
        turn.setDate(date);
        turn.setStart(start);
        turn.setEnd(end);
        turn.setLocation(location);
        turn.setOwnerId(organiser.getId());
        turn.setAvailabilityDeadline(availabilityDeadline);
        validateNewTurn(turn);
        return repository.save(turn);
    }

    public Turn createServiceTurn(ServiceInfo service, int setupMinutes, int cleanupMinutes,
                                  String location, LocalDateTime availabilityDeadline) {
        User organiser = requireOrganiser();
        if (service == null || service.getId() <= 0 || service.getDate() == null
                || service.getTimeStart() == null || service.getTimeEnd() == null
                || setupMinutes < 0 || cleanupMinutes < 0) {
            throw new IllegalArgumentException("A persisted service and non-negative setup times are required");
        }
        LocalDateTime start = LocalDateTime.of(service.getDate().toLocalDate(), service.getTimeStart().toLocalTime())
                .minusMinutes(setupMinutes);
        LocalDateTime end = LocalDateTime.of(service.getDate().toLocalDate(), service.getTimeEnd().toLocalTime());
        if (!end.isAfter(start.plusMinutes(setupMinutes))) end = end.plusDays(1);
        end = end.plusMinutes(cleanupMinutes);
        if (end.toLocalDate().isAfter(start.toLocalDate().plusDays(1))) {
            throw new IllegalArgumentException("A service turn cannot span more than one night");
        }
        Turn turn = new Turn();
        turn.setKind(Turn.Kind.SERVICE);
        turn.setService(service);
        turn.setDate(java.sql.Date.valueOf(start.toLocalDate()));
        turn.setStart(Time.valueOf(start.toLocalTime()));
        turn.setEnd(Time.valueOf(end.toLocalTime()));
        turn.setLocation(location);
        turn.setOwnerId(organiser.getId());
        turn.setAvailabilityDeadline(availabilityDeadline);
        validateNewTurn(turn);
        return repository.save(turn);
    }

    public List<Turn> createRecurringKitchenTurns(Date firstDate, Time start, Time end,
                                                   String location, Frequency frequency, int occurrences) {
        User organiser = requireOrganiser();
        if (firstDate == null || frequency == null || occurrences < 2 || occurrences > 366) {
            throw new IllegalArgumentException("A bounded recurrence needs at least two turns");
        }
        List<Turn> turns = new ArrayList<>();
        LocalDate date = new java.sql.Date(firstDate.getTime()).toLocalDate();
        for (int index = 0; index < occurrences; index++) {
            Turn turn = new Turn();
            turn.setKind(Turn.Kind.KITCHEN);
            turn.setDate(java.sql.Date.valueOf(date));
            turn.setStart(start);
            turn.setEnd(end);
            turn.setLocation(location);
            turn.setOwnerId(organiser.getId());
            validateNewTurn(turn);
            turns.add(turn);
            switch (frequency) {
                case DAILY: date = date.plusDays(1); break;
                case WEEKLY: date = date.plusWeeks(1); break;
                case MONTHLY: date = date.plusMonths(1); break;
                default: throw new IllegalArgumentException("Unsupported frequency");
            }
        }
        return repository.saveSeries(turns, frequency.name(), organiser.getId());
    }

    public int groupKitchenTurns(List<Turn> turns) {
        User organiser = requireOrganiser();
        if (turns == null || turns.size() < 2) throw new IllegalArgumentException("At least two turns are required");
        List<Integer> ids = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn == null || turn.getId() <= 0 || turn.getKind() != Turn.Kind.KITCHEN
                    || turn.getOwnerId() != organiser.getId() || turn.getGroupId() > 0) {
                throw new IllegalArgumentException("Only owned, ungrouped kitchen turns can be grouped");
            }
            requireFuture(turn);
            ids.add(turn.getId());
        }
        int groupId = repository.createGroup(ids);
        turns.forEach(turn -> turn.setGroupId(groupId));
        return groupId;
    }

    public List<Integer> groupKitchenTurnsRecurring(List<List<Turn>> occurrences) {
        User organiser = requireOrganiser();
        if (occurrences == null || occurrences.size() < 2) {
            throw new IllegalArgumentException("At least two recurring groups are required");
        }
        List<List<Integer>> ids = new ArrayList<>();
        for (List<Turn> occurrence : occurrences) {
            if (occurrence == null || occurrence.size() < 2) {
                throw new IllegalArgumentException("Each occurrence needs at least two turns");
            }
            List<Integer> group = new ArrayList<>();
            for (Turn turn : occurrence) {
                if (turn == null || turn.getId() <= 0 || turn.getKind() != Turn.Kind.KITCHEN
                        || turn.getOwnerId() != organiser.getId() || turn.getGroupId() > 0) {
                    throw new IllegalArgumentException("Only owned, ungrouped kitchen turns can be grouped");
                }
                requireFuture(turn);
                group.add(turn.getId());
            }
            ids.add(group);
        }
        List<Integer> groupIds = repository.createGroupSeries(ids, organiser.getId());
        for (int index = 0; index < occurrences.size(); index++) {
            int groupId = groupIds.get(index);
            occurrences.get(index).forEach(turn -> turn.setGroupId(groupId));
        }
        return groupIds;
    }

    public void dissolveRecurringGroups(int anyGroupId) {
        requireOrganiser();
        int seriesId = repository.findGroupSeriesId(anyGroupId);
        if (seriesId <= 0) throw new IllegalArgumentException("Recurring group series not found");
        repository.dissolveGroupSeries(seriesId);
    }

    public void dissolveGroup(int groupId) {
        requireOrganiser();
        if (groupId <= 0) throw new IllegalArgumentException("Group ID is required");
        List<Turn> turns = repository.findByGroup(groupId);
        if (turns.isEmpty()) throw new IllegalArgumentException("Group not found");
        for (Turn turn : turns) requireOwnedFutureTurn(turn);
        repository.dissolveGroup(groupId);
        turns.forEach(turn -> turn.setGroupId(0));
    }

    public void declareAvailability(Turn turn) {
        User user = requireStaffFor(turn);
        repository.setAvailability(turn.getId(), user.getId(), true);
    }

    public void withdrawAvailability(Turn turn) {
        User user = requireStaffFor(turn);
        repository.setAvailability(turn.getId(), user.getId(), false);
    }

    public void updateTurn(Turn turn) {
        requireOwnedFutureTurn(turn);
        validateNewTurn(turn);
        repository.updateSchedule(turn);
    }

    public int updateSeriesFrom(int seriesId, LocalDate effectiveDate, Time start, Time end,
                                String location) {
        if (seriesId <= 0 || effectiveDate == null || start == null || end == null
                || start.equals(end) || location == null || location.isBlank()) {
            throw new IllegalArgumentException("Series, effective date, schedule and location are required");
        }
        List<Turn> selected = new ArrayList<>();
        for (Turn turn : repository.findBySeries(seriesId)) {
            if (!new java.sql.Date(turn.getDate().getTime()).toLocalDate().isBefore(effectiveDate)) {
                requireOwnedFutureTurn(turn);
                turn.setStart(start);
                turn.setEnd(end);
                turn.setLocation(location);
                validateNewTurn(turn);
                selected.add(turn);
            }
        }
        if (selected.isEmpty()) throw new IllegalArgumentException("No editable turns match the series filter");
        repository.updateSchedules(selected);
        return selected.size();
    }

    public int deleteSeriesFrom(int seriesId, LocalDate effectiveDate) {
        if (seriesId <= 0 || effectiveDate == null) {
            throw new IllegalArgumentException("Series and effective date are required");
        }
        List<Integer> selected = new ArrayList<>();
        for (Turn turn : repository.findBySeries(seriesId)) {
            if (!new java.sql.Date(turn.getDate().getTime()).toLocalDate().isBefore(effectiveDate)) {
                requireOwnedFutureTurn(turn);
                if (repository.hasAvailability(turn.getId())) {
                    throw new IllegalArgumentException("Turn with declared availability cannot be deleted");
                }
                selected.add(turn.getId());
            }
        }
        if (selected.isEmpty()) throw new IllegalArgumentException("No editable turns match the series filter");
        return repository.deleteAll(selected);
    }

    public boolean deleteTurn(Turn turn) {
        requireOwnedFutureTurn(turn);
        if (repository.hasAvailability(turn.getId())) {
            throw new IllegalArgumentException("Turn with declared availability cannot be deleted");
        }
        return repository.delete(turn.getId());
    }

    public void addLocationToTurn(Turn turn, String location) {
        if (turn == null || turn.getId() <= 0 || location == null || location.isBlank()) {
            throw new IllegalArgumentException("Persisted turn and location are required");
        }
        String previous = turn.getLocation();
        turn.setLocation(location);
        try {
            updateTurn(turn);
        } catch (RuntimeException ex) {
            turn.setLocation(previous);
            throw ex;
        }
    }

    public void assignTurn(Turn turn, User user) {
        if (turn == null || turn.getId() <= 0 || user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Persisted turn and user are required");
        }
        User actor = currentUser.get();
        if (actor == null || turn.getKind() == Turn.Kind.KITCHEN && !actor.isChef()
                || turn.getKind() == Turn.Kind.SERVICE && !actor.isOrganiser()) {
            throw new IllegalArgumentException("Only the responsible chef or organiser may assign staff");
        }
        if (turn.getKind() == Turn.Kind.KITCHEN && !user.isCook()
                || turn.getKind() == Turn.Kind.SERVICE && !user.isCook() && !user.isService()) {
            throw new IllegalArgumentException("User cannot staff this turn type");
        }
        if (!repository.isAvailable(turn.getId(), user.getId())) {
            throw new IllegalArgumentException("Staff must declare availability before assignment");
        }
        for (Turn assigned : repository.findAssignedToUser(user.getId())) {
            if (assigned.getId() != turn.getId() && assigned.overlaps(turn)) {
                throw new IllegalArgumentException("User is already assigned to an overlapping turn");
            }
        }
        repository.assign(turn, user);
        turn.assign(user);
    }

    public List<EventInfo> getEvents() {
        return EventInfo.loadAllEventInfo();
    }

    public Turn findById(int id) {
        return repository.findById(id);
    }

    public List<Turn> findAll() {
        return repository.findAll();
    }

    public List<Turn> findByService(int serviceId) {
        return repository.findByService(serviceId);
    }

    public List<Turn> findBySeries(int seriesId) {
        return repository.findBySeries(seriesId);
    }

    public List<Turn> findByGroup(int groupId) {
        return repository.findByGroup(groupId);
    }

    private User requireOrganiser() {
        User user = currentUser.get();
        if (user == null || user.getId() <= 0 || !user.isOrganiser()) {
            throw new IllegalArgumentException("An authenticated organiser is required");
        }
        return user;
    }

    private User requireStaffFor(Turn turn) {
        User user = currentUser.get();
        if (turn == null || turn.getId() <= 0 || user == null || user.getId() <= 0
                || turn.getKind() == Turn.Kind.KITCHEN && !user.isCook()
                || turn.getKind() == Turn.Kind.SERVICE && !user.isCook() && !user.isService()) {
            throw new IllegalArgumentException("Authenticated staff for this turn type is required");
        }
        return user;
    }

    private void requireOwnedFutureTurn(Turn turn) {
        User organiser = requireOrganiser();
        if (turn == null || turn.getId() <= 0 || turn.getOwnerId() != organiser.getId()) {
            throw new IllegalArgumentException("Only the turn owner may change it");
        }
        requireFuture(turn);
    }

    private void validateNewTurn(Turn turn) {
        if (turn.getDate() == null || turn.getStart() == null || turn.getEnd() == null
                || turn.getStart().equals(turn.getEnd()) || turn.getLocation() == null
                || turn.getLocation().isBlank() || turn.getKind() == Turn.Kind.SERVICE
                && (turn.getService() == null || turn.getService().getId() <= 0)) {
            throw new IllegalArgumentException("Turn requires a valid schedule, location and service for service turns");
        }
        requireFuture(turn);
        if (turn.getAvailabilityDeadline() != null
                && !turn.getAvailabilityDeadline().isBefore(turn.startsAt())) {
            throw new IllegalArgumentException("Availability deadline must precede the turn");
        }
    }

    private void requireFuture(Turn turn) {
        if (!turn.startsAt().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("Only future turns can be changed");
        }
    }
}
