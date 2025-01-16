package com.tnt.domain.pt;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPtTrainerTrainee is a Querydsl query type for PtTrainerTrainee
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPtTrainerTrainee extends EntityPathBase<PtTrainerTrainee> {

    private static final long serialVersionUID = 14180152L;

    public static final QPtTrainerTrainee ptTrainerTrainee = new QPtTrainerTrainee("ptTrainerTrainee");

    public final com.tnt.global.common.entity.QBaseTimeEntity _super = new com.tnt.global.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> deletedAt = createDate("deletedAt", java.time.LocalDate.class);

    public final NumberPath<Integer> finishedPtCount = createNumber("finishedPtCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> startedAt = createDate("startedAt", java.time.LocalDate.class);

    public final NumberPath<Integer> totalPtCount = createNumber("totalPtCount", Integer.class);

    public final NumberPath<Long> traineeId = createNumber("traineeId", Long.class);

    public final NumberPath<Long> trainerId = createNumber("trainerId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPtTrainerTrainee(String variable) {
        super(PtTrainerTrainee.class, forVariable(variable));
    }

    public QPtTrainerTrainee(Path<? extends PtTrainerTrainee> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPtTrainerTrainee(PathMetadata metadata) {
        super(PtTrainerTrainee.class, metadata);
    }

}

