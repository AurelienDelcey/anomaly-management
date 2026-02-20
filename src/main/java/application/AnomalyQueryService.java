package application;

import java.util.List;
import java.util.UUID;

import domain.anomaly.Anomaly;

public class AnomalyQueryService {
	
	private final Repo repo;

	public AnomalyQueryService(Repo repo) {
		this.repo = repo;
	}
	
	public AnomalyDto getById(UUID id) {
		return repo.findByIdOptional(id)
				.map(AnomalyDtoMapper::mapToDto)
				.orElse(null);
	}
	
	public List<AnomalyDto> getAll(){
		List<Anomaly> anomalyList = repo.findAll();
		return anomalyList.stream()
				.map(AnomalyDtoMapper::mapToDto)
				.toList();
	}
}
