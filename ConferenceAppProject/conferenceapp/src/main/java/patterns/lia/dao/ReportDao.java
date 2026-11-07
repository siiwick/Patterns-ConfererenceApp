package patterns.lia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.model.enums.RegistrationStatus;
import patterns.lia.model.report.ConferenceRow;
import patterns.lia.model.report.ParticipantRow;
import patterns.lia.model.report.RegistrationRow;
import patterns.lia.utilities.DateTimeUtil;

/**
 * Read-only queries for administrative reports.
 */
public class ReportDao {

    private static final Logger LOG = Logger.getLogger(ReportDao.class.getName());

    private final DatabaseManager db;

    public ReportDao(DatabaseManager db) {
        this.db = db;
    }

    //report 1: Conference Overview
    public List<ConferenceRow> findConferenceOverview() {
        String sql = """
                SELECT c.conference_id, c.title, c.venue, c.start_datetime, c.end_datetime, c.capacity,
                       (SELECT COUNT(*) FROM registrations r
                        WHERE r.conference_id = c.conference_id AND r.status = 'CONFIRMED') AS confirmed
                FROM conferences c
                ORDER BY c.start_datetime, c.conference_id
                """;
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return readConferences(rs);
        } catch (SQLException e) {
            throw failure("conference overview", e);
        }
    }

    //report 2: Conference Participant Report
    public List<ParticipantRow> findConfirmedParticipants(long conferenceId) {
        String sql = """
                SELECT u.user_id, u.full_name, u.email, u.age, r.registered_at, r.waitlist_position
                FROM registrations r JOIN users u ON u.user_id = r.user_id
                WHERE r.conference_id = ? AND r.status = 'CONFIRMED'
                ORDER BY r.registered_at, r.registration_id
                """;
        return queryParticipants(sql, conferenceId, "conference participants");
    }

    //report 3: Waiting-List Report
    public List<ParticipantRow> findWaitingList(long conferenceId) {
        String sql = """
                SELECT u.user_id, u.full_name, u.email, u.age, r.registered_at, r.waitlist_position
                FROM registrations r JOIN users u ON u.user_id = r.user_id
                WHERE r.conference_id = ? AND r.status = 'WAITLISTED'
                ORDER BY r.waitlist_position, r.registration_id
                """;
        return queryParticipants(sql, conferenceId, "waiting list");
    }

    //report 4: Participant Registration Report
    public List<RegistrationRow> findRegistrationsOfParticipant(long userId) {
        String sql = """
                SELECT c.conference_id, c.title, c.start_datetime, c.end_datetime, r.status, r.waitlist_position
                FROM registrations r JOIN conferences c ON c.conference_id = r.conference_id
                WHERE r.user_id = ?
                ORDER BY c.start_datetime, c.conference_id
                """;
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegistrationRow> rows = new ArrayList<>();
                while (rs.next()) {
                    int position = rs.getInt("waitlist_position");
                    Integer waitlistPosition = rs.wasNull() ? null : position;
                    rows.add(new RegistrationRow(rs.getLong("conference_id"), rs.getString("title"),
                            DateTimeUtil.parse(rs.getString("start_datetime")),
                            DateTimeUtil.parse(rs.getString("end_datetime")),
                            RegistrationStatus.valueOf(rs.getString("status")), waitlistPosition));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw failure("participant registrations", e);
        }
    }

    //report 5: Conference Occupancy Report
    public List<ConferenceRow> findConferenceOccupancy() {
        String sql = """
                SELECT c.conference_id, c.title, c.venue, c.start_datetime, c.end_datetime, c.capacity,
                       (SELECT COUNT(*) FROM registrations r
                        WHERE r.conference_id = c.conference_id AND r.status = 'CONFIRMED') AS confirmed
                FROM conferences c
                ORDER BY confirmed * 1.0 / c.capacity DESC, c.title
                """;
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return readConferences(rs);
        } catch (SQLException e) {
            throw failure("conference occupancy", e);
        }
    }

    //report 6: Unregistered Conferences Report
    public List<ConferenceRow> findUnregisteredConferences() {
        String sql = """
                SELECT c.conference_id, c.title, c.venue, c.start_datetime, c.end_datetime, c.capacity, 0 AS confirmed
                FROM conferences c
                WHERE NOT EXISTS (SELECT 1 FROM registrations r WHERE r.conference_id = c.conference_id)
                ORDER BY c.start_datetime, c.conference_id
                """;
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return readConferences(rs);
        } catch (SQLException e) {
            throw failure("unregistered conferences", e);
        }
    }

    //helpers
    private List<ParticipantRow> queryParticipants(String sql, long conferenceId, String operation) {
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ParticipantRow> rows = new ArrayList<>();
                while (rs.next()) {
                    int position = rs.getInt("waitlist_position");
                    Integer waitlistPosition = rs.wasNull() ? null : position;
                    rows.add(new ParticipantRow(rs.getLong("user_id"), rs.getString("full_name"),
                            rs.getString("email"), rs.getInt("age"),
                            DateTimeUtil.parse(rs.getString("registered_at")), waitlistPosition));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw failure(operation, e);
        }
    }

    private static List<ConferenceRow> readConferences(ResultSet rs) throws SQLException {
        List<ConferenceRow> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new ConferenceRow(rs.getLong("conference_id"), rs.getString("title"),
                    rs.getString("venue"), DateTimeUtil.parse(rs.getString("start_datetime")),
                    DateTimeUtil.parse(rs.getString("end_datetime")), rs.getInt("capacity"),
                    rs.getInt("confirmed")));
        }
        return rows;
    }

    private static DataAccessException failure(String operation, SQLException e) {
        LOG.log(Level.SEVERE, "Database error while building report: " + operation, e);
        return new DataAccessException(e);
    }
}
