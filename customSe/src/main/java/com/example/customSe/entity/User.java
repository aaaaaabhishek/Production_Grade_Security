package com.example.customSe.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    private String username;             //give email id
    private String password;
    private String secretKey;
    @ManyToMany(fetch = FetchType.EAGER,cascade =CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id",referencedColumnName ="id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName ="id")
    )
    private Set<Role> roles;
}