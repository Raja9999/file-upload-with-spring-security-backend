package com.demo.service;

import com.demo.models.MyUserDetails;
import com.demo.models.User;
import com.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUserName(username);
        return new MyUserDetails(user);
    }


    public void saveCompressedFile(byte[] file, String fileName, String username) throws IOException {
        User user = new User();
        user = repo.findByUserName(username);

//        List<byte[]> files = user.getAttachmentFileData();
//        files.add(file);
        user.setAttachmentFileData(Collections.singletonList(file));
        user.setFileName(fileName);
        repo.save(user);
    }



}
