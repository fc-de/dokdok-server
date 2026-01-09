package com.dokdok.retrospective.entity;

import com.dokdok._global.BaseTimeEntity;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "shared_retrospective")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE shared_retrospective SET deleted_at = CURRENT_TIMESTAMP WHERE shared_retrospective_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SharedRetrospective extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shared_retrospective_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "key_topics", columnDefinition = "TEXT")
    private String keyTopics;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}