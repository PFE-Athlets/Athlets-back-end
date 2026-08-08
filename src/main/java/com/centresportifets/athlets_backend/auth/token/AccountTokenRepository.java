package com.centresportifets.athlets_backend.auth.token;

import com.centresportifets.athlets_backend.user.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

	Optional<AccountToken> findByToken(String token);

	List<AccountToken> findByUserAndTypeAndUsedAtIsNull(UserAccount user, String type);
}