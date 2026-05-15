package com.ngtoan.phone_store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MembershipHistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer historyID;

    Integer userID;

    Integer fromLevelID;

    Integer toLevelID;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", insertable = false, updatable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fromLevelID", insertable = false, updatable = false)
    MembershipLevel fromLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toLevelID", insertable = false, updatable = false)
    MembershipLevel toLevel;
}