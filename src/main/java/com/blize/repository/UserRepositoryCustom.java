package com.blize.repository;

import com.blize.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryCustom {

    public static final int LIST_LIMIT = 24;

    @PersistenceContext
    private EntityManager em;

    public List<User> findUsersNotInIds(List<Integer> ids, int offset) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery
                .select(root)
                .where(root.get("id").in(ids).not())
                .orderBy(criteriaBuilder.asc(root.get("id")));

        return em.createQuery(criteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(LIST_LIMIT)
                .getResultList();
    }

//        public List<User> getByTest() {
//        CriteriaBuilder crBuilder = this.em.getCriteriaBuilder();
//        CriteriaQuery<User> crQuery= this.em.getCriteriaBuilder().createQuery(User.class);
//
//        Root<User> userRoot = crQuery.from(User.class);
//
//        crQuery.select(userRoot)
//                .where(crBuilder.lt(userRoot.get("id"), 10));
//
//        return em.createQuery(crQuery).getResultList();
//    }
}
