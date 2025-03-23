package com.example.dice_talk.member.entity;

import com.example.dice_talk.item.entity.Item;
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
public class DiceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String logType;

    @Column(nullable = false)
    private String info;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

//    @ManyToOne
//    @JoinColumn(name = "productId")
//    private Product product;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
}
