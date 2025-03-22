package com.example.dice_talk.report.mapper;

import com.example.dice_talk.report.dto.ChatReportDto;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.ChatReport;
import com.example.dice_talk.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    Report reportPostToReport(ReportDto.Post dto);
    ReportDto.Response reportToReportResponse(Report report);
    ChatReportDto.Post chatReportPostToChatReport(ChatReport chatReport);
    ChatReportDto.Response chatReportToChatReportResponse(ChatReport chatReport);
    List<ReportDto.Response> reportsToReportResponses(List<Report> reports);
}
