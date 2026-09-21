package com.centresportifets.athlets_backend.core.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.centresportifets.athlets_backend.user.UserAccountRepository;
import com.centresportifets.athlets_backend.user.UserStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserAccountRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var session = request.getSession(false);
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && users.findByUsername(auth.getName())
                    .filter(user -> UserStatus.ACTIVE.getStatus().equals(user.getAccountStatus())
                            && session != null
                            && user.getId().equals(session.getAttribute("accountId"))
                            && Long.valueOf(user.getSessionVersion()).equals(session.getAttribute("accountSessionVersion"))).isEmpty()) {
            SecurityContextHolder.clearContext();
            if (session != null) session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erreur\":\"La session a été révoquée. Veuillez vous reconnecter.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
