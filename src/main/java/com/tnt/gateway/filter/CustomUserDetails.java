package com.tnt.gateway.filter;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

	private final Long memberId;
	private final String username;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(Long memberId, String username, Collection<? extends GrantedAuthority> authorities) {
		this.memberId = memberId;
		this.username = username;
		this.authorities = authorities;
	}

	public Long getMemberId() {
		return memberId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
