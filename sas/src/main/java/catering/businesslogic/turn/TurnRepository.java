package catering.businesslogic.turn;

import catering.businesslogic.user.User;
import java.util.List;

public interface TurnRepository {
    Turn save(Turn turn);
    Turn findById(int id);
    List<Turn> findAll();
    List<Turn> findByService(int serviceId);
    List<Turn> findAssignedToUser(int userId);
    List<Turn> findBySeries(int seriesId);
    List<Turn> findByGroup(int groupId);
    List<Turn> saveSeries(List<Turn> turns, String frequency, int ownerId);
    int createGroup(List<Integer> turnIds);
    default List<Integer> createGroupSeries(List<List<Integer>> groups, int ownerId) {
        java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
        for (List<Integer> group : groups) result.add(createGroup(group));
        return result;
    }
    void dissolveGroup(int groupId);
    default void dissolveGroupSeries(int seriesId) {
        throw new UnsupportedOperationException("Recurring groups are not supported");
    }
    default int findGroupSeriesId(int groupId) { return 0; }
    void updateSchedule(Turn turn);
    default void updateSchedules(List<Turn> turns) {
        turns.forEach(this::updateSchedule);
    }
    void setAvailability(int turnId, int userId, boolean available);
    boolean hasAvailability(int turnId);
    boolean isAvailable(int turnId, int userId);
    boolean delete(int id);
    default int deleteAll(List<Integer> ids) {
        int deleted = 0;
        for (int id : ids) if (delete(id)) deleted++;
        return deleted;
    }
    void updateLocation(Turn turn);
    void assign(Turn turn, User user);
}
