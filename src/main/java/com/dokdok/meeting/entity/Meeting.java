package com.dokdok.meeting.entity;

import com.dokdok.book.entity.Book;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok._global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE meeting SET deleted_at = CURRENT_TIMESTAMP WHERE meeting_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Meeting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_leader_id")
    private User meetingLeader;

    @Column(name = "meeting_name", length = 100)
    private String meetingName;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "meeting_status", nullable = false, length = 20)
    @Builder.Default
    private String meetingStatus = "PENDING";

    @Column(name = "meeting_start_date")
    private LocalDateTime meetingStartDate;

    @Column(name = "meeting_end_date")
    private LocalDateTime meetingEndDate;
}