package com.centresportifets.athlets_backend.user;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);
	Optional<UserAccount> findByEmail(String email);

	boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<UserAccount> findByAccessLevelInOrderByLastNameAscFirstNameAsc(List<Integer> accessLevels);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUsernameAndIdNot(String username, Long id);
    long countByAccessLevelAndAccountStatus(int accessLevel, String accountStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserAccount> findByAccessLevelOrderByIdAsc(int accessLevel);
}
