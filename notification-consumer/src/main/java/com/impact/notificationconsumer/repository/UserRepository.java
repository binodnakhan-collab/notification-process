package com.impact.notificationconsumer.repository;

import com.impact.notificationconsumer.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u from UserEntity u where u.country = :country")
    List<UserEntity> getUserByCountry(String country);
}
