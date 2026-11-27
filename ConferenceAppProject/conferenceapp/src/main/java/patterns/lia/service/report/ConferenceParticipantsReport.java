package patterns.lia.service.report;

import java.util.List;

import patterns.lia.dao.ReportDao;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.report.ReportTable;
import patterns.lia.utilities.LanguageManager;

//report 2: confirmed participants of one conference
public class ConferenceParticipantsReport implements ReportStrategy {

    private final ReportDao reportDao;

    public ConferenceParticipantsReport(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public String getNameKey() {
        return "report.conferenceParticipants";
    }

    @Override
    public ReportInput getInput() {
        return ReportInput.CONFERENCE;
    }

    @Override
    public ReportTable generate(Long conferenceId) {
        LanguageManager lang = LanguageManager.get();
        List<List<String>> rows = reportDao.findConfirmedParticipants(conferenceId).stream()
                .map(p -> List.of(p.getFullName(), p.getEmail(), String.valueOf(p.getAge()), lang.formatDateTime(p.getRegisteredAt())))
                .toList();
        return new ReportTable(columns("column.name", "column.email", "column.age", "column.registeredAt"), rows);
    }
}
