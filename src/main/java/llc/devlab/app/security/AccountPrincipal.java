package llc.devlab.app.security;

import llc.devlab.app.model.entity.Account;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AccountPrincipal(Long id, String email, String displayName, String passwordHash) implements UserDetails {

    public static AccountPrincipal from(Account account) {
        return new AccountPrincipal(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getPasswordHash()
        );
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }
}
