package application;

import java.time.Year;

import application.repository.AnomalyRepository;
import domain.valueobject.BusinessId;

public class BusinessIdGenerator {
	
	private final AnomalyRepository repository;
	
	public BusinessIdGenerator(AnomalyRepository repository) {
		this.repository = repository;
	}
	
	public BusinessId getBusinessId() {
		int year = Year.now().getValue();
		int lastMax = repository.getMaxSequenceByYear(year);
		return new BusinessId(year, lastMax + 1);
	}
}
