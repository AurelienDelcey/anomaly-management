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
import domain.valueobject.Description;
import domain.valueobject.ImpactedQuantity;
import domain.valueobject.Machine;
import domain.valueobject.ProductionOrder;
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
	
	public CommandResult createAnomaly (String description, String sector, int quantity, int productionOrder, String machine) {
		int savingTry = 5;
		log.debug("CreateAnomaly requested - actorId={}",actor.id()); 
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		Description newDescription = new Description(description);
		ImpactedQuantity impactedQuantity = new ImpactedQuantity(quantity);
		ProductionOrder newProductionOrder = new ProductionOrder(productionOrder);
		Sector newSector;
		Machine newMachine;
		try {
		    newSector = Sector.valueOf(sector);
		} catch (IllegalArgumentException e) {
		    return new CommandFailure("Invalid sector: " + sector);
		}
		try {
			newMachine = Machine.valueOf(machine);
		}catch(IllegalArgumentException e) {
			 return new CommandFailure("Invalid Machine: " + machine);
		}
		
		while(savingTry > 0) {
			savingTry--;
			try {
				BusinessId businessId = businessIdGenerator.getBusinessId();
				Anomaly anomaly = new Anomaly(businessId, newDescription, newSector, impactedQuantity, newProductionOrder, newMachine, trace);
				repository.save(anomaly);	
				log.info("CreateAnomaly succeeded - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
				return new CommandSuccess(anomaly.getId());
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
		Description newDescription = new Description(description);
		return handleCommand("AttachDescription", anomalyId, (e)->e.attachDescription(newDescription));
	}
	
	public CommandResult attachProductionOrder (UUID anomalyId, int productionOrder) {
		ProductionOrder newProductionOrder = new ProductionOrder(productionOrder);
		return handleCommand("AttachProductionOrder", anomalyId, (e)->e.attachProductionOrder(newProductionOrder));
			
	}
	
	public CommandResult attachImpactedQuantity (UUID anomalyId, int quantity) {
		ImpactedQuantity newQuantity = new ImpactedQuantity(quantity);
		return handleCommand("AttachImpactedQuantity", anomalyId, (e)->e.attachImpactedQuantity(newQuantity));
	}
	
	public CommandResult attachMachine (UUID anomalyId, String machine) {
		Machine newMachine = Machine.valueOf(machine);//TODO reintorduir les enums metier directement dans l'ui
		return handleCommand("AttachMachine", anomalyId, (e)->e.attachMachine(newMachine));
	}
	
	public CommandResult attachSector (UUID anomalyId, String sector) {
		Sector newSector = Sector.valueOf(sector);//TODO reintorduir les enums metier directement dans l'ui
		return handleCommand("AttachSector", anomalyId, (e)->e.attachSector(newSector));
	}
	
	public CommandResult attachCorrectiveAction (UUID anomalyId, String docId) {
		return handleCommand("AttachCorrectiveAction", anomalyId, (e)->e.attachCorrectiveAction(docId));
	}
	
	public CommandResult attachQualityDecision (UUID anomalyId, QualityDecision decision) {
		return handleCommand("AttachQualityDecision", anomalyId, (e)->e.attachQualityDecision(decision));
	}
	
	public CommandResult transitionToCorrected (UUID anomalyId) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		return handleCommand("TransitionToCorrected", anomalyId, (e)->e.transitionToCorrected(trace));
	}
	
	public CommandResult attachEvidence (UUID anomalyId, String docId) {
		return handleCommand("AttachEvidence", anomalyId, (e)->e.attachEvidence(docId));
	}
	
	public CommandResult transitionToResolved (UUID anomalyId) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		return handleCommand("TransitionToResolved", anomalyId, (e)->e.transitionToResolved(trace));
	}
	
	public CommandResult transitionToArchived (UUID anomalyId) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		return handleCommand("TransitionToArchived", anomalyId, (e)->e.transitionToArchived(trace));
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
				Anomaly prolongation = createProlongation(archivedAnomaly.getQualityDecision(), businessId, context, archivedAnomaly.getDescription(), archivedAnomaly.getSector(), 
						archivedAnomaly.getQuantity(), archivedAnomaly.getProductionOrder(), archivedAnomaly.getMachine());
				Anomaly anomalyWithProlongationId = archivedAnomaly.linkProlongation(prolongation.getId());
				repository.saveAtomic(anomalyWithProlongationId, prolongation);
				log.info("TransitionToArchivedWithProlongation succeeded - anomalyId={}, actorId={}", anomalyId, actor.id());
				return new CommandSuccess(prolongation.getId());
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
	
	private Anomaly createProlongation (QualityDecision qualityDecision, BusinessId businessId, ProlongationContext prolongationContext, Description description, 
			Sector sector, ImpactedQuantity quantity, ProductionOrder productionOrder, Machine machine) {
		EventTrace trace = new EventTrace(actor.id(), Instant.now());
		Anomaly anomaly = new Anomaly(qualityDecision, businessId, description, sector, quantity, productionOrder, machine, trace, prolongationContext);
		log.debug("CreateProlongation - anomalyId={}, actorId={}", anomaly.getId(), actor.id());
		return anomaly;
	}
	
	private CommandResult handleCommand(String logLabel, UUID anomalyId, CommandHandler command) {
		try {
			log.debug("{} requested - anomalyId={}, actorId={}", logLabel, anomalyId, actor.id());
			Anomaly anomaly = repository.findById(anomalyId);
			Anomaly newAnomaly = command.execute(anomaly);
			repository.save(newAnomaly);
			log.info("{} succeeded - anomalyId={}, actorId={}", logLabel, anomalyId, actor.id());
			return new CommandSuccess(anomalyId);
		} catch (DomainException | IllegalArgumentException | TechnicalException e) {
			log.warn("{} failed - anomalyId={}, reason={}", logLabel, anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		} catch (AnomalyNotFoundException e) {
			log.warn("{} anomaly not found in command service- anomalyId={}, reason={}", logLabel, anomalyId, e.getMessage());
			return new CommandFailure(e.getMessage());
		}
	}
}
