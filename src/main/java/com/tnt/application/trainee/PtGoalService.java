package com.tnt.application.trainee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.trainee.PtGoal;
import com.tnt.infrastructure.mysql.repository.pt.PtGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PtGoalService {

	private final PtGoalRepository ptGoalRepository;

	public List<PtGoal> getAllPtGoalsWithTraineeId(Long traineeId) {
		return ptGoalRepository.findAllByTraineeIdAndDeletedAtIsNull(traineeId);
	}

	public List<PtGoal> saveAllPtGoals(List<PtGoal> ptGoals) {
		return ptGoalRepository.saveAll(ptGoals);
	}
}
