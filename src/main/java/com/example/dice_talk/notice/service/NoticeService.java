package com.example.dice_talk.notice.service;

import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.dashboard.dto.DashboardNotice;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.entity.NoticeImage;
import com.example.dice_talk.notice.event.NoticeCreatedEvent;
import com.example.dice_talk.notice.repository.NoticeImageRepository;
import com.example.dice_talk.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final S3Uploader s3Uploader;
    private final ApplicationEventPublisher publisher;
    private final MemberService memberService;

    public Notice createNotice(NoticeDto.Post postDto, List<MultipartFile> imageFiles, List<Boolean> thumbnailFlags) throws IOException {
        Notice notice = new Notice();
        notice.setTitle(postDto.getTitle());
        notice.setContent(postDto.getContent());
        notice.setStartDate(postDto.getStartDate());
        notice.setEndDate(postDto.getEndDate());
        notice.setNoticeType(postDto.getNoticeType());
        notice.setNoticeStatus(postDto.getNoticeStatus());
        notice.setNoticeImportance(postDto.getNoticeImportance());

        if (imageFiles != null && !imageFiles.isEmpty()) {
            if (thumbnailFlags == null || thumbnailFlags.size() != imageFiles.size()) {
                throw new BusinessLogicException(ExceptionCode.INVALID_IMAGE_METADATA);
            }

            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                boolean isThumbnail = thumbnailFlags.get(i);

                String imageUrl = s3Uploader.upload(file, "notice-image");

                NoticeImage image = new NoticeImage();
                image.setImageUrl(imageUrl);
                image.setThumbnail(isThumbnail);
                image.setNotice(notice);
                notice.getImages().add(image);
            }
        }
        Notice created = noticeRepository.save(notice);
        // 이벤트 발행 (전체 멤버)
        publisher.publishEvent(new NoticeCreatedEvent(created.getNoticeId(), notice.getTitle()));

        return created;
    }

    @Transactional
    public Notice updateNotice(NoticeDto.Patch patchDto, List<MultipartFile> imageFiles, List<Boolean> thumbnailFlags) throws IOException {
        Notice findNotice = findVerifiedNotice(patchDto.getNoticeId());

        Optional.ofNullable(patchDto.getNoticeType()).ifPresent(value -> findNotice.setNoticeType(value));
        Optional.ofNullable(patchDto.getTitle()).ifPresent(value -> findNotice.setTitle(value));
        Optional.ofNullable(patchDto.getContent()).ifPresent(value -> findNotice.setContent(value));
        Optional.ofNullable(patchDto.getStartDate()).ifPresent(value -> findNotice.setStartDate(value));
        Optional.ofNullable(patchDto.getEndDate()).ifPresent(value -> findNotice.setEndDate(value));
        Optional.ofNullable(patchDto.getNoticeStatus()).ifPresent(value -> findNotice.setNoticeStatus(value));
        Optional.ofNullable(patchDto.getNoticeImportance()).ifPresent(value -> findNotice.setNoticeImportance(value));

        // 유지할 이미지 ID 목록
        List<Long> keepImageIds = Optional.ofNullable(patchDto.getKeepImageIds())
                .orElse(List.of());

        // 삭제 대상 고르기
        List<NoticeImage> currentImages = new ArrayList<>(findNotice.getImages());
        for (NoticeImage image : currentImages) {
            if (!keepImageIds.contains(image.getNoticeImageId())) {
                s3Uploader.moveToDeletedFolder(image.getImageUrl(), "deleted-notice-image");
                noticeImageRepository.delete(image);
                findNotice.getImages().remove(image);
            }
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            if (thumbnailFlags == null || thumbnailFlags.size() != imageFiles.size()) {
                throw new BusinessLogicException(ExceptionCode.INVALID_IMAGE_METADATA);
            }

            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                boolean isThumbnail = thumbnailFlags.get(i);

                String imageUrl = s3Uploader.upload(file, "notice-image");

                NoticeImage image = new NoticeImage();
                image.setImageUrl(imageUrl);
                image.setThumbnail(isThumbnail);
                image.setNotice(findNotice);
                findNotice.getImages().add(image);
            }

        }

        return noticeRepository.save(findNotice);
    }

    public Notice findNotice(long noticeId) {
        return findVerifiedNotice(noticeId);
    }

    public Page<Notice> findNotices(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        }
        return noticeRepository.findAll(PageRequest.of(page - 1, size, Sort.by("createdAt").descending()));
    }

    public void deleteNotice(long noticeId) {
        Notice notice = findVerifiedNotice(noticeId);
        notice.setNoticeStatus(Notice.NoticeStatus.CLOSED);
        noticeRepository.save(notice);
    }

    public Notice findVerifiedNotice(long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTICE_NOT_EXIST));
    }

    public List<Notice> findBannerEvents() {
        return noticeRepository.findAllByNoticeTypeAndNoticeStatus(
                Notice.NoticeType.EVENT, Notice.NoticeStatus.ONGOING);
    }

    //adminWeb : 최신 등록된 공지글 정보 반환
    public List<DashboardNotice> findRecentNotices() {
        //최근 등록되 공지글 1개 내림차순으로 조회
        List<Notice> recentNotice = noticeRepository.findTop1ByOrderByCreatedAtDesc();
        List<Notice> noticeList = noticeRepository.findAllByNoticeTypeAndNoticeStatus(Notice.NoticeType.EVENT, Notice.NoticeStatus.ONGOING );

        return recentNotice.stream()
                .map(notice -> new DashboardNotice(notice.getTitle(), noticeList.size()))
                .collect(Collectors.toList());

    }

}


