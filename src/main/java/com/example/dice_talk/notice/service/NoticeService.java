package com.example.dice_talk.notice.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.entity.NoticeImage;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.repository.NoticeImageRepository;
import com.example.dice_talk.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;

    public NoticeService(NoticeRepository noticeRepository, NoticeImageRepository noticeImageRepository) {
        this.noticeRepository = noticeRepository;
        this.noticeImageRepository = noticeImageRepository;
    }

    //notice 등록
    public Notice createNotice(Notice notice) {
        if(notice.getImages() != null){
            for (NoticeImage image : notice.getImages()){
                notice.setImage(image);
            }
        }
        //notice 등록 후 반환
        return noticeRepository.save(notice);
    }

    //notice 수정
    public Notice updateNotice(Notice notice) {
        Notice findNotice = findVerifiedNotice(notice.getNoticeId());
        // 일반 필드 없데이트
        Optional.ofNullable(notice.getNoticeType())
                .ifPresent(noticeType -> findNotice.setNoticeType(noticeType));
        Optional.ofNullable(notice.getTitle())
                .ifPresent(title -> findNotice.setTitle(title));
        Optional.ofNullable(notice.getContent())
                .ifPresent(content -> findNotice.setContent(content));
        Optional.ofNullable(notice.getStartDate())
                .ifPresent(startDate -> findNotice.setStartDate(startDate));
        Optional.ofNullable(notice.getEndDate())
                .ifPresent(endDate -> findNotice.setEndDate(endDate));
        Optional.ofNullable(notice.getNoticeStatus())
                .ifPresent(noticeStatus -> findNotice.setNoticeStatus(noticeStatus));
        Optional.ofNullable(notice.getNoticeImportance())
                .ifPresent(noticeImportance -> findNotice.setNoticeImportance(noticeImportance));

        // 이미지 처리
        List<NoticeImage> uploadImages = notice.getImages();

        if(uploadImages != null){
            // 들어온 이미지 중 기존 이미지는 남김
            List<Long> uploadImagesIds = uploadImages.stream()
                    .map(noticeImage -> noticeImage.getNoticeImageId())
                    .filter(noticeImageId -> noticeImageId != null)
                    .collect(Collectors.toList());

            // 기존 이미지 중 삭제된 이미지 제거
            // 🚩 S3 버킷 생성 시 삭제는 따로 이관해서 보관하기 toRemove만 옮기면 됨.
            List<NoticeImage> toRemove = findNotice.getImages().stream()
                    .filter(noticeImage -> noticeImage.getNoticeImageId() != null &&
                            !uploadImagesIds.contains(noticeImage.getNoticeImageId()))
                    .collect(Collectors.toList());

            toRemove.forEach(noticeImage -> findNotice.getImages().remove(noticeImage));

            // 새로운 이미지는 추가
            for (NoticeImage image : uploadImages){
                if(image.getNoticeImageId() == null){
                    findNotice.setImage(image);
                }
            }
        }
        //수정사항 저장 후 반환
        return noticeRepository.save(findNotice);
    }

    //notice 단일 조회
    public Notice findNotice(long noticeId) {
        return findVerifiedNotice(noticeId);
    }

    //notice 전체 목록 조회
    public Page<Notice> findNotices(int page, int size) {
        //page 번호검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return noticeRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending()));
    }

    //notice 삭제: 상태변경
    public void deleteNotice(long noticeId) {
        Notice findNotice = findVerifiedNotice(noticeId);
        findNotice.setNoticeStatus(Notice.NoticeStatus.CLOSED);
        // 변경사항 저장
        noticeRepository.save(findNotice);
    }

    //검증로직 : noticeId로 DB 조회, 없으면 예외처리
    public Notice findVerifiedNotice(long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTICE_NOT_EXIST)
        );
    }

    // 배너에 띄울 이벤트 반환하는 메서드
    public List<Notice> findBannerEvents() {
        return noticeRepository.findAllByNoticeTypeAndNoticeStatus(Notice.NoticeType.EVENT, Notice.NoticeStatus.ONGOING);
    }

}
