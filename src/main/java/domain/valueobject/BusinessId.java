package domain.valueobject;

import java.time.Year;

public record BusinessId(int year, int sequence) {

    public BusinessId {
        if (year < 2026 || year > Year.now().getValue()) {
            throw new IllegalArgumentException("Invalid year in business ID.");
        }

        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be positive.");
        }
    }

    @Override
    public String toString() {
        return "ANO-%d-%04d".formatted(year, sequence);
    }
}
