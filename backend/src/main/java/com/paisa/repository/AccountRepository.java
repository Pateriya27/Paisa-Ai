package com.paisa.repository;

import com.paisa.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId")
    Optional<Account> findByIdAndUserId(
        @Param("id") String id,
        @Param("userId") String userId
    );
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Account> findByUserIdAndIsDefaultTrue(@Param("userId") String userId);
}

