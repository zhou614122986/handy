package com.finals.handy.controller;

import com.finals.handy.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author xiaoqiang
 * @date $(DATE)-$(TIME)
 */
@Controller
public class TaskController {
    @Autowired
    TaskService taskService;

    //    添加任务
    @ResponseBody
    @RequestMapping(value = "/user/addTask", method = RequestMethod.POST)
    public Map<String, Object> addTask(String accessToken, String name, String content, MultipartFile[] file) {
        return taskService.addTask(accessToken, name, content, file);
    }

    //获取任务的总数量
    @ResponseBody
    @RequestMapping(value = "/guest/findTaskCounts", method = RequestMethod.GET)
    public Map<String, Object> getTask() {
        return taskService.findTaskCounts();
    }

    //    查找n个任务
    @ResponseBody
    @RequestMapping(value = "/guest/getTasks", method = RequestMethod.GET)
    public Map<String, Object> getTasks(Integer n) {
        return taskService.getTasks(n);
    }

    //  修改任务 内容 名称
    @ResponseBody
    @RequestMapping(value = "/user/updateTask", method = RequestMethod.POST)
    public Map<String, Object> updateTask(String accessToken, Integer id, String name, String content) {

        return taskService.updateTask(accessToken,id, name, content);
    }

    //    举报任务
    @ResponseBody
    @RequestMapping(value = "/user/reportTask", method = RequestMethod.GET)
    public Map<String, Object> reportTask(String accessToken, String reason, Integer id) {
        return taskService.reportTask(accessToken, reason, id);
    }

    //    取消举报任务
    @ResponseBody
    @RequestMapping(value = "/user/cancelReportTask", method = RequestMethod.GET)
    public Map<String, Object> cancelReportTask(String accessToken,Integer id, Integer taskId) {
        return taskService.cancelReportTask(accessToken,id, taskId);
    }

    //    接受任务
    @ResponseBody
    @RequestMapping(value = "/user/acceptTask", method = RequestMethod.GET)
    public Map<String, Object> acceptTask(String accessToken, Integer id) {
        return taskService.acceptTask(accessToken, id);
    }


    //    放弃任务
    @ResponseBody
    @RequestMapping(value = "/user/cancelTask", method = RequestMethod.GET)
    public Map<String, Object> cancelTask(String accessToken, Integer id) {
        return taskService.cancelTask(accessToken, id);
    }

    //    完成任务
    @ResponseBody
    @RequestMapping(value = "/user/finishTask", method = RequestMethod.GET)
    public Map<String, Object> finishTask(String accessToken, Integer id) {
        return taskService.finishTask(accessToken, id);
    }
    //    删除任务
    @ResponseBody
    @RequestMapping(value = "/user/deleteTask", method = RequestMethod.GET)
    public Map<String, Object> deleteTask(String accessToken, Integer id) {
        return taskService.deleteTask(accessToken, id);
    }

    //    评论
    @ResponseBody
    @RequestMapping(value = "/user/commentTask", method = RequestMethod.POST)
    public Map<String, Object> commentTask(String accessToken, String content, Integer taskId) {
        return taskService.commentTask(accessToken, content, taskId);
    }

    //    举报评论
    @ResponseBody
    @RequestMapping(value = "/user/reportComment", method = RequestMethod.GET)
    public Map<String, Object> reportComment(String accessToken, Integer id, String reason) {
        return taskService.reportComment(accessToken, id, reason);
    }

    //    删除评论
    @ResponseBody
    @RequestMapping(value = "/user/deleteComment", method = RequestMethod.GET)
    public Map<String, Object> deleteComment(String accessToken,Integer id) {
        return taskService.deleteComment(accessToken,id);
    }

    //    获取n个评论
    @ResponseBody
    @RequestMapping(value = "/guest/getComments", method = RequestMethod.GET)
    public Map<String, Object> getComments(Integer n,Integer taskId) {
        return taskService.getComments(n,taskId);
    }


}
