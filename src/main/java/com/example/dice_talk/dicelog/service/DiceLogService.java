package com.example.dice_talk.dicelog.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.service.ItemService;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.dicelog.repository.DiceLogRepository;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.service.ProductService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiceLogService {
    private final DiceLogRepository diceLogRepository;
    private final ProductService productService;
    private final ItemService itemService;
    private final MemberService memberService;

    public DiceLogService(DiceLogRepository diceLogRepository, ProductService productService, ItemService itemService, MemberService memberService) {
        this.diceLogRepository = diceLogRepository;
        this.productService = productService;
        this.itemService = itemService;
        this.memberService = memberService;
    }

    @Transactional
    public DiceLog createDiceLogCharge(DiceLog diceLog, long memberId) {
        Product product = productService.findVerifiedProduct(diceLog.getProduct().getProductId());
        Member member = memberService.findVerifiedMember(memberId);
        diceLog.setInfo(product.getProductName());
        diceLog.setLogType(DiceLog.LogType.DICE_CHARGE);
        diceLog.setMember(member);
        diceLog.setQuantity(product.getQuantity());
        member.setDiceLog(diceLog);
        member.setTotalDice(member.getTotalDice() + diceLog.getQuantity());
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
        return diceLogRepository.save(diceLog);
    }

    public Page<DiceLog> findDiceLogs(int page, int size, long memberId) {
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        memberService.findVerifiedMember(memberId);
        // 각 member에 해당하는 DiceLog 찾기
        Page<DiceLog> diceLogPage = diceLogRepository.findAllByMember_MemberId(memberId, PageRequest.of(page, size, Sort.by("logId").descending()));
        return diceLogPage;
    }

    // 관리자용 DiceLog 전체 조회
    public Page<DiceLog> findAllDiceLogs(int page, int size){
        AuthorizationUtils.verifyAdmin();
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return diceLogRepository.findAll(PageRequest.of(page, size, Sort.by("logId").descending()));
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
}
