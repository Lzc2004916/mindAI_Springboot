package com.lzc.mindaispringboot.controller;

import cn.hutool.json.JSONUtil;
import com.lzc.mindaispringboot.AiService.PsychologicalSupportService;
import com.lzc.mindaispringboot.AiService.StructOutPut;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.ConsultaionStreamDTO;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.response.ConsultationMessageResponseDTO;
import com.lzc.mindaispringboot.service.ConsultationMessageService;
import com.lzc.mindaispringboot.service.ConsultationSessionService;
import com.lzc.mindaispringboot.util.AuthUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
        //获取当前用户
        Long userId = Token_Aspect.getUserId();
        if (userId == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(
                            JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(),"用户未登录"))
                    )
                    .build()
            );
        }
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
                .delayElements(Duration.ofMillis(50));//添加延时确保流式数据体验
    }
    @GetToken
    @GetMapping("/sessions")
    public Result<List<ConsultationSession>> listSessions(@RequestParam(value = "userId",required = false) Long userId){
        Long currentUserId = Token_Aspect.getUserId();
        //如果是用户怎么样都能有值，只能看到自己的信息
        Long filterUserId = AuthUtil.isAdmin() ? userId : currentUserId;
        return Result.success(consultationSessionService.listSessions(filterUserId));
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