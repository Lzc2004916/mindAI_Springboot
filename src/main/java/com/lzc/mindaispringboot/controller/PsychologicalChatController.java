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
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Resource
    private PsychologicalSupportService psychologicalSupportService;
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
}
