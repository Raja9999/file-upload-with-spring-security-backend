package com.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "login-compreesed-db")
public class User {
    @Id
    @GeneratedValue
    private String id;
    private String userName;

    public User( String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.active = true;
        this.roles= "user";
    }

    private String password;
    private String roles;
    private boolean active;
    private List<byte[]> attachmentFileData;
    private String fileName;


}
