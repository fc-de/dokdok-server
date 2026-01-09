package com.dokdok.user.entity;

import com.dokdok._global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;
}
