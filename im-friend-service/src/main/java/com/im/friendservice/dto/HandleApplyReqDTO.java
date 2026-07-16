package com.im.friendservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleApplyReqDTO {
    @NotNull(message = "申请单ID不能为空")
    private Long applyId;

    @NotNull(message = "处理状态不能为空")
    @Min(value = 1, message = "非法的状态参数") // 1-已同意
    @Max(value = 2, message = "非法的状态参数") // 2-已拒绝
    private Integer status;
}
