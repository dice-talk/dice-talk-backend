package com.example.dice_talk.report.mapper;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.report.dto.ChatReportDto;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.ChatReport;
import com.example.dice_talk.report.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    default List<Report> reportPostToReport(ReportDto.Post dto){
        if(dto == null) return List.of();

        List<Report> reports = new ArrayList<>();
        long reporterId = dto.getReporterId();
        List<ChatReportDto.Post> chatReportDtos = Optional.ofNullable(dto.getChatReports()).orElse(List.of());

        for(Long reportedMemberId : dto.getReportedMemberIds()) {
            Report report = new Report();
            report.setReason(dto.getReason());
            report.setReporterId(reporterId);
            report.setReportedMemberId(reportedMemberId);

            for (ChatReportDto.Post crDto : chatReportDtos){
                ChatReport cr = chatReportPostToChatReport(crDto);
                Chat chat = new Chat();
                chat.setChatId(crDto.getChatId());
                cr.setChat(chat);
                report.setChatReport(cr);
            }
            reports.add(report);
        }
        return reports;
    }

    default ReportDto.Response reportToReportResponse(Report report){
        ReportDto.Response response = new ReportDto.Response();
        response.setReportId(report.getReportId());
        response.setReporterId(report.getReporterId());
        response.setReportedChats(List.of());
        response.setReason(report.getReason());
        response.setReportStatus(report.getReportStatus());
        return response;
    }

    @Mapping(target = "chat.chatId", source = "chatId")
    ChatReport chatReportPostToChatReport(ChatReportDto.Post dto);

    @Mapping(target = "reportId", source = "report.reportId")
    ChatReportDto.Response chatReportToChatReportResponse(ChatReport chatReport);

    List<ReportDto.Response> reportsToReportResponses(List<Report> reports);
}
