package com.futureprograms.NexusAPI.controllers;

import com.futureprograms.NexusAPI.interfaces.*;
import com.futureprograms.NexusAPI.models.*;
import com.futureprograms.NexusAPI.security.AspNetIdentityPasswordVerifier;
import com.futureprograms.NexusAPI.service.JwtService;
import com.futureprograms.NexusAPI.service.EmailSenderService;
import com.futureprograms.NexusAPI.service.UserService;
import com.futureprograms.NexusAPI.service.UserTokenService;
import com.futureprograms.NexusAPI.utils.DateConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ConstellationRepository constellationRepository;
    private final CommentsRepository commentsRepository;
    private final UserService userService;
    private final UserTokenService userTokenService;
    private final JwtService jwtService;
    private final EmailSenderService emailSender;
    private final EmailConfirmationRepository emailConfirmationRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping(value = "/Auth/Register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest model) {
        if (userRepository.existsByNick(model.getNick())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nick ya registrado"));
        }
        if (userRepository.findByEmail(model.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
        }

        try {
            User user = userService.createUserFromRegisterRequest(model);
            userService.register(user);

            String token = java.util.UUID.randomUUID().toString();
            EmailConfirmation confirmationToken = new EmailConfirmation();
            confirmationToken.setToken(token);
            confirmationToken.setUser(user);
            confirmationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
            emailConfirmationRepository.save(confirmationToken);
            String emailBody = buildConfirmationEmail(user, token);

            emailSender.sendEmail(user.getEmail(), "Confirma tu registro en NexusAPI", emailBody);

            return ResponseEntity.ok("Confirma tu Registro.");
        } catch (Exception e) {
            logger.error("Error al registrar usuario", e);
            return ResponseEntity.status(500).body("Error al registrar usuario: " + e.getMessage());
        }
    }

    @GetMapping("/Auth/ConfirmEmail")
    public ResponseEntity<?> confirmEmail(@RequestParam("userId") String userId, @RequestParam("token") String token) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        User user = userOptional.get();

        EmailConfirmation confirmation = emailConfirmationRepository.findByToken(token);
        if (confirmation == null || !confirmation.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body("Token inválido");
        }

        if (confirmation.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailConfirmationRepository.delete(confirmation);
            return ResponseEntity.badRequest().body("El token ha expirado");
        }

        user.setEmailConfirmed(Boolean.TRUE);
        userRepository.save(user);
        emailConfirmationRepository.delete(confirmation);

        return ResponseEntity.ok("¡Email confirmado correctamente! Ya puedes iniciar sesión.");
    }

    @PatchMapping(value = "/Account/Update", consumes = {"multipart/form-data"})
    public ResponseEntity<?> update(@ModelAttribute UpdateProfileRequest req, @CookieValue(value = "token", required = false) String token) {
        logger.info("Inicio de petición PATCH a /Account/Update");
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no proporcionado"));
            }
            User user = userTokenService.getUserFromToken(token);
            if (user == null) {
                logger.warn("Usuario no encontrado para el token proporcionado");
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

            logger.info("Actualizando perfil para el usuario: {} (ID: {})", user.getNick(), user.getId());

            if (req.getNick() != null && !req.getNick().isEmpty() && !req.getNick().equals(user.getNick())) {
                if (userRepository.existsByNick(req.getNick())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Nick ya registrado"));
                }
                user.setNick(req.getNick());
            }
            if (req.getEmail() != null && !req.getEmail().isEmpty() && !req.getEmail().equals(user.getEmail())) {
                if (userRepository.findByEmail(req.getEmail()) != null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
                }
                user.setEmail(req.getEmail());
            }

            if (req.getName() != null && !req.getName().isEmpty()) user.setName(req.getName());
            if (req.getSurname1() != null && !req.getSurname1().isEmpty()) user.setSurname1(req.getSurname1());
            if (req.getSurname2() != null && !req.getSurname2().isEmpty()) user.setSurname2(req.getSurname2());
            if (req.getPhoneNumber() != null && !req.getPhoneNumber().isEmpty()) user.setPhone(req.getPhoneNumber());
            
            if (req.getBday() != null && !req.getBday().isEmpty()) {
                try {
                    user.setBday(DateConverter.convertToLocalDate(req.getBday()));
                } catch (Exception e) {
                    logger.warn("Error al convertir fecha: {}", req.getBday(), e);
                    return ResponseEntity.badRequest().body(Map.of("error", "Formato de fecha inválido. Usa yyyy-MM-dd o d/M/yyyy"));
                }
            }
            
            if (req.getUserLocation() != null && !req.getUserLocation().isEmpty()) user.setUserLocation(req.getUserLocation());
            if (req.getProfileImage() != null && !req.getProfileImage().isEmpty()) {
                try {
                    String imagePath = saveProfileImage(req.getProfileImage(), user.getNick());
                    user.setProfileImage(imagePath);
                } catch (IOException e) {
                    logger.error("Error al guardar imagen de perfil", e);
                    return ResponseEntity.status(500).body(Map.of("error", "Error al guardar la imagen de perfil"));
                }
            }
            if (req.getPublicProfile() != null && !req.getPublicProfile().isEmpty())
                user.setPublicProfile(Boolean.valueOf("1".equals(req.getPublicProfile())));
            if (req.getAbout() != null && !req.getAbout().isEmpty()) user.setAbout(req.getAbout());

            userRepository.save(user);
            logger.info("Perfil actualizado con éxito para: {}", user.getNick());
            return ResponseEntity.ok("Datos Actualizados.");
        } catch (Exception e) {
            logger.error("Error al actualizar perfil", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error al actualizar perfil: " + e.getMessage()));
        }
    }

    @GetMapping("/Account/GetUsers")
    public ResponseEntity<List<UserBasicDto>> getUsers() {
        return ResponseEntity.ok(userRepository.findAllBasicUsers());
    }

    @PostMapping("/Auth/Login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            logger.warn("Usuario no encontrado: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "E-mail Inválido"));
        }
        if (!AspNetIdentityPasswordVerifier.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Contraseña inválida para usuario: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña Inválida"));
        }
        if (!user.getEmailConfirmed()) {
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

        addTokenCookie(token, response);

        return ResponseEntity.ok(Map.of("token", token, "message", "Login Exitoso"));
    }

    @PostMapping("/Auth/GoogleLogin")
    public ResponseEntity<?> googleLogin(@RequestBody ExternalLoginRequest request, HttpServletResponse response) {
        try {
            String googleClientId = "1071917637623-020l5qbcihpj4u7tdv411cov4cfh530c.apps.googleusercontent.com";
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getToken());
            if (idToken == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Token inválido"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            User user = userService.verifyUser(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );

            String localToken = jwtService.generateToken(user, user.getRoles().stream().map(Role::getName).toList());

            addTokenCookie(localToken, response);

            return ResponseEntity.ok(Map.of("token", localToken, "message", "Google Login Exitoso"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token inválido", "error", ex.getMessage()));
        }
    }

    @GetMapping("/Account/Profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("No se recibió cookieToken en la petición a /Account/Profile");
            return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado. Por favor inicia sesión."));
        }
        User user = userTokenService.getUserFromToken(token);
        if (user == null) {
            logger.warn("Token inválido o usuario no encontrado");
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o expirado. Por favor inicia sesión nuevamente."));
        }
        return getResponseEntity(user);
    }

    private ResponseEntity<?> getResponseEntity(User user) {
        List<Integer> favoriteIds = user.getFavorites().stream().map(Favorite::getConstellationId).toList();
        List<?> favoriteConstellations = constellationRepository.findByIdIn(favoriteIds);
        List<?> userComments = commentsRepository.findByUserId(user.getId()).stream()
                .map(c -> Map.of(
                        "id", c.getId(),
                        "userNick", c.getUserNick(),
                        "constellationName", c.getConstellationName(),
                        "comment", c.getComment(),
                        "userId", c.getUserId(),
                        "constellationId", c.getConstellationId()
                )).toList();
        UserInfoDto dto = new UserInfoDto(user);
        dto.favorites = favoriteConstellations;
        dto.comments = userComments;
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/Account/GetUserInfo")
    public ResponseEntity<?> getUserInfo(@RequestParam("nick") String nick, @CookieValue("token") String token) {
        User logedUser = userTokenService.getUserFromToken(token);
        if (logedUser == null) {
            return ResponseEntity.status(404).body("ERROR: Esa Sesión no es válida.");
        }
        User user = userRepository.findByNick(nick);
        if (user == null) {
            return ResponseEntity.status(404).body("ERROR: Ese Usuario no Existe.");
        }
        return getResponseEntity(user);
    }

    @PostMapping("/Account/Logout")
    public ResponseEntity<?> logout(HttpServletResponse response, @CookieValue(value = "token", required = false) String token) {
        if (token != null) {
            User user = userTokenService.getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }
        }
        
        // Limpiar la cookie del token
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // MaxAge=0 elimina la cookie
        
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logout Exitoso"));
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

    private String buildConfirmationEmail(User user, String token) {
        String confirmationLink = "http://localhost:8080/api/Auth/ConfirmEmail?userId=" + user.getId() + "&token=" + token;
        return """
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6;">
            <h2>Bienvenido a Nexus Astralis, %s!</h2>
            <p>Gracias por registrarte. Para activar tu cuenta, por favor haz clic en el siguiente enlace:</p>
            <p><a href="%s" style="padding: 10px 15px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;">Confirmar mi cuenta</a></p>
            <p>El enlace expirará en 24 horas.</p>
            <p>Si no has creado esta cuenta, puedes ignorar este mensaje.</p>
            <p>Saludos,<br>El equipo de Nexus Astralis</p>
        </body>
        </html>
        """.formatted(user.getName(), confirmationLink);
    }

    public static String saveProfileImage(MultipartFile profileImageFile, String nick) throws IOException {
        if (profileImageFile == null || profileImageFile.isEmpty()) {
            return "/imgs/default-profile.jpg";
        }

        try {
            logger.info("Iniciando guardado de imagen para nick: {}", nick);
            String projectDir = System.getProperty("user.dir");
            if (!projectDir.endsWith("NexusAPI") && new File(projectDir, "NexusAPI").exists()) {
                projectDir = projectDir + File.separator + "NexusAPI";
            }
            
            String uploadsDir = projectDir + File.separator + "uploads" + File.separator + "profile" + File.separator + nick;
            logger.info("Directorio de destino: {}", uploadsDir);
            
            File dir = new File(uploadsDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    logger.warn("No se pudo crear el directorio en uploads, usando fallback en src");
                    uploadsDir = projectDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "imgs" + File.separator + "profile" + File.separator + nick;
                    dir = new File(uploadsDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                }
            }

            String fileName = "profile.jpg";
            Path filePath = Paths.get(uploadsDir, fileName);
            logger.info("Ruta final del archivo: {}", filePath.toAbsolutePath());
            
            Files.deleteIfExists(filePath);
            
            // Optimizar imagen: Redimensionar a 400x400 y comprimir (calidad 0.8)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(profileImageFile.getInputStream())
                    .size(400, 400)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);
            
            Files.copy(new ByteArrayInputStream(outputStream.toByteArray()), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Imagen guardada y optimizada con éxito.");
            return "/imgs/profile/" + nick + "/profile.jpg";
        } catch (IOException e) {
            logger.error("Error al guardar imagen de perfil: " + e.getMessage(), e);
            throw e;
        }
    }

    private void addTokenCookie(String token, HttpServletResponse response) {
        // Crear cookie con el JWT token
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);      // No accesible desde JavaScript
        cookie.setSecure(false);        // http en desarrollo, true en producción
        cookie.setPath("/");            // Disponible en toda la aplicación
        cookie.setMaxAge(86400);        // 24 horas
        cookie.setAttribute("SameSite", "Lax"); // CSRF protection
        
        response.addCookie(cookie);
        
        // Log para verificar que se está enviando la cookie
        logger.info("Cookie JWT enviada: token set-cookie");
    }
}