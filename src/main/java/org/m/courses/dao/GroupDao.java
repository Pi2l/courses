package org.m.courses.dao;

import org.m.courses.model.Group;
import org.m.courses.repository.GroupRepository;
import org.m.courses.repository.PrimaryRepository;
import org.m.courses.service.UserAuthorizationService;
import org.springframework.stereotype.Component;

@Component
public class GroupDao extends AbstractDao<Group> {

    private final GroupRepository repository;

    public GroupDao(GroupRepository repository, UserAuthorizationService authorizationService) {
        super(authorizationService);
        this.repository = repository;
    }

    @Override
    protected PrimaryRepository<Group, Long> getRepository() {
        return repository;
    }

    @Override
    protected boolean canModify(Long id) {
        return isAdmin();
    }

}
