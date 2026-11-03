package com.example.wallet;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository 
public interface AccountRepository extends JpaRepository<Account, Long>{
    
    Optional<Account> findByOwnerIdAndId(Long id, String ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findLockByOwnerIdAndId(String ownerId, Long id);
}
