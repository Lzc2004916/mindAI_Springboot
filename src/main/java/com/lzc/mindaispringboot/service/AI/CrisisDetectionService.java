package com.lzc.mindaispringboot.service.AI;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class CrisisDetectionService {
    private final static Set<String> CRISIS_KEYWORDS = Set.of(
            "不想活", "活着没意思", "自杀", "自残", "自伤",
            "结束生命", "解脱", "割腕", "跳楼", "投河",
            "不想坚持", "撑不下去", "告别", "遗书",
            "活着没用", "一了百了", "离开这个世界"
    );
    private static final Map<String,String> CRISIS_RESOURCES = Map.of(
            "national", "北京心理危机研究与干预中心：010-82951332（24小时）",
            "national2", "全国24小时心理危机干预热线：400-161-9995",
            "sms", "希望24热线：400-161-9995",
            "emergency", "如情况紧急，请立即拨打 120 或前往最近医院急诊"
    );
    public String delect(String userMessage){
        if (StrUtil.isBlank(userMessage)) return null;
        String msg = userMessage.trim();
        boolean hit = CRISIS_KEYWORDS.stream().anyMatch(msg::contains);
        if (!hit) return null;
        return "我听到你现在非常痛苦。你不是一个人，这些感受是可以被理解的。\n\n" +
                "但请一定相信，困难是暂时的，你值得被帮助。\n\n" +
                "请立即联系专业支持：\n" +
                "• " + CRISIS_RESOURCES.get("national") + "\n" +
                "• " + CRISIS_RESOURCES.get("national2") + "\n" +
                "• " + CRISIS_RESOURCES.get("emergency") + "\n\n" +
                "如果你身边有信任的人，请告诉他们你的感受。";
    }
}
