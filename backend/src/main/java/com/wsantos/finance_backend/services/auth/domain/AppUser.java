package com.wsantos.finance_backend.services.auth.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.wsantos.finance_backend.services.currency.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "app_user")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {
    public AppUser(String email, String passwordHash, Currency currency) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.currency = currency;
    }

    @Id
    @GeneratedValue 
    private UUID id; 

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "created_at")
    @CreationTimestamp 
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp 
    private Instant updatedAt;

    @ManyToOne 
    @JoinColumn(name = "base_currency")
    private Currency currency;
}
