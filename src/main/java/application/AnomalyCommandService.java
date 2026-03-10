package application;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.anomaly.Anomaly;
import domain.exception.DomainException;
import domain.traceability.EventTrace;
import domain.valueobject.QualityDecision;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;

public class AnomalyCommandService {
	
	private static final Logger log = LoggerFactory.getLogger(AnomalyCommandService.class);
	private final Repo repo;
	private final Actor actor;
	
	public AnomalyCommandService(Repo repo, Actor actor) {
		this.repo = repo;
		this.actor = actor;
	}
	
	public CommandResult createAnomaly (String description) {
		try {
			log.debug("CreateAnomaly requested - actorId={}",actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = new Anomaly(description, trace);
			repo.save(anomaly);
			log.info("CreateAnomaly succeeded - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
			return new CommandSuccess();
			} catch (TechnicalException | IllegalArgumentException e) {
				log.warn("CreateAnomaly failed - reason={}", e.getMessage());
				return new CommandFailure(e.getMessage());
			}
	}
	
	public CommandResult attachCorrectiveAction (UUID anomalyId, String docId) {
		try {
			log.debug("AttachCorrectiveAction requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachCorrectiveAction(docId);
			repo.save(newAnomaly);
			log.info("AttachCorrectiveAction succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("AttachCorrectiveAction failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachQualityDecision (UUID anomalyId, QualityDecision decision) {
		try {
			log.debug("AttachQualityDecision requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachQualityDecision(decision);
			repo.save(newAnomaly);
			log.info("AttachQualityDecision succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | TechnicalException e) {
			log.warn("AttachQualityDecision failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToCorrected (UUID anomalyId) {
		try {
			log.debug("TransitionToCorrected requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToCorrected(trace);
			repo.save(newAnomaly);
			log.info("TransitionToCorrected succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToCorrected failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachEvidence (UUID anomalyId, String docId) {
		try {
			log.debug("AttachEvidence requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachEvidence(docId);
			repo.save(newAnomaly);
			log.info("AttachEvidence succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("AttachEvidence failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToResolved (UUID anomalyId) {
		try {
			log.debug("TransitionToResolved requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToResolved(trace);
			repo.save(newAnomaly);
			log.info("TransitionToResolved succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToResolved failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchived (UUID anomalyId) {
		try {
			log.debug("TransitionToArchived requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToArchived(trace);
			repo.save(newAnomaly);
			log.info("TransitionToArchived succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToArchived failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchivedWithProlongation (UUID anomalyId) {
		try {
			log.debug("TransitionToArchivedWithProlongation requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repo.findById(anomalyId);
			Anomaly archivedAnomaly = anomaly.transitionToArchived(trace);
			Anomaly prolongation = createProlongation(archivedAnomaly.getId(), archivedAnomaly.getDescription().description());
			Anomaly anomalyWithProlongationId = archivedAnomaly.attachProlongationId(prolongation.getId());
			repo.saveAtomic(anomalyWithProlongationId, prolongation);
			log.info("TransitionToArchivedWithProlongation succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToArchivedWithProlongation failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachCorrectiveAction anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	private Anomaly createProlongation (UUID parentId, String description) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		Anomaly anomaly = new Anomaly(description, trace, parentId);
		log.debug("CreateProlongation - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
		return anomaly;
	}
}
