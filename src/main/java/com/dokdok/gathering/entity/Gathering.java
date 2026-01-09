package com.dokdok.gathering.entity;

import com.dokdok._global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "gathering")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE gathering SET deleted_at = CURRENT_TIMESTAMP WHERE gathering_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Gathering extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gathering_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_leader_id", nullable = false)
    private User gatheringLeader;

    @Column(name = "gathering_name", nullable = false, length = 255)
    private String gatheringName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "invitation_link", nullable = false, length = 255)
    private String invitationLink;

    @Column(name = "gathering_status", nullable = false, length = 20)
    @Builder.Default
    private String gatheringStatus = "ACTIVE";
}