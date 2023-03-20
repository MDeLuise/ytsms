package com.github.mdeluise.ytsms.authentication.payload.response;

import com.github.mdeluise.ytsms.security.jwt.JwtTokenInfo;

public record UserInfoResponse(long id, String username, JwtTokenInfo jwt) {
}
