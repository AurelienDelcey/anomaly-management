package application;

import java.time.Instant;
import java.util.UUID;

import domain.anomaly.Anomaly;
import domain.exception.IllegalAttachment;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.IllegalTransition;
import domain.traceability.EventTrace;
import domain.valueobject.Description;

public class AnomalyCommandService {
	
	private final Repo repo;
	private final Actor actor;
	
	public AnomalyCommandService(Repo repo, Actor actor) {
		this.repo = repo;
		this.actor = actor;
	}
	
	public CommandResult createAnomaly (String description) {
		try {
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = new Anomaly(description, trace);
			repo.save(anomaly);
			return new CommandSucces();
			} catch (IllegalArgumentException e) {
				return new CommandFailure(e.getMessage());
			}
	}
	
	public CommandResult attachCorrectiveAction (UUID anomalyId, String docId) {
		try {
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachCorrectiveAction(docId);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalAttachment | IllegalArgumentException e) {
			return new CommandFailure(e.getMessage());
		} 
	}
	
	public CommandResult attachQualityDecision (UUID anomalyId, QualityDecision decision) {//String ou int Decision puis switch mapping??
		try {
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachQualityDecision(decision);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalAttachment e) {
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToCorrected (UUID anomalyId) {
		try {
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToCorrected(trace);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalTransition | IllegalTraceErasureTentative e) {
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachProvingDocument (UUID anomalyId, String docId) {
		try {
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachProvingDocument(docId);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalAttachment | IllegalArgumentException e) {
			return new CommandFailure(e.getMessage());
		} 
	}
	
	public CommandResult transitionToResolved (UUID anomalyId) {
		try {
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToResolved(trace);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalTransition | IllegalTraceErasureTentative e) {
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchived (UUID anomalyId) {
		try {
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToArchived(trace);
			repo.save(newAnomaly);
			return new CommandSucces();
		} catch (IllegalTransition | IllegalTraceErasureTentative e) {
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchivedWithProlongation (UUID anomalyId) {
		try {
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly archivedAnomaly = anomaly.transitionToArchived(trace);
			Anomaly prolongation = createProlongation(archivedAnomaly.getId(), archivedAnomaly.getDescription().description());
			Anomaly anomalyWithProlongationId = archivedAnomaly.attachProlongationId(prolongation.getId());
			repo.save(anomalyWithProlongationId);
			repo.save(prolongation);
			return new CommandSucces();
		} catch (IllegalTransition | IllegalTraceErasureTentative | IllegalAttachment e) {
			return new CommandFailure(e.getMessage());
		}
	}
	
	private Anomaly createProlongation (UUID parentId, String description) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		Anomaly anomaly = new Anomaly(description, trace, parentId);
		return anomaly;
	}
}
