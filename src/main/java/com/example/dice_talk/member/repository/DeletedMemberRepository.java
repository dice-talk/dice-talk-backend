package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.DeletedMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {

}
