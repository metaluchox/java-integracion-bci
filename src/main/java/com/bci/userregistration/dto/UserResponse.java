package com.bci.userregistration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private Date created;
    private Date modified;

    @JsonProperty("last_login")
    private Date lastLogin;

    private String token;

    @JsonProperty("isactive")
    private Boolean isActive;
}
