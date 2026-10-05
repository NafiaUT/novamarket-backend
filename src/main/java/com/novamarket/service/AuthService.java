package com.novamarket.service;

import com.novamarket.dto.AuthRequest;
import com.novamarket.dto.AuthResponse;
import com.novamarket.dto.RegisterRequest;
import com.novamarket.exception.BadRequestException;
import com.novamarket.model.Role;
import com.novamarket.model.RoleType;
import com.novamarket.model.User;
import com.novamarket.repository.RoleRepository;
import com.novamarket.repository.UserRepository;
import com.novamarket.security.CustomUserDetails;
import com.novamarket.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Un compte avec cette adresse email existe déjà");
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Le rôle ROLE_CUSTOMER n'est pas initialisé"));

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName()
        );
        user.setRoles(Set.of(customerRole));
        User savedUser = userRepository.save(user);

        List<String> roles = List.of(RoleType.ROLE_CUSTOMER.name());
        String token = jwtUtils.generateToken(savedUser.getEmail(), roles);

        return AuthResponse.of(
                token,
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                roles,
                jwtUtils.getJwtExpirationMs()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = jwtUtils.generateToken(userDetails.getUsername(), roles);

        return AuthResponse.of(
                token,
                userDetails.getUsername(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles,
                jwtUtils.getJwtExpirationMs()
        );
    }
}