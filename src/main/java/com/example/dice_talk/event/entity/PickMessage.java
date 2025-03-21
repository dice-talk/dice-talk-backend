package com.example.dice_talk.event.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MESSAGE")
public class PickMessage extends RoomEvent {
    private String message;
}
