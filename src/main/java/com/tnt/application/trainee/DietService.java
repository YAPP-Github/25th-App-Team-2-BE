package com.tnt.application.trainee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.trainee.Diet;
import com.tnt.infrastructure.mysql.repository.trainee.DietRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {

	private final DietRepository dietRepository;

	@Transactional
	public Diet save(Diet diet) {
		return dietRepository.save(diet);
	}
}
