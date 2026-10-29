package com.example.wallet;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
    name = "accounts"
)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String ownerId;
    private long balanceCents;

    @Version
    private long version;
    private Instant createdAt = Instant.now();

    protected Account(){

    }
    public Account(String ownerId){
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID cannot be null or blank");
        }
        this.ownerId = ownerId;
        this.balanceCents = 0L;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
