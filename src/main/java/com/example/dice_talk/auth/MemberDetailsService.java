package com.example.dice_talk.auth;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Optional;

@Component
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final AuthorityUtils authorityUtils;

    public MemberDetailsService(MemberRepository memberRepository, AuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // email로 정보 조회
        Optional<Member> optionalMember = memberRepository.findByEmail(username);
        // 존재하지 않는 회원이면 예외
        Member findMember = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        // 정지 회원이면 예외 발생
        if(findMember.getMemberStatus() == Member.MemberStatus.MEMBER_BANNED){
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attr != null){
                attr.getRequest().setAttribute("exception", "BANNED");
            }
            throw new DisabledException("정지된 회원입니다.");
        }
        return new MemberDetails(findMember);
    }

    public final class MemberDetails extends Member implements UserDetails{

        public MemberDetails(Member member){
            setMemberId(member.getMemberId());
            setEmail(member.getEmail());
            setPassword(member.getPassword());
            setRoles(member.getRoles());
            setName(member.getName());
            setMemberStatus(member.getMemberStatus());
            setQuestions(member.getQuestions());
        }

        // 사용자의 역할 기반으로 권한 목록 생성 및 반환
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorityUtils.createAuthorities(this.getRoles());
        }

        @Override
        public String getUsername() {
            return getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        // 영구정지 회원의 접근 차단
        @Override
        public boolean isEnabled() {
            return this.getMemberStatus() != MemberStatus.MEMBER_BANNED;
        }
    }
}
