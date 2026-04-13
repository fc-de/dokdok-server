package com.dokdok.meeting.scheduler;

import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingStatus;
import com.dokdok.meeting.repository.MeetingRepository;
import com.dokdok.topic.service.PreOpinionAutoShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Meeting 상태 자동 업데이트 Scheduler
 * - 매 10분마다 실행
 * - meetingEndDate가 지난 CONFIRMED 상태의 Meeting을 DONE으로 변경
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingStatusScheduler {

    private final MeetingRepository meetingRepository;
    private final PreOpinionAutoShareService preOpinionAutoShareService;

    /**
     * 종료 시간이 지난 모임의 상태를 자동으로 DONE으로 변경
     * - 매 10분마다 실행 (0분, 10분, 20분, ...)
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void updateExpiredMeetings() {
        autoShareStartedMeetings();

        long startTime = System.currentTimeMillis();
        
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 종료 시간이 지난 CONFIRMED 상태의 Meeting 조회
        List<Meeting> expiredMeetings = meetingRepository
                .findByMeetingEndDateBeforeAndMeetingStatus(now, MeetingStatus.CONFIRMED);
        
        if (expiredMeetings.isEmpty()) {
            log.info("[Scheduler] No expired meetings found.");
            return;
        }
        
        // 2. 상태를 DONE으로 변경
        int count = 0;
        for (Meeting meeting : expiredMeetings) {
            try {
                meeting.changeStatus(MeetingStatus.DONE);
                count++;
            } catch (Exception e) {
                log.error("[Scheduler] Failed to update meeting {}: {}", meeting.getId(), e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 3. 성능 지표 로깅
        log.info("[Scheduler] ========================================");
        log.info("[Scheduler] Updated {} / {} meetings to DONE", count, expiredMeetings.size());
        log.info("[Scheduler] Execution time: {}ms", duration);
        log.info("[Scheduler] Throughput: {} meetings/sec", 
                 duration > 0 ? (count * 1000.0 / duration) : count);
        log.info("[Scheduler] ========================================");
    }

    private void autoShareStartedMeetings() {
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> startedMeetings = meetingRepository
                .findStartedMeetingsWithDraftPreOpinions(now, MeetingStatus.CONFIRMED);

        if (startedMeetings.isEmpty()) {
            log.info("[Scheduler] No started meetings for pre-opinion auto share.");
            return;
        }

        int submittedAnswerCount = 0;
        int submittedUserCount = 0;
        int appliedReviewCount = 0;

        for (Meeting meeting : startedMeetings) {
            try {
                PreOpinionAutoShareService.AutoShareResult result =
                        preOpinionAutoShareService.autoShareDrafts(meeting);
                submittedAnswerCount += result.submittedAnswerCount();
                submittedUserCount += result.submittedUserCount();
                appliedReviewCount += result.appliedReviewCount();
            } catch (Exception e) {
                log.error("[Scheduler] Failed to auto-share pre-opinions for meeting {}: {}",
                        meeting.getId(), e.getMessage());
            }
        }

        log.info("[Scheduler] Auto-shared pre-opinions for {} meetings: answers={}, users={}, reviews={}",
                startedMeetings.size(), submittedAnswerCount, submittedUserCount, appliedReviewCount);
    }
}
