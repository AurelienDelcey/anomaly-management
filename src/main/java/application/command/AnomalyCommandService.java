package application.command;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.BusinessIdGenerator;
import application.actor.Actor;
import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import domain.exception.DomainException;
import domain.traceability.EventTrace;
import domain.valueobject.BusinessId;
import domain.valueobject.ProlongationContext;
import domain.valueobject.QualityDecision;
import domain.valueobject.Sector;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.BusinessIdColisionException;
import infrastructure.exception.TechnicalException;

public class AnomalyCommandService {
	
	private static final Logger log = LoggerFactory.getLogger(AnomalyCommandService.class);
	private final AnomalyRepository repository;
	private final Actor actor;
	private final BusinessIdGenerator businessIdGenerator;
	
	public AnomalyCommandService(AnomalyRepository repository, Actor actor) {
		this.repository = repository;
		this.actor = actor;
		this.businessIdGenerator = new BusinessIdGenerator(repository);
	}
	
	public CommandResult createAnomaly (String description, Sector sector) {
		int savingTry = 5;
		log.debug("CreateAnomaly requested - actorId={}",actor.id()); 
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		
		while(savingTry > 0) {
			savingTry--;
			try {
				BusinessId businessId = businessIdGenerator.getBusinessId();
				Anomaly anomaly = new Anomaly(businessId, description, sector, trace);
				repository.save(anomaly);	
				log.info("CreateAnomaly succeeded - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
				return new CommandSuccess();
			} catch (BusinessIdColisionException e) {
				continue;
			} catch (TechnicalException | IllegalArgumentException e) {
				log.warn("CreateAnomaly failed - reason={}", e.getMessage());
				return new CommandFailure(e.getMessage());
			}
		}
		log.warn("CreateAnomaly failed - reason=Maximum aptempt of retry to allocate a businessId");
		return new CommandFailure("CreateAnomaly failed: Maximum aptempt of retry to allocate a businessId");
	}
	
	public CommandResult attachDescription (UUID anomalyId, String description) {
		try {
			log.debug("AttachDescription requested - anomalyId={}, actorId={}", anomalyId, actor.id()); 
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachDescription(description);
			repository.save(newAnomaly);
			log.info("AttachDescription succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | TechnicalException | IllegalArgumentException e) {
			log.warn("AttachDescription failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachDescription anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachSector (UUID anomalyId, Sector sector) {
		try {
			log.debug("AttachSector requested - anomalyId={}, actorId={}", anomalyId, actor.id()); 
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachSector(sector);
			repository.save(newAnomaly);
			log.info("AttachSector succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | TechnicalException | IllegalArgumentException e) {
			log.warn("AttachSector failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachSector anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachCorrectiveAction (UUID anomalyId, String docId) {
		try {
			log.debug("AttachCorrectiveAction requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachCorrectiveAction(docId);
			repository.save(newAnomaly);
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
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachQualityDecision(decision);
			repository.save(newAnomaly);
			log.info("AttachQualityDecision succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | TechnicalException e) {
			log.warn("AttachQualityDecision failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachQualityDecision anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToCorrected (UUID anomalyId) {
		try {
			log.debug("TransitionToCorrected requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToCorrected(trace);
			repository.save(newAnomaly);
			log.info("TransitionToCorrected succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToCorrected failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("TransitionToCorrected anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult attachEvidence (UUID anomalyId, String docId) {
		try {
			log.debug("AttachEvidence requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.attachEvidence(docId);
			repository.save(newAnomaly);
			log.info("AttachEvidence succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("AttachEvidence failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("AttachEvidence anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToResolved (UUID anomalyId) {
		try {
			log.debug("TransitionToResolved requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToResolved(trace);
			repository.save(newAnomaly);
			log.info("TransitionToResolved succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToResolved failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("TransitionToResolved anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchived (UUID anomalyId) {
		try {
			log.debug("TransitionToArchived requested - anomalyId={}, actorId={}", anomalyId, actor.id());
			EventTrace trace = new EventTrace(actor.id(), Instant.now());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = anomaly.transitionToArchived(trace);
			repository.save(newAnomaly);
			log.info("TransitionToArchived succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
			return new CommandSuccess();
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("TransitionToArchived failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("TransitionToArchived anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
	
	public CommandResult transitionToArchivedWithProlongation (UUID anomalyId, String comment) {
		int savingTry = 5;
		
		while(savingTry > 0) {
		savingTry--;	
			try {
				log.debug("TransitionToArchivedWithProlongation requested - anomalyId={}, actorId={}", anomalyId, actor.id());
				EventTrace trace = new EventTrace(actor.id(), Instant.now());
				Anomaly anomaly = repository.findById(anomalyId);
				Anomaly archivedAnomaly = anomaly.transitionToArchived(trace);
				ProlongationContext context = new ProlongationContext(archivedAnomaly.getId(), comment);
				BusinessId businessId = businessIdGenerator.getBusinessId();
				Anomaly prolongation = createProlongation(businessId, context, archivedAnomaly.getDescription().description(), archivedAnomaly.getSector());
				Anomaly anomalyWithProlongationId = archivedAnomaly.linkProlongation(prolongation.getId());
				repository.saveAtomic(anomalyWithProlongationId, prolongation);
				log.info("TransitionToArchivedWithProlongation succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
				return new CommandSuccess();
			} catch (BusinessIdColisionException e) {
				continue;
			}catch (DomainException | IllegalArgumentException | TechnicalException e) {
				log.warn("TransitionToArchivedWithProlongation failed - anomalyId={}, reason={}", anomalyId, e.getMessage());
				return new CommandFailure(e.getMessage());
			} catch (AnomalyNotFoundException e) {
				log.warn("TransitionToArchivedWithProlongation anomaly not found in command service- anomalyId={}, reason={}", anomalyId, e.getMessage());
				return new CommandFailure(e.getMessage());
			}
		}
		log.warn("transitionToArchivedWithProlongation failed - reason=Maximum aptempt of retry to allocate a businessId");
		return new CommandFailure("transitionToArchivedWithProlongation failed: Maximum aptempt of retry to allocate a businessId");
	}
	
	private Anomaly createProlongation (BusinessId businessId, ProlongationContext prolongationContext, String description, Sector sector) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		Anomaly anomaly = new Anomaly(businessId, description, sector, trace, prolongationContext);
		log.debug("CreateProlongation - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
		return anomaly;
	}
}
