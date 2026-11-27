package patterns.lia.model.report;

public class ReportOption {

    private final long id;
    private final String label;

    public ReportOption(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
