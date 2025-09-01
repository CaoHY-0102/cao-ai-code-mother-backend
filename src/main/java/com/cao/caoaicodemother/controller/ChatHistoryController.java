package com.cao.caoaicodemother.controller;

import com.cao.caoaicodemother.annotation.AuthCheck;
import com.cao.caoaicodemother.common.BaseResponse;
import com.cao.caoaicodemother.common.ResultUtils;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.cao.caoaicodemother.model.entity.ChatHistory;
import com.cao.caoaicodemother.service.ChatHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史对话 控制层。
 *
 * @author 小曹同学
 */
@Tag(name = "对话历史")
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页获取某个应用的对话历史
     *
     * @param appId 应用ID
     * @param lastCreateTime 最后一条记录的创建时间
     * @param pageSize 每页数量
     * @param request 请求
     * @return
     */
    @Operation(summary = "分页获取某个应用的对话历史")
    @PostMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 查询数据
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listChatHistoryByPage(appId, pageSize, loginUser, lastCreateTime);
        return ResultUtils.success(chatHistoryPage);
    }


    /**
     * 管理员分页获取所有对话历史
     *
     * @param chatHistoryQueryRequest 查询参数
     * @return 对话历史分页
     */
    @Operation(summary = "管理员分页获取所有对话历史")
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(chatHistoryQueryRequest.getPageNum(), chatHistoryQueryRequest.getPageSize()), queryWrapper);
        return ResultUtils.success(result);


    }

}
