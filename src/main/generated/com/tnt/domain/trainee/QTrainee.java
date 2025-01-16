package com.tnt.domain.trainee;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTrainee is a Querydsl query type for Trainee
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTrainee extends EntityPathBase<Trainee> {

    private static final long serialVersionUID = 132580313L;

    public static final QTrainee trainee = new QTrainee("trainee");

    public final com.tnt.global.common.entity.QBaseTimeEntity _super = new com.tnt.global.common.entity.QBaseTimeEntity(this);

    public final StringPath cautionNote = createString("cautionNote");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> height = createNumber("height", Double.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QTrainee(String variable) {
        super(Trainee.class, forVariable(variable));
    }

    public QTrainee(Path<? extends Trainee> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTrainee(PathMetadata metadata) {
        super(Trainee.class, metadata);
    }

}

