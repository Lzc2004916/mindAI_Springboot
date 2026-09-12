package com.lzc.mindaispringboot.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.AiService.PsychologicalSupportService;
import com.lzc.mindaispringboot.AiService.StructOutPut;
import com.lzc.mindaispringboot.common.ConsultaionStreamDTO;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Resource
    private PsychologicalSupportService psychologicalSupportService;
    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDto consultationSessionCreateDto, HttpServletRequest request){
        //获取当前用户
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        StructOutPut.StreamChatSession startSession = psychologicalSupportService.startSession(userId, consultationSessionCreateDto);
        return Result.success(startSession);
    }
    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultaionStreamDTO consultaionStreamDTO, HttpServletRequest request){
        //获取当前用户
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        if (userId == null || StringUtils.hasText(userId.toString())) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(
                            JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(),"用户未登录"))
                    )
                    .build()
            );
        }
        return null;
    }
}
