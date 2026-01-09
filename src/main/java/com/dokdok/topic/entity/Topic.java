package com.dokdok.topic.entity;

import com.dokdok._global.BaseTimeEntity;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "topic")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE topic SET deleted_at = CURRENT_TIMESTAMP WHERE topic_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Topic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by", nullable = false)
    private User proposedBy;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "topic_type", nullable = false, length = 30)
    private String topicType;

    @Column(name = "topic_status", nullable = false, length = 20)
    @Builder.Default
    private String topicStatus = "PROPOSED";

    @Column(name = "vote_count", nullable = false)
    @Builder.Default
    private Integer voteCount = 0;
}