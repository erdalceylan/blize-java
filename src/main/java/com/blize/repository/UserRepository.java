package com.blize.repository;

import com.blize.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    public List<User> findByEmail(String email);

    @Query(value = "SELECT u FROM user u where u.username=:username OR u.email=:username")
    public User findForLogin(String username);

    public User findById(int id);

    public List<User> findUsersByIdIn(List<Integer> ids);

    public User findFirstByUsername(String username);

    boolean existsByEmail(String firstName);

    boolean existsByUsername(String username);
}
