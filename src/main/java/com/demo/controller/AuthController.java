package com.demo.controller;


import com.demo.models.AuthenticationRequest;
import com.demo.models.AuthenticationResponse;
import com.demo.models.MessageResponse;
import com.demo.models.User;
import com.demo.repository.UserRepository;
import com.demo.service.MyUserDetailsService;
import com.demo.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository repo;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @GetMapping("/hello")
    public String greet(){
        return "Hello";
    }
    @PostMapping("/login")

    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest){

        UsernamePasswordAuthenticationToken token=
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(),authenticationRequest.getPassword());

        authenticationManager.authenticate(token);
        UserDetails userDetails=userDetailsService.loadUserByUsername(authenticationRequest.getUserName());
        if (!userDetails.getUsername().equals(authenticationRequest.getUserName()) || !userDetails.getPassword().equals(authenticationRequest.getPassword())){
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid username or password", HttpStatus.UNAUTHORIZED));
        }
        String jwt=jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt, userDetails.getUsername()));

    }
    @PostMapping("/upload")
    public ResponseEntity<StreamingResponseBody> uploadAndCompress(@RequestParam("files") MultipartFile[] files) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files.zip\"")
                .body(outputStream -> {
                    try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                        for (MultipartFile file : files) {
                            if (!file.isEmpty()) {
                                String fileName = file.getOriginalFilename();
                                ZipEntry zipEntry = new ZipEntry(fileName);
                                zipOut.putNextEntry(zipEntry);

                                byte[] bytes = file.getBytes();
                                zipOut.write(bytes);
                                UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                                String username = userDetails.getUsername();
                                userDetailsService.saveCompressedFile(bytes, fileName, username);
                            }
                        }
                        zipOut.finish();
                    } catch (IOException e) {
                       System.out.println("error");
                    }
                });
    }
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token){
        log.info("===========token"+token);
        UserDetails userDetails=userDetailsService.loadUserByUsername(jwtUtil.extractUsername(token));
        boolean result=jwtUtil.validateToken(token,userDetails);
        if(result){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Unauthorized",HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser( @RequestBody User user) {
        try {
            if (repo.existsByUserName(user.getUserName())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Username already exists", HttpStatus.INTERNAL_SERVER_ERROR));
            }

            User user1 = new User(user.getUserName(), user.getPassword());
            repo.save(user1);

            return ResponseEntity.ok(new MessageResponse("User signed up successfully", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
    @GetMapping("/all")
    public ResponseEntity<User> getAllFiles(@RequestParam String userName) {
        try {
            User user1 = repo.findByUserName(userName);
            return new ResponseEntity<>(user1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
