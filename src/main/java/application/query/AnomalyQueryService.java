package application.query;

import java.util.List;
import java.util.UUID;

import application.dto.AnomalyDto;
import application.dto.AnomalyDtoMapper;
import application.repository.Repository;
import domain.anomaly.Anomaly;
import domain.exception.InconsistentAnomalyStateException;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;

public class AnomalyQueryService {
	
	private final Repository repo;

	public AnomalyQueryService(Repository repo) {
		this.repo = repo;
	}
	
	public QueryResult<AnomalyDto> getById(UUID id)throws InconsistentAnomalyStateException {
		try {
			AnomalyDto result = AnomalyDtoMapper.mapToDto(repo.findById(id));
			return new QuerySuccess<AnomalyDto>(result);
		}catch(AnomalyNotFoundException e) {
			return new QueryNotFound<AnomalyDto>();
		}catch(TechnicalException e) {
			return new QueryFailure<AnomalyDto>(e.getMessage());
		}
	}
	
	public QueryResult<List<AnomalyDto>> getAll(int page) throws InconsistentAnomalyStateException{
		try {
			List<Anomaly> anomalyList = repo.findAll(page);
			List<AnomalyDto> anomalyListDto = anomalyList.stream()
					.map(AnomalyDtoMapper::mapToDto)
					.toList();
			return new QuerySuccess<List<AnomalyDto>>(anomalyListDto);
		}catch(TechnicalException e) {
			return new QueryFailure<List<AnomalyDto>>(e.getMessage());
		}
	}
}
