package  org.example.hospital.dto;

public class SummaryItem {

    private String label;
    private long value;

    public SummaryItem(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }
}
