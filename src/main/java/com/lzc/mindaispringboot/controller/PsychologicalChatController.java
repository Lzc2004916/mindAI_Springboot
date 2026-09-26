package com.lzc.mindaispringboot.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.AiService.PsychologicalSupportService;
import com.lzc.mindaispringboot.AiService.StructOutPut;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.ConsultaionStreamDTO;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.common.Dto.SessionPageQuery;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.response.ConsultationMessageResponseDTO;
import com.lzc.mindaispringboot.response.SessionAdminVO;
import com.lzc.mindaispringboot.service.ConsultationMessageService;
import com.lzc.mindaispringboot.service.ConsultationSessionService;
import com.lzc.mindaispringboot.util.AuthUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Resource
    private PsychologicalSupportService psychologicalSupportService;
    @Resource
    private ConsultationMessageService consultationMessageService;
    @Resource
    private ConsultationSessionService consultationSessionService;
    @GetToken
    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDto consultationSessionCreateDto){
        //获取当前用户
        Long userId = Token_Aspect.getUserId();
        StructOutPut.StreamChatSession startSession = psychologicalSupportService.startSession(userId, consultationSessionCreateDto);
        return Result.success(startSession);
    }
    @GetToken
    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultaionStreamDTO consultaionStreamDTO){
        // ⚠️ 这个接口**不能抛异常出去**。
        //    它的返回值是 SSE 流，而调用方带的是 `Accept: text/event-stream`；
        //    一旦异常冒到全局异常处理器，处理器想返回 JSON 的 Result，会被内容协商拒绝：
        //      HttpMediaTypeNotAcceptableException: No acceptable representation
        //    → 异常处理器自己失败 → 客户端只拿到 **HTTP 500 + 空 body**，看不到任何原因
        //      （服务端还会刷一大段堆栈，看起来像鉴权/过滤器出错）。
        //    所以这里所有失败都转成 SSE 的 `error` 事件帧返回，前端能直接展示真正的错误文案。
        try {
            //获取当前用户
            Long userId = Token_Aspect.getUserId();
            if (userId == null) {
                return errorStream(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
            }
            // 会话归属校验：非管理员只能往自己的会话里发消息（防越权写入）
            Long dbSessionId = extractDbSessionId(consultaionStreamDTO.getSessionId());
            checkOwnership(dbSessionId);
            //开始流式对话
            return psychologicalSupportService.streamPsychologicalChat(consultaionStreamDTO.getSessionId(),consultaionStreamDTO.getUserMessage())
                    .map(Fragment ->{
                        return ServerSentEvent.<String>builder()
                                .event("message")
                                .data(JSONUtil.toJsonStr(Result.success(Map.of("content",Fragment,"type","normal"))))
                                .build();
                    })
                    .concatWith(Flux.just(ServerSentEvent.<String>builder()
                            .event("done")
                            .data("{}")
                            .build()
                    ))
                    // 流「中途」出错（AI 超时 / 限流 / 网络中断…）同样转成 error 帧
                    .onErrorResume(e -> errorStream(ResultCode.BUSINESS_ERROR.getCode(), describe(e)))
                    .delayElements(Duration.ofMillis(50));//添加延时确保流式数据体验
        } catch (Exception e) {
            return errorStream(ResultCode.BUSINESS_ERROR.getCode(), describe(e));
        }
    }

    /** 构造一个只含 `error` 事件的最小 SSE 流（客户端按 event=error 处理） */
    private Flux<ServerSentEvent<String>> errorStream(String code, String message){
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data(JSONUtil.toJsonStr(Result.error(code, message, null)))
                .build()
        );
    }

    /** 取出可以给用户看的错误信息：业务异常用它自己的话，其它一律给通用文案（不泄露内部细节） */
    private String describe(Throwable e){
        if (e instanceof BusionessException) return e.getMessage();
        log.warn("流式对话异常：{}", e.toString());
        return "AI回复失败，请稍后重试";
    }
    @GetToken
    @GetMapping("/sessions")
    public Result<Page<SessionAdminVO>> listSessions(SessionPageQuery query){
        Long currentUserId = Token_Aspect.getUserId();
        /// 非管理员强制覆盖 userId 为自己，就算前端传了别人的 id 也无效
        if (!AuthUtil.isAdmin()){
            query.setUserId(currentUserId);
        }
        return Result.success(consultationSessionService.pageSessions(query));
    }
    @GetToken
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> listMessages(@PathVariable String sessionId){
        Long dbSessionId = extractDbSessionId(sessionId);
        checkOwnership(dbSessionId);
        return Result.success(consultationMessageService.listBySession(dbSessionId));
    }
    @GetToken
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId){
        Long dbSessionId = extractDbSessionId(sessionId);
        checkOwnership(dbSessionId);
        consultationSessionService.deleteSession(dbSessionId);
        return  Result.success();
    }
    @GetToken
    @GetMapping("/session/{sessionId}/emotion")
    public Result<StructOutPut.EmotionAnalysis> sessionEmotion(@PathVariable String sessionId){
        Long userId = Token_Aspect.getUserId();
        StructOutPut.EmotionAnalysis resultEmotion = psychologicalSupportService.getEmotionAnalysis(sessionId, userId, AuthUtil.isAdmin());
        return Result.success(resultEmotion);
    }
    /// 安全校验
    private Long extractDbSessionId(String sessionId){
        try {
            String s = sessionId.startsWith("session_")
                    ? sessionId.substring("session_".length()) : sessionId;
            return Long.parseLong(s);
        }catch (Exception e){
            throw new BusionessException("会话ID格式错误");
        }
    }
    private void checkOwnership(Long sessionId){
        if (AuthUtil.isAdmin()) return;
        ConsultationSession session = consultationSessionService.getById(sessionId);
        if (session == null) throw new BusionessException("会话不存在");
        if (!session.getUserId().equals(Token_Aspect.getUserId())) throw new BusionessException("无权操作该会话");
    }

}