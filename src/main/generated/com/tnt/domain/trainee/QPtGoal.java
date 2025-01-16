package com.tnt.domain.trainee;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPtGoal is a Querydsl query type for PtGoal
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPtGoal extends EntityPathBase<PtGoal> {

    private static final long serialVersionUID = -1217540634L;

    public static final QPtGoal ptGoal = new QPtGoal("ptGoal");

    public final com.tnt.global.common.entity.QBaseTimeEntity _super = new com.tnt.global.common.entity.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> traineeId = createNumber("traineeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPtGoal(String variable) {
        super(PtGoal.class, forVariable(variable));
    }

    public QPtGoal(Path<? extends PtGoal> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPtGoal(PathMetadata metadata) {
        super(PtGoal.class, metadata);
    }

}

