package com.wsantos.finance_backend.services.accounts.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    public Account(UUID owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID owner;

    @Column(nullable = false)
    private String name;

    @Column(name = "archived_at")
    private Instant archivedAt;

    /** Changes the account's display name. */
    public void rename(String name) {
        this.name = name;
    }
}
