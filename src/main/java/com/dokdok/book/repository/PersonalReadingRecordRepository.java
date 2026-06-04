package com.dokdok.book.repository;

import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PersonalReadingRecordRepository extends JpaRepository<PersonalReadingRecord, Long> {
    Optional<PersonalReadingRecord> findByIdAndPersonalBook_IdAndUserId(Long id, Long personalBookId, Long userId);
    Page<PersonalReadingRecord> findAllByPersonalBook_IdAndUserId(Long personalBookId, Long userId, Pageable pageable);
    Page<PersonalReadingRecord> findAllByPersonalBook_IdAndUserIdAndRecordTypeIn(Long personalBookId, Long userId, List<RecordType> recordTypes, Pageable pageable);
    long countByPersonalBook_IdAndUserId(Long personalBookId, Long userId);
    long countByPersonalBook_IdAndUserIdAndRecordTypeIn(Long personalBookId, Long userId, List<RecordType> recordTypes);
    List<PersonalReadingRecord> findByIdInAndPersonalBook_IdAndUserId(List<Long> ids, Long personalBookId, Long userId);

    @Query("""
            select record
            from PersonalReadingRecord record
            where record.personalBook.id = :personalBookId
                and record.user.id = :userId
                and (
                    :cursorCreatedAt is null
                    or record.createdAt < :cursorCreatedAt
                    or (record.createdAt = :cursorCreatedAt and record.id < :cursorRecordId)
                )
            order by record.createdAt desc, record.id desc
            """)
    List<PersonalReadingRecord> findRecordsByCursor(
            @Param("personalBookId") Long personalBookId,
            @Param("userId") Long userId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorRecordId") Long cursorRecordId,
            Pageable pageable
    );

    @Query("""
            select record
            from PersonalReadingRecord record
            where record.personalBook.id = :personalBookId
                and record.user.id = :userId
                and record.recordType in :recordTypes
                and (
                    :cursorCreatedAt is null
                    or record.createdAt < :cursorCreatedAt
                    or (record.createdAt = :cursorCreatedAt and record.id < :cursorRecordId)
                )
            order by record.createdAt desc, record.id desc
            """)
    List<PersonalReadingRecord> findRecordsByCursorAndTypes(
            @Param("personalBookId") Long personalBookId,
            @Param("userId") Long userId,
            @Param("recordTypes") List<RecordType> recordTypes,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorRecordId") Long cursorRecordId,
            Pageable pageable
    );
}
