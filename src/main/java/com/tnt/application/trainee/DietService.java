package com.tnt.application.trainee;

import static com.tnt.common.error.model.ErrorMessage.DIET_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.trainee.Diet;
import com.tnt.infrastructure.mysql.repository.trainee.DietRepository;
import com.tnt.infrastructure.mysql.repository.trainee.DietSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {

	private final DietRepository dietRepository;
	private final DietSearchRepository dietSearchRepository;

	@Transactional
	public Diet save(Diet diet) {
		return dietRepository.save(diet);
	}

	public Diet getDietWithTraineeIdAndDietId(Long dietId, Long traineeId) {
		return dietRepository.findByIdAndTraineeIdAndDeletedAtIsNull(dietId, traineeId)
			.orElseThrow(() -> new NotFoundException(DIET_NOT_FOUND));
	}

	public List<Diet> getDietsWithTraineeIdForDaily(Long traineeId, Integer year, Integer month) {
		return dietSearchRepository.findAllByTraineeIdForDaily(traineeId, year, month);
	}
}
