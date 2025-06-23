package com.example.dice_talk.dicelog.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.event.ItemUsedEvent;
import com.example.dice_talk.item.service.ItemService;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.dicelog.repository.DiceLogRepository;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.service.ProductService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DiceLogService {
    private final DiceLogRepository diceLogRepository;
    private final ProductService productService;
    private final ItemService itemService;
    private final MemberService memberService;
    private final ApplicationEventPublisher publisher;

    public DiceLogService(DiceLogRepository diceLogRepository, ProductService productService, ItemService itemService, MemberService memberService, ApplicationEventPublisher publisher) {
        this.diceLogRepository = diceLogRepository;
        this.productService = productService;
        this.itemService = itemService;
        this.memberService = memberService;
        this.publisher = publisher;
    }

    @Transactional
    public DiceLog createDiceLogCharge(DiceLog diceLog, long memberId) {
        Member member = memberService.findVerifiedMember(memberId);
        
        // 다이스 수량 업데이트
        int newTotalDice = member.getTotalDice() + diceLog.getQuantity();
        member.setTotalDice(newTotalDice);
        
        // 로그 정보 설정
        diceLog.setLogType(DiceLog.LogType.DICE_CHARGE);
        diceLog.setMember(member);
        member.setDiceLog(diceLog);
        
        return diceLogRepository.save(diceLog);
    }

    @Transactional
    public DiceLog createDiceLogUsed(DiceLog diceLog, long memberId) {
        Item item = itemService.findVerifiedItem(diceLog.getItem().getItemId());
        Member member = memberService.findVerifiedMember(memberId);
        diceLog.setInfo(item.getItemName());
        diceLog.setLogType(DiceLog.LogType.DICE_USED);
        diceLog.setMember(member);
        diceLog.setQuantity(item.getDicePrice());
        if (member.getTotalDice() < item.getDicePrice()) {
            throw new BusinessLogicException(ExceptionCode.NOT_ENOUGH_DICE);
        }
        member.setDiceLog(diceLog);
        member.setTotalDice(member.getTotalDice() - diceLog.getQuantity());
        publisher.publishEvent(new ItemUsedEvent(memberId, item.getItemName(), item.getDicePrice()));
        return diceLogRepository.save(diceLog);
    }

    public Page<DiceLog> findDiceLogs(int page, int size, long memberId) {
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        memberService.findVerifiedMember(memberId);
        // 각 member에 해당하는 DiceLog 찾기
        Page<DiceLog> diceLogPage = diceLogRepository.findAllByMember_MemberId(memberId, PageRequest.of(page -1, size, Sort.by("logId").descending()));
        return diceLogPage;
    }

    // 관리자용 DiceLog 전체 조회
    public Page<DiceLog> findAllDiceLogs(int page, int size){
        AuthorizationUtils.verifyAdmin();
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return diceLogRepository.findAll(PageRequest.of(page -1, size, Sort.by("logId").descending()));
    }

    // 충전 취소
    @Transactional
    public DiceLog cancelDiceLogCharge(long diceLogId, long memberId) {
        DiceLog findDiceLog = diceLogRepository.findById(diceLogId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DICE_LOG_NOT_FOUND));
        if (findDiceLog.getMember().getMemberId() != memberId) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        DiceLog diceLog = new DiceLog();
        if (!findDiceLog.getLogType().equals(DiceLog.LogType.DICE_CHARGE)) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_CANCEL);
        }
        diceLog.setLogType(DiceLog.LogType.DICE_CHARGE);
        diceLog.setProduct(findDiceLog.getProduct());
        diceLog.setInfo(findDiceLog.getInfo() + " (취소)");
        diceLog.setMember(findDiceLog.getMember());
        diceLog.setQuantity(findDiceLog.getQuantity() - (findDiceLog.getQuantity() * 2));
        findDiceLog.getMember().setDiceLog(diceLog);
        int totalDice = findDiceLog.getMember().getTotalDice() +findDiceLog.getQuantity();
        findDiceLog.getMember().setTotalDice(totalDice);
        return diceLog;
    }

    //웹페이지 : 일일 아이템 사용 건 수 조회
    public int countItemUsesToday() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        return diceLogRepository.countByCreatedAtBetweenAndType(todayStart, todayEnd, DiceLog.LogType.DICE_USED);
    }
}