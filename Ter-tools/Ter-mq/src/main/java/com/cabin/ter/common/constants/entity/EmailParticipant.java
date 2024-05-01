package com.cabin.ter.common.constants.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 *     邮箱消息基类
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-05-01 16:40
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EmailParticipant implements MessageParticipant {
    /**
     * 消息主题
     */
    private String subject;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 附件地址
     */
    private String filePath;
    /**
     * 消息接收者
     */
    private String to;
}
