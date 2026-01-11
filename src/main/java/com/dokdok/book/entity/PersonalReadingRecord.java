package com.dokdok.book.entity;

import com.dokdok._global.BaseTimeEntity;
import com.dokdok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "personal_reading_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE personal_reading_record SET deleted_at = CURRENT_TIMESTAMP WHERE record_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PersonalReadingRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_book_id", nullable = false)
    private PersonalBook personalBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_content", columnDefinition = "TEXT")
    private String recordContent;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;
}