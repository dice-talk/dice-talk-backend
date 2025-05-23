package com.example.dice_talk.notice.mapper;

import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.dto.NoticeImageDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.entity.NoticeImage;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticeMapper {
   default Notice noticePostToNotice(NoticeDto.Post postDto){
      if (postDto == null) return null;

      Notice notice = new Notice();
      notice.setTitle(postDto.getTitle());
      notice.setContent(postDto.getContent());
      notice.setStartDate(postDto.getStartDate());
      notice.setEndDate(postDto.getEndDate());
      notice.setNoticeType(postDto.getNoticeType());
      notice.setNoticeStatus(postDto.getNoticeStatus());
      notice.setNoticeImportance(postDto.getNoticeImportance());

      return notice;
   }
   default Notice noticePatchToNotice(NoticeDto.Patch patchDto) {
      if (patchDto == null) return null;

      Notice notice = new Notice();
      notice.setNoticeId(patchDto.getNoticeId());
      notice.setTitle(patchDto.getTitle());
      notice.setContent(patchDto.getContent());
      notice.setStartDate(patchDto.getStartDate());
      notice.setEndDate(patchDto.getEndDate());
      notice.setNoticeType(patchDto.getNoticeType());
      notice.setNoticeStatus(patchDto.getNoticeStatus());
      notice.setNoticeImportance(patchDto.getNoticeImportance());

      return notice;
   }
   default NoticeDto.Response noticeToNoticeResponse(Notice notice) {
      if (notice == null) return null;

      List<NoticeImageDto.Response> imageDtos = new ArrayList<>();
      if (notice.getImages() != null) {
         for (NoticeImage image : notice.getImages()) {
            imageDtos.add(new NoticeImageDto.Response(
                    image.getNoticeImageId(),
                    notice.getNoticeId(),
                    image.getImageUrl(),
                    image.isThumbnail()
            ));
         }
      }

      return new NoticeDto.Response(
              notice.getNoticeId(),
              notice.getTitle(),
              notice.getContent(),
              imageDtos,
              notice.getStartDate(),
              notice.getEndDate(),
              notice.getNoticeType(),
              notice.getNoticeStatus(),
              notice.getNoticeImportance(),
              notice.getCreatedAt(),
              notice.getModifiedAt()
      );
   }
   default List<NoticeDto.Response> noticesToNoticeResponses(List<Notice> notices) {
      if (notices == null) return null;

      List<NoticeDto.Response> result = new ArrayList<>();
      for (Notice notice : notices) {
         result.add(noticeToNoticeResponse(notice));
      }
      return result;
   }
}
