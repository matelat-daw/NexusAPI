package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.interfaces.RoleRepository;
import com.futureprograms.NexusAPI.interfaces.UserRepository;
import com.futureprograms.NexusAPI.model.*;
import com.futureprograms.NexusAPI.security.AspNetIdentityPasswordVerifier;
import com.futureprograms.NexusAPI.service.JwtService;
import com.futureprograms.NexusAPI.service.EmailSenderService;
import com.futureprograms.NexusAPI.model.EmailConfirmation;
import com.futureprograms.NexusAPI.interfaces.EmailConfirmationRepository;
import com.futureprograms.NexusAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.futureprograms.NexusAPI.security.AspNetIdentityPasswordVerifier.hashPassword;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailSenderService emailSender;
    private final EmailConfirmationRepository emailConfirmationRepository;

    @Autowired
    public UserController(UserRepository userRepository, RoleRepository roleRepository, JwtService jwtService, EmailSenderService emailSender, UserService userService, EmailConfirmationRepository emailConfirmationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailSender = emailSender;
        this.emailConfirmationRepository = emailConfirmationRepository;
    }

    @PostMapping(value = "/Auth/Register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(@ModelAttribute RegisterRequest model) {
        if (userRepository.existsByNick(model.getNick())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nick ya registrado"));
        }
        if (userRepository.findByEmail(model.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
        }

        try {
            String profileImagePath = "";
            boolean profile = false;

            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setNick(model.getNick());
            user.setName(model.getName());
            user.setSurname1(model.getSurname1());
            user.setSurname2(nullIfEmpty(model.getSurname2()));
            user.setUserName(model.getEmail());
            user.setEmail(model.getEmail());
            user.setPhone(nullIfEmpty(model.getPhoneNumber()));
            user.setProfileImage(profileImagePath);
            user.setBday(model.getBday() != null ? model.getBday() : LocalDate.now());
            user.setAbout(nullIfEmpty(model.getAbout()));
            user.setUserLocation(nullIfEmpty(model.getUserLocation()));
            user.setPublicProfile(profile);
            user.setEmailConfirmed(false);

            try {
                user.setPassword(hashPassword(model.getPassword()));
            } catch (Exception e) {
                throw new RuntimeException("ERROR: No se pudo procesar la contraseña.");
            }
            Role basicRole = roleRepository.findByName("Basic");
            Set<Role> roles = new HashSet<>();
            roles.add(basicRole);
            user.setRoles(roles);
            userService.register(user);

            String token = java.util.UUID.randomUUID().toString();
            EmailConfirmation confirmationToken = new EmailConfirmation();
            confirmationToken.setToken(token);
            confirmationToken.setUser(user);
            confirmationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
            emailConfirmationRepository.save(confirmationToken);
            String confirmationLink = "http://localhost:8080/api/Auth/ConfirmEmail?userId=" + user.getId() + "&token=" + token;

            String emailBody = """
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6;">
            <h2>Bienvenido a NexusAPI, %s!</h2>
            <p>Gracias por registrarte. Para activar tu cuenta, por favor haz clic en el siguiente enlace:</p>
            <p><a href="%s" style="padding: 10px 15px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">Confirmar mi cuenta</a></p>
            <p>El enlace expirará en 24 horas.</p>
            <p>Si no has creado esta cuenta, puedes ignorar este mensaje.</p>
            <p>Saludos,<br>El equipo de Nexus Astralis</p>
        </body>
        </html>
    """.formatted(user.getName(), confirmationLink);

            emailSender.sendEmail(user.getEmail(), "Confirma tu registro en NexusAPI", emailBody);

            return ResponseEntity.ok("Confirma tu Registro.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al registrar usuario: " + e.getMessage());
        }
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    @GetMapping("/Auth/ConfirmEmail")
    public ResponseEntity<?> confirmEmail(@RequestParam("userId") String userId, @RequestParam("token") String token) {
        // Buscar el usuario
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        User user = userOptional.get();

        // Buscar el token
        EmailConfirmation confirmation = emailConfirmationRepository.findByToken(token);
        if (confirmation == null) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        // Verificar que el token corresponde al usuario
        if (!confirmation.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body("El token no corresponde a este usuario");
        }

        // Verificar que el token no ha expirado
        if (confirmation.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailConfirmationRepository.delete(confirmation);
            return ResponseEntity.badRequest().body("El token ha expirado");
        }

        // Actualizar el usuario y guardar
        user.setEmailConfirmed(true);
        userRepository.save(user);

        // Eliminar el token usado
        emailConfirmationRepository.delete(confirmation);

        return ResponseEntity.ok("¡Email confirmado correctamente! Ya puedes iniciar sesión.");
    }

    @PutMapping("/Account/Update")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails userDetails, @RequestBody RegisterRequest req) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        if (req.getName() != null) user.setName(req.getName());
        if (req.getSurname1() != null) user.setSurname1(req.getSurname1());
        if (req.getSurname2() != null) user.setSurname2(req.getSurname2());
        if (req.getPhoneNumber() != null) user.setPhone(req.getPhoneNumber());
        if (req.getBday() != null) user.setBday(req.getBday());
        if (req.getUserLocation() != null) user.setUserLocation(req.getUserLocation());
        if (req.getProfileImage() != null) user.setProfileImage(null);
        if (req.getPublicProfile() != null)
            user.setPublicProfile("1".equals(req.getPublicProfile()));
        if (req.getAbout() != null) user.setAbout(req.getAbout());

        userRepository.save(user);
        return ResponseEntity.ok("Perfil actualizado correctamente");
    }

    @GetMapping("/Account/GetUsers")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/Auth/Login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            System.out.println("Usuario no encontrado en la base de datos");
            return ResponseEntity.status(401).body(Map.of("error", "E-mail Inválido"));
        }

        boolean passwordCorrect = AspNetIdentityPasswordVerifier.verifyPassword(loginRequest.getPassword(), user.getPassword());
        System.out.println("Password verificación: " + (passwordCorrect ? "correcta" : "incorrecta"));

        if (!passwordCorrect) {
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña Inválida"));
        }

        // La línea problemática - depende de cómo está definido tu getter
        boolean emailConfirmed = user.getEmailConfirmed(); // O user.getEmailConfirmed() según tu clase
        System.out.println("Email confirmado: " + emailConfirmed);

        if (!emailConfirmed) {
            return ResponseEntity.status(401).body(Map.of("error", "Por favor, confirma tu email antes de iniciar sesión"));
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        if (roles.isEmpty()) {
            Role basicRole = roleRepository.findByName("Basic");
            if (basicRole != null) {
                user.setRoles(Set.of(basicRole));
                userRepository.save(user);
                roles = List.of("Basic");
            }
        }
        String token = jwtService.generateToken(user, roles);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);

        response.addCookie(cookie);

        return ResponseEntity.ok("Login Exitoso");
    }

    @PostMapping("/Account/Logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok("Logout Exitoso");
    }

    @DeleteMapping("/Account/Delete")
    public ResponseEntity<?> delete(@CookieValue("token") String token) {
        String userId = jwtService.getUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        // Desvincular roles antes de eliminar
        user.getRoles().clear();
        userRepository.save(user);

        // Ahora eliminar el usuario
        userRepository.delete(user);
        return ResponseEntity.ok("Perfil Eliminado Correctamente");
    }
}