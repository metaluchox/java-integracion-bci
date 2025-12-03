package com.bci.userregistration.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PhoneDTO {

    @NotBlank(message = "El número es obligatorio")
    private String number;

    @NotBlank(message = "El código de ciudad es obligatorio")
    private String citycode;

    @NotBlank(message = "El código de país es obligatorio")
    private String contrycode;
}
