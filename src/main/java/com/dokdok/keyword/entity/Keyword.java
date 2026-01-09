package com.dokdok.keyword.entity;

import com.dokdok._global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE keyword SET deleted_at = CURRENT_TIMESTAMP WHERE keyword_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Keyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    @Column(name = "keyword_type", nullable = false, length = 30)
    private String keywordType;

    @Column(name = "keyword_name", nullable = false, length = 50)
    private String keywordName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Keyword parent;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "sort_order")
    private Integer sortOrder;
}