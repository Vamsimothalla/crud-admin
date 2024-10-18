package com.motalla.crudadmin.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public class AdminRepository<T,ID> extends SimpleJpaRepository<T,ID> {

    private final EntityManager entityManager;
    public AdminRepository(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }




    @Override
    public Optional<T> findById(ID id) {
        return Optional.of(entityManager.find(getDomainClass(), id));
    }

    public Page<T> findAll(int page, int size) {
        return super.findAll(PageRequest.of(page, size));
    }
    public Page<T> findAll(int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = Sort.by(direction, sortBy);
        return super.findAll(PageRequest.of(page, size,sort));
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(getDomainClass());
        Root<T> root = cq.from(getDomainClass());
        return entityManager.createQuery(cq.select(root)).getResultList();
    }

    public List<T> findAllByCriteria(CriteriaQuery<T> criteriaQuery) {
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public T findOneByCriteria(CriteriaQuery<T> criteriaQuery) {
        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }
}
