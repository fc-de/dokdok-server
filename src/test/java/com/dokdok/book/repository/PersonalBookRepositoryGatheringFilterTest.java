package com.dokdok.book.repository;

import com.dokdok.book.entity.Book;
import com.dokdok.gathering.entity.Gathering;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QA: "모임에 없는 책인데 필터링 버튼이 있음" 회귀 테스트.
 *
 * 책 상세의 독서모임 필터 목록(GET /api/book/{personalBookId}/gatherings)은
 * "그 책으로 약속(meeting)이 잡혔고, 그 약속에 내가 참여한" 모임만 나와야 한다.
 * 과거 구현은 personal_book.gathering_id 만 봐서 책과 무관한 모임이 노출됐다.
 * PersonalBookRepository.findActiveGatheringsWithMeetingsByUserAndBook 를 실제 DB로 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
class PersonalBookRepositoryGatheringFilterTest {

    @Autowired
    private PersonalBookRepository personalBookRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("그 책으로 약속이 잡힌, 내가 참여한 모임만 반환한다 (다른 책 모임은 제외)")
    void returnsOnlyGatheringsWhereBookIsUsedAndUserIsMember() {
        // given
        User user = persistUser("회원");

        Book targetBook = persistBook("모순");
        Book otherBook = persistBook("관련없는책");

        // 대상 책으로 약속이 잡힌 모임 (내가 참여) → 포함되어야 함
        Gathering targetGathering = persistGathering(user, "2차QA모임");
        Meeting targetMeeting = persistMeeting(targetGathering, targetBook);
        persistMember(targetMeeting, user);

        // 다른 책으로 약속이 잡힌 모임 (내가 참여) → 제외되어야 함
        Gathering otherGathering = persistGathering(user, "다른모임");
        Meeting otherMeeting = persistMeeting(otherGathering, otherBook);
        persistMember(otherMeeting, user);

        em.flush();
        em.clear();

        // when
        List<PersonalBookGatheringProjection> result =
                personalBookRepository.findActiveGatheringsWithMeetingsByUserAndBook(
                        user.getId(), targetBook.getId());

        // then
        assertThat(result)
                .extracting(PersonalBookGatheringProjection::getGatheringName)
                .containsExactly("2차QA모임");
    }

    @Test
    @DisplayName("그 책으로 약속이 있어도 내가 참여하지 않은 모임은 제외한다")
    void excludesGatheringWhereUserIsNotMeetingMember() {
        // given
        User user = persistUser("회원");
        User other = persistUser("남");

        Book book = persistBook("모순");

        Gathering gathering = persistGathering(other, "내가없는모임");
        Meeting meeting = persistMeeting(gathering, book);
        persistMember(meeting, other); // 다른 사람만 참여

        em.flush();
        em.clear();

        // when
        List<PersonalBookGatheringProjection> result =
                personalBookRepository.findActiveGatheringsWithMeetingsByUserAndBook(
                        user.getId(), book.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 모임에서 같은 책으로 약속이 여러 개여도 중복 없이 한 번만 반환한다")
    void deduplicatesSameGathering() {
        // given
        User user = persistUser("회원");
        Book book = persistBook("모순");

        Gathering gathering = persistGathering(user, "2차QA모임");
        Meeting m1 = persistMeeting(gathering, book);
        Meeting m2 = persistMeeting(gathering, book);
        persistMember(m1, user);
        persistMember(m2, user);

        em.flush();
        em.clear();

        // when
        List<PersonalBookGatheringProjection> result =
                personalBookRepository.findActiveGatheringsWithMeetingsByUserAndBook(
                        user.getId(), book.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGatheringName()).isEqualTo("2차QA모임");
    }

    private User persistUser(String name) {
        return em.persist(User.builder()
                .userName(name)
                .nickname(name)
                .userEmail(name + System.nanoTime() + "@test.com")
                .kakaoId(System.nanoTime())
                .build());
    }

    private Gathering persistGathering(User leader, String name) {
        return em.persist(Gathering.builder()
                .gatheringLeader(leader)
                .gatheringName(name)
                .invitationLink("invite-" + System.nanoTime())
                .build());
    }

    private Book persistBook(String name) {
        return em.persist(Book.builder()
                .bookName(name)
                .author("저자")
                .publisher("출판사")
                .isbn("978-" + System.nanoTime())
                .build());
    }

    private Meeting persistMeeting(Gathering gathering, Book book) {
        return em.persist(Meeting.builder()
                .gathering(gathering)
                .book(book)
                .meetingName("테스트 약속")
                .meetingStatus(MeetingStatus.CONFIRMED)
                .meetingStartDate(LocalDateTime.of(2026, 1, 1, 19, 0))
                .meetingEndDate(LocalDateTime.of(2026, 1, 1, 21, 0))
                .build());
    }

    private void persistMember(Meeting meeting, User user) {
        em.persist(MeetingMember.builder()
                .meeting(meeting)
                .user(user)
                .joinedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build());
    }
}
