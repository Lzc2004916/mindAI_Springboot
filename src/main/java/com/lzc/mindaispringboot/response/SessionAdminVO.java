package com.lzc.mindaispringboot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAdminVO {
    private static final int PREVIEW_MAX = 60;
    /// 会话Id
    private Long id;
    /// 用户id
    private Long userId;
    /// 用户昵称
    private String userNickname;
    /// 会话标题
    private String sessionTitle;
    /// 最后一条消息内容(做列表用，已截断)
    private String lastMessageContent;
    /// 消息条数
    private Long messageCount;
    /// 最后一条消息时间
    private LocalDateTime lastMessageTime;
    /// 会话开始时间
    private LocalDateTime startedAt;
    /// 截断预览
    public static String preview(String content){
        if (content == null || content.isBlank()) return null;
        return content.length() <= PREVIEW_MAX ? content : content.substring(0, PREVIEW_MAX) + "...";
    }
}
