//package com.example.dice_talk.chatroom.queue;
//
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class ChatRoomQueue {
//    private final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
//
//    private final AtomicInteger queueSize = new AtomicInteger(0);
//
//    public void addToQueue(Long memberId) {
//        queue.offer(memberId);
//        queueSize.incrementAndGet();
//    }
//
//    public List<Long> removeFromQueue() {
//        List<Long> members = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            Long memberId = queue.poll();
//            if (memberId != null) {
//                members.add(memberId);
//                queueSize.decrementAndGet();
//            }
//        }
//        return members;
//    }
//
//    public boolean isQueueReady() {
//        return queueSize.get() >= 6;
//    }
//
//    public int getQueueSize() {
//        return queueSize.get();
//
//    }
//}
