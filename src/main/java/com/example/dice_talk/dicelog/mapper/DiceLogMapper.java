package com.example.dice_talk.dicelog.mapper;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.dicelog.dto.DiceLogDto;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.product.entity.Product;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DiceLogMapper {
    default DiceLog diceLogPostToDiceLog (DiceLogDto.Post dto){
        DiceLog diceLog = new DiceLog();
        diceLog.setQuantity(dto.getQuantity());
        diceLog.setLogType(dto.getLogType());
        diceLog.setInfo(dto.getInfo());
        Member member = new Member();
        member.setMemberId(dto.getMemberId());
        diceLog.setMember(member);
        if(dto.getLogType().equals(DiceLog.LogType.DICE_CHARGE)){
            Product product = new Product();
            product.setProductId(dto.getProductId());
            diceLog.setProduct(product);
        } else if(dto.getLogType().equals(DiceLog.LogType.DICE_USED)){
            Item item = new Item();
            item.setItemId(dto.getItemId());
            diceLog.setItem(item);
        } else {
            throw new BusinessLogicException(ExceptionCode.LOG_TYPE_INVALID);
        }
        return diceLog;
    }

    default DiceLogDto.Response diceLogToDiceLogResponse(DiceLog diceLog){
        DiceLogDto.Response response = new DiceLogDto.Response();
        response.setLogId(diceLog.getLogId());
        response.setQuantity(diceLog.getQuantity());
        response.setLogType(diceLog.getLogType());
        response.setInfo(diceLog.getInfo());
        response.setMemberId(diceLog.getMember().getMemberId());
        response.setCreatedAt(diceLog.getCreatedAt());
        if(diceLog.getLogType().equals(DiceLog.LogType.DICE_CHARGE)){
            response.setProductId(diceLog.getProduct().getProductId());
        } else {
            response.setItemId(diceLog.getItem().getItemId());
        }
        return response;
    }

    default List<DiceLogDto.Response> diceLogsToDiceLogResponses(List<DiceLog> logs){
        List<DiceLogDto.Response> responses = new ArrayList<>();
        logs.stream().forEach(diceLog -> {
            responses.add(diceLogToDiceLogResponse(diceLog));
        });
        return responses;
    }
}
