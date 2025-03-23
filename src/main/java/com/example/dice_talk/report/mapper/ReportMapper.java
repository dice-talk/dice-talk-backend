package com.example.dice_talk.report.mapper;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.report.dto.ChatReportDto;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.ChatReport;
import com.example.dice_talk.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    default Report reportPostToReport(ReportDto.Post dto){
        Report report = new Report();
        List<ChatReport> chatReports = dto.getChatReports().stream()
                .map(chatReportDto -> {
                    Chat chat = new Chat();
                    chat.setChatId(chatReportDto.getChatId());
                    ChatReport chatReport = chatReportPostToChatReport(chatReportDto);
                    chatReport.setReport(report);
                    chatReport.setChat(chat);
                    return chatReport;
                }).collect(Collectors.toList());
        report.setChatReports(chatReports);
        report.setReason(dto.getReason());
        return report;
    }
    ReportDto.Response reportToReportResponse(Report report);
    ChatReport chatReportPostToChatReport(ChatReportDto.Post dto);
    ChatReportDto.Response chatReportToChatReportResponse(ChatReport chatReport);
    List<ReportDto.Response> reportsToReportResponses(List<Report> reports);
}
