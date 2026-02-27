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
		return AnomalyDtoMapper.mapToDto(repo.findById(id));
	}
	
	public List<AnomalyDto> getAll(int page){
		List<Anomaly> anomalyList = repo.findAll(page);
		return anomalyList.stream()
				.map(AnomalyDtoMapper::mapToDto)
				.toList();
	}
}
