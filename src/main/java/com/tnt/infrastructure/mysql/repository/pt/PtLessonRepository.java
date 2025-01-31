package com.tnt.infrastructure.mysql.repository.pt;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.pt.PtLesson;

public interface PtLessonRepository extends JpaRepository<PtLesson, Long> {

}
