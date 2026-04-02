package application.command;

import domain.anomaly.Anomaly;
import domain.exception.DomainException;

@FunctionalInterface
public interface CommandHandler {
	Anomaly execute(Anomaly anomaly) throws DomainException;
}
