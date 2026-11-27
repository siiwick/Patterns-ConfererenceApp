package patterns.lia.service;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.enums.Role;
import patterns.lia.model.report.ReportOption;
import patterns.lia.model.report.ReportTable;
import patterns.lia.service.report.ConferenceOverviewReport;
import patterns.lia.service.report.ConferenceParticipantsReport;
import patterns.lia.service.report.ReportStrategy;
import patterns.lia.service.report.WaitingListReport;
import patterns.lia.utilities.UserSession;

/**
 * Strategy pattern: holds the reports and runs the one chosen.
 */
public class ReportService {

    private final ReportDao reportDao;
    private final List<ReportStrategy> reports;

    public ReportService(DatabaseManager db) {
        this.reportDao = new ReportDao(db);
        this.reports = List.of(
                new ConferenceOverviewReport(reportDao),
                new ConferenceParticipantsReport(reportDao),
                new WaitingListReport(reportDao));
    }

    //the reports in the order they show in the list
    public List<ReportStrategy> getReports() {
        UserSession.requireRole(Role.ADMIN);
        return reports;
    }

    //the conferences the admin can choose from
    public List<ReportOption> getOptions(ReportInput input) {
        UserSession.requireRole(Role.ADMIN);
        return switch (input) {
            case CONFERENCE ->
                reportDao.findConferenceOverview().stream()
                .map(c -> new ReportOption(c.getId(), c.getTitle()))
                .toList();
            case NONE ->
                List.of();
        };
    }

    //runs the chosen report
    public ReportTable generate(ReportStrategy report, Long selectedId) {
        UserSession.requireRole(Role.ADMIN);
        if (report.getInput() != ReportInput.NONE && selectedId == null) {
            throw new IllegalArgumentException("This report needs a selection first");
        }
        return report.generate(selectedId);
    }
}
