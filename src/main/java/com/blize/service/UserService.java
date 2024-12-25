package com.blize.service;

import com.blize.dto.request.RegisterRequestDTO;
import com.blize.entity.User;
import com.blize.repository.UserRepository;
import com.blize.repository.UserRepositoryCustom;
import com.blize.service.files.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private FileService fileService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepositoryCustom userRepositoryCustom;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private LocalFileUploader localFileUploader;
    @Autowired
    private S3FileUploader s3FileUploader;


    public User findById(int id) {
        return userRepository.findById(id);
    }

    public User findByUserName(String userName) {
        return userRepository.findFirstByUsername(userName);
    }

    public User updateLastSeen(User user) {
        userRepository.findById(user.getId());
        user.setLastSeen(OffsetDateTime.now());
        return userRepository.save(user);
    }

    public void manuelAuthenticateUser(String username, String password, HttpServletRequest request) {

        var context = SecurityContextHolder.getContext();
        var session = request.getSession(true);
        var token = new UsernamePasswordAuthenticationToken(username, password);

        context.setAuthentication(authenticationManager.authenticate(token));
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    public User register(RegisterRequestDTO registerRequestDTO, String imageFilePath) {

        var user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setEmail(registerRequestDTO.getEmail());
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setLastSeen(OffsetDateTime.now());
        user.setRoles("[\"ROLE_USER\"]");
        user.setImage(imageFilePath);
        return this.userRepository.save(user);
    }

    public Object createJwtToken(User user) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        var json = objectMapper.writeValueAsString(user);
        var claims = new HashMap<>(Map.of("user", objectMapper.readValue(json, Map.class)));

        String rsaKey = this.fileService.getFileContent("/rsa/private.key");

        rsaKey = rsaKey
                .replaceAll("(?m)^--.*", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(rsaKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        var jwtResponse = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 30)) // 30 sec
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .setHeaderParam("typ", "JWT")
                .compact();

        return new HashMap<>(Map.of("token", jwtResponse));
    }

    public List<User> findUsersNotInIds(List<Integer> ids, int offset) {
        return userRepositoryCustom.findUsersNotInIds(ids, offset);
    }

    public FileUploader.FileInfo uploadImage(BufferedImage bufferedImage) {

            try {
                var rootFolder = "/files/images";
                var filePath = "/user/"+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
                var filename = DigestUtils.md5DigestAsHex((System.nanoTime() + UUID.randomUUID().toString()).getBytes())+".jpeg";

                imageUploadService.checkSize(bufferedImage, ImageUploadService.CROP_128, ImageUploadService.CROP_128);
                bufferedImage = imageUploadService.resizeMin(bufferedImage,ImageUploadService.CROP_128, ImageUploadService.CROP_128);
                bufferedImage = imageUploadService.cropRatio(bufferedImage, 1);
                //return imageUploadService.save(bufferedImage, rootFolder, filePath, filename, localFileUploader);
                return imageUploadService.save(bufferedImage, rootFolder, filePath, filename, s3FileUploader);

            } catch (Exception e) {
                e.printStackTrace();
            }

        return null;
    }

}
