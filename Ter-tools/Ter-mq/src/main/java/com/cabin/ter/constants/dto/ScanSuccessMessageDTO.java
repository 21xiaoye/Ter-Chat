package com.cabin.ter.constants.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 *     扫码成功对象，推送给用户的消息对象
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-05-11 11:31
 */
@Data
public class ScanSuccessMessageDTO extends MQBaseMessage implements Serializable {
    /**
     * 推送的uid
     */
    private Integer loginCode;
    private String openId;

}
