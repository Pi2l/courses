package org.m.courses.repository;

import org.m.courses.model.Identity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface PrimaryRepository<Entity extends Identity<IdType>, IdType> extends JpaRepository<Entity, IdType>
        , JpaSpecificationExecutor<Entity> {
}
