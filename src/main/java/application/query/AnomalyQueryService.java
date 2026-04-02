package application.query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.dto.AnomalyDto;
import application.dto.AnomalyDtoMapper;
import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;

public class AnomalyQueryService {
	
	private final AnomalyRepository repository;

	public AnomalyQueryService(AnomalyRepository repository) {
		this.repository = repository;
	}
	
	public QueryResult<AnomalyDto> findById(UUID id) {
		try {
			AnomalyDto result = AnomalyDtoMapper.mapToDto(repository.findById(id));
			return new QuerySuccess<AnomalyDto>(result);
		}catch(AnomalyNotFoundException e) {
			return new QueryNotFound<AnomalyDto>();
		}catch(TechnicalException e) {
			return new QueryFailure<AnomalyDto>(e.getMessage());
		}
	}
	
	public QueryResult<List<AnomalyDto>> findPage(int page) {
		try {
			List<Anomaly> anomalies = repository.findAll(page);
			List<AnomalyDto> anomalyDtos = anomalies.stream()
					.map(AnomalyDtoMapper::mapToDto)
					.toList();
			return new QuerySuccess<List<AnomalyDto>>(anomalyDtos);
		}catch(TechnicalException e) {
			return new QueryFailure<List<AnomalyDto>>(e.getMessage());
		}
	}
	
	public QueryResult<List<AnomalyDto>> findHistory(UUID originId){
		try {
			Anomaly originAnomaly = repository.findById(originId);
			List<Anomaly> parentList = new ArrayList<>();
			List<Anomaly> childList = new ArrayList<>();
			List<Anomaly> resultList = new ArrayList<>();
			
			Anomaly currentAnomaly = originAnomaly;
			if(currentAnomaly.getProlongationContext() != null && currentAnomaly.getProlongationContext().parentId() != null) {
				while(currentAnomaly.getProlongationContext() != null && currentAnomaly.getProlongationContext().parentId() != null) {
					currentAnomaly = repository.findById(currentAnomaly.getProlongationContext().parentId());
					parentList.add(currentAnomaly);
				}
			}
			
			currentAnomaly = originAnomaly;
			if(currentAnomaly.getChildId() != null) {
				while(currentAnomaly.getChildId() != null) {
					currentAnomaly = repository.findById(currentAnomaly.getChildId());
					childList.add(currentAnomaly);
				}
			}
			if(parentList.size() > 0) {
				resultList = new ArrayList<>(parentList.reversed());
			}
			resultList.add(originAnomaly);
			resultList.addAll(childList);
			
			return new QuerySuccess<List<AnomalyDto>>(resultList.stream()
					.map(AnomalyDtoMapper::mapToDto)
					.toList());
			
		}catch(AnomalyNotFoundException e) {
			return new QueryNotFound<List<AnomalyDto>>();
		}catch(TechnicalException e) {
			return new QueryFailure<List<AnomalyDto>>(e.getMessage());
		}
	}
}
