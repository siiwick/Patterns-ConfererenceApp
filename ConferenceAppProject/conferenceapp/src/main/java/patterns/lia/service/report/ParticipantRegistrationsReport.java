package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 4: every conference one participant registered for
public class ParticipantRegistrationsReport implements ReportStrategy {

    private final ReportDao reportDao;

    public ParticipantRegistrationsReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.participantRegistrations";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.PARTICIPANT;
    }

    @Override
    public ReportTable generate(Long userId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findRegistrationsOfParticipant(userId).stream()
                .map(r -> List.of(r.getTitle(), lang.formatDateTime(r.getStart()), lang.formatDateTime(r.getEnd()),
                        lang.get("status." + r.getStatus().name()),
                        r.getWaitlistPosition() == null ? "" : String.valueOf(r.getWaitlistPosition())))
                .toList();
        return new ReportTable(columns("column.conference", "column.start", "column.end", "column.status", "column.position"), rows);
    }
}
