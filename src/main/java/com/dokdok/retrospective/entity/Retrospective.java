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
@Table(name = "retrospective")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE retrospective SET deleted_at = CURRENT_TIMESTAMP WHERE retrospective_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Retrospective extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "retrospective_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "changed_thought", columnDefinition = "TEXT")
    private String changedThought;

    @Column(name = "impressive_perspective", columnDefinition = "TEXT")
    private String impressivePerspective;

    @Column(name = "reread_part", columnDefinition = "TEXT")
    private String rereadPart;

    @Column(name = "is_shared", nullable = false)
    @Builder.Default
    private Boolean isShared = false;
}