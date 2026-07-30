package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/ai")
@Api(tags = "ai助手接口")
@Slf4j
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * ai对话
     * @param request
     * @return
     */
    @PostMapping("/chat")
    @ApiOperation("ai对话")
    public Result<String> chat(@RequestBody Map<String, String> request){
        String userInput = request.get("userInput");
        if (userInput == null || userInput.trim().isEmpty()) {
            return Result.error("请输入问题");
        }
        String reply = aiService.chat(userInput);
        return Result.success(reply);
    }
}
