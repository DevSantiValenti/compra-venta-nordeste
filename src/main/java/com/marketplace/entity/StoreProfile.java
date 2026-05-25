package com.marketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "store_profiles")
@Getter
@Setter
public class StoreProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 140)
    private String storeName;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    private String logoUrl;
    private String bannerUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 40)
    private String phone;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    private boolean premium = false;
    private LocalDateTime premiumUntil;
    private boolean active = true;
}
