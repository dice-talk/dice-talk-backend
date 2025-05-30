package com.example.dice_talk.dicelog.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiceLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    private LogType logType = LogType.DICE_CHARGE;

    @Column(nullable = false)
    private String info;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    public enum LogType {
        DICE_CHARGE("다이스 충전"),
        DICE_USED("다이스 사용");

        @Getter
        private String status;

        LogType(String status){
            this.status = status;
        }
    }
}
