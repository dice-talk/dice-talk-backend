package com.example.dice_talk.notice.mapper;

import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticeMapper {
   Notice noticePostToNotice(NoticeDto.Post post);
   Notice noticePatchToNotice(NoticeDto.Patch patch);
   NoticeDto.Response noticeToNoticeResponse(Notice notice);
   List<NoticeDto.Response> noticesToNoticeResponses(List<Notice> notices);
}
