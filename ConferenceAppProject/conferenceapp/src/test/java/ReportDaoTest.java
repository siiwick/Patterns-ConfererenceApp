
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import patterns.lia.dao.ReportDao;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.User;
import patterns.lia.model.enums.RegistrationStatus;
import patterns.lia.model.enums.Role;
import patterns.lia.model.report.ConferenceRow;
import patterns.lia.model.report.ParticipantRow;
import patterns.lia.model.report.RegistrationRow;

class ReportDaoTest {

    private DatabaseManager db;
    private ReportDao reports;

    private long alice, bob, cara, dan;
    private long meeting, awards, prices;

    @BeforeEach
    void setUp() throws SQLException {
        db = TestDb.create();
        reports = new ReportDao(db);
        UserDao users = new UserDao(db);

        alice = users.create(new User("Alice", "alice@x.com", 30, Role.PARTICIPANT), "h").getId();
        bob = users.create(new User("Bob", "bob@x.com", 31, Role.PARTICIPANT), "h").getId();
        cara = users.create(new User("Cara", "cara@x.com", 32, Role.PARTICIPANT), "h").getId();
        dan = users.create(new User("Dan", "dan@x.com", 33, Role.PARTICIPANT), "h").getId();

        awards = addConference("Awards", 4, "2027-03-12T09:00:00", "2027-03-12T17:00:00", "Hall B");
        prices = addConference("Prices", 10, "2027-03-11T09:00:00", "2027-03-11T17:00:00", null);
        meeting = addConference("Meeting", 2, "2027-03-10T09:00:00", "2027-03-10T17:00:00", "Hall A");

        addRegistration(alice, meeting, "CONFIRMED", null, "2027-01-01T10:00:00");
        addRegistration(bob, meeting, "CONFIRMED", null, "2027-01-01T09:00:00");
        addRegistration(cara, meeting, "WAITLISTED", 2, "2027-01-02T10:00:00");
        addRegistration(dan, meeting, "WAITLISTED", 1, "2027-01-03T10:00:00");
        addRegistration(alice, awards, "CONFIRMED", null, "2027-01-05T10:00:00");
    }

    private long addConference(String title, int capacity, String start, String end, String venue) throws SQLException {
        String sql = "INSERT INTO conferences (title, capacity, start_datetime, end_datetime, venue) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setInt(2, capacity);
            ps.setString(3, start);
            ps.setString(4, end);
            ps.setString(5, venue);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void addRegistration(long userId, long conferenceId, String status, Integer position, String registeredAt) throws SQLException {
        String sql = "INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registered_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, conferenceId);
            ps.setString(3, status);
            ps.setObject(4, position);
            ps.setString(5, registeredAt);
            ps.executeUpdate();
        }
    }

    //report 1
    @Test
    void testOrderedByStartDate() {
        List<ConferenceRow> rows = reports.findConferenceOverview();

        assertEquals(List.of(meeting, prices, awards), rows.stream().map(ConferenceRow::getId).toList());
    }

    @Test
    void testOnlyConfirmedRegistrations() {
        List<ConferenceRow> rows = reports.findConferenceOverview();

        assertEquals(List.of(2, 0, 1), rows.stream().map(ConferenceRow::getConfirmed).toList());
    }

    @Test
    void testAvailableSeatsAndDetails() {
        ConferenceRow a = reports.findConferenceOverview().get(0);

        assertEquals("Meeting", a.getTitle());
        assertEquals("Hall A", a.getVenue());
        assertEquals(2, a.getCapacity());
        assertEquals(0, a.getAvailableSeats());
        assertEquals(LocalDateTime.of(2027, 3, 10, 9, 0), a.getStart());
        assertEquals(LocalDateTime.of(2027, 3, 10, 17, 0), a.getEnd());
    }

    @Test
    void testNullVenue() {
        ConferenceRow g = reports.findConferenceOverview().get(1);
        assertNull(g.getVenue());
    }

    //report 2
    @Test
    void testIncludedParticipantDetails() {
        ParticipantRow row = reports.findConfirmedParticipants(awards).get(0);

        assertEquals(alice, row.getUserId());
        assertEquals("alice@x.com", row.getEmail());
        assertEquals(30, row.getAge());
    }

    @Test
    void testUnkownConference() {
        assertTrue(reports.findConfirmedParticipants(9999).isEmpty());
    }

    //report 3
    @Test
    void testQueuePositionOrdering() {
        List<ParticipantRow> rows = reports.findWaitingList(meeting);

        assertEquals(List.of("Dan", "Cara"), rows.stream().map(ParticipantRow::getFullName).toList());
        assertEquals(List.of(1, 2), rows.stream().map(ParticipantRow::getWaitlistPosition).toList());
    }

    @Test
    void testExcludesConfirmedUsers() {
        List<Long> ids = reports.findWaitingList(meeting).stream().map(ParticipantRow::getUserId).toList();

        assertTrue(!ids.contains(alice) && !ids.contains(bob));
    }

    @Test
    void testEmptyWaitingList() {
        assertTrue(reports.findWaitingList(awards).isEmpty());
    }

    //report 4
    @Test
    void testIncludesWaitListedRegistrations() {
        List<RegistrationRow> rows = reports.findRegistrationsOfParticipant(cara);

        assertEquals(1, rows.size());
        assertEquals(RegistrationStatus.WAITLISTED, rows.get(0).getStatus());
        assertEquals(2, rows.get(0).getWaitlistPosition());
    }

    @Test
    void testUnknownUser() {
        assertTrue(reports.findRegistrationsOfParticipant(9999).isEmpty());
    }

    //report 5
    @Test
    void testRates() {
        List<ConferenceRow> rows = reports.findConferenceOccupancy();

        assertEquals(1.0, rows.get(0).getOccupancyRate());
        assertEquals(0.25, rows.get(1).getOccupancyRate());
        assertEquals(0.0, rows.get(2).getOccupancyRate());
    }

    //report 6
    @Test
    void testEmptyConference() {
        List<ConferenceRow> rows = reports.findUnregisteredConferences();

        assertEquals(List.of(prices), rows.stream().map(ConferenceRow::getId).toList());
        assertEquals(0, rows.get(0).getConfirmed());
    }

    @Test
    void testNoEmptyConference() throws SQLException {
        addRegistration(bob, prices, "CONFIRMED", null, "2027-02-01T10:00:00");

        assertTrue(reports.findUnregisteredConferences().isEmpty());
    }
}
