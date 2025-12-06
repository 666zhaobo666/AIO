package com.aio.module.user.repository;

import com.aio.module.user.entity.UserPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPasswordRepository extends JpaRepository<UserPasswordEntity, Integer> {

    @Query("SELECT up.password FROM UserPasswordEntity up WHERE up.userId = :userId")
    String findPasswordByUserId(@Param("userId") Integer userId);

    Optional<UserPasswordEntity> findByUserId(Integer userId);
}

