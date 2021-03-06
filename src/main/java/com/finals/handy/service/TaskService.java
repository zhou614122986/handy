package com.finals.handy.service;

import com.auth0.jwt.interfaces.Claim;
import com.finals.handy.bean.Comment;
import com.finals.handy.bean.Report;
import com.finals.handy.bean.Task;
import com.finals.handy.config.ServerConfig;
import com.finals.handy.mapper.CommentMapper;
import com.finals.handy.mapper.ImgMapper;
import com.finals.handy.mapper.ReportMapper;
import com.finals.handy.mapper.TaskMapper;
import com.finals.handy.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xiaoqiang
 * @date $(DATE)-$(TIME)
 */
@Service
public class TaskService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    ImgMapper imgMapper;
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private CommentMapper commentMapper;

    private static final String path ="/root/handy/user/taskImg/";//"d:/taskImg/";

    @Transactional
    public Map<String, Object> addTask(String accessToken, String name, String content, MultipartFile[] files) {
        Map<String, Object> map = null;
        Task task = new Task();

        Integer userId = getId(accessToken);


        task.setPublishId(userId);
        task.setName(name);
        task.setContent(content);
        task.setAcceptId(0);
        String time = getTime();
        task.setTime(time);
        task.setIsFinish(0);
        task.setIsReport(0);
        if (!taskMapper.addTask(task)) {
            map = new HashMap<>();
            map.put("code", -1);
            throw new RuntimeException();
        }
        if (files == null) {
            map.put("msg", "文件为空没有图片");
        } else {
            map = dealImg(files, task);

        }
        return map;
    }
    @Transactional
    public Map<String, Object> dealImg(MultipartFile[] files, Task task) {
        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList();
        Integer taskId = task.getId();
        for (MultipartFile multipartFile : files) {
            if (!multipartFile.isEmpty()) {
                try {
                    byte[] bytes = multipartFile.getBytes();
                    String fileName = multipartFile.getOriginalFilename();
//                使用UUID给图片重命名
                    String uuid = UUID.randomUUID().toString().replaceAll("-", "");
//                获取文件的扩展名
                    String ext = fileName.substring(fileName.lastIndexOf("."), fileName.length());

                    String newName = uuid + ext;
                    System.out.println("fileName:" + fileName);
                    System.out.println(newName);
                    String imgPath = path + newName;
                    System.out.println(imgPath);
                    File file1 = new File(imgPath);
                    list.add(imgPath);
                    imgMapper.addImgPath(taskId, newName);
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file1));
                    outputStream.write(bytes);
                    outputStream.close();

                } catch (IOException e) {
                    map.put("code", -1);
                    return map;
                }


            }
        }
        task.setImgsPath(list);
        map.put("code", 0);
        return map;
    }

    @Transactional
    public String getURL() {
        return serverConfig.getUrl();
    }

    public Map<String, Object> findTaskCounts() {
        Integer num = taskMapper.findCounts();
        Map<String, Object> map = new HashMap<>();

        map.put("num", num);
        map.put("code", 0);
        return map;
    }

    @Transactional
    public Map<String, Object> getTasks(Integer n) {
        Map<String, Object> map = new HashMap<>();
        List<Task> tasks = taskMapper.findTasks(n);
        for (Task task : tasks) {
            List<String> imgPath = imgMapper.getImgPath(task.getId());
            task.setImgsPath(imgPath);
        }
        map.put("tasks", tasks);
        map.put("code", 0);
        return map;
    }

    @Transactional
    public Map<String, Object> updateTask(String accessToken, Integer id, String name, String content) {
        Map<String, Object> map = new HashMap<>();

        Integer id1 = getId(accessToken);
        Task taskById = taskMapper.findTaskById(id);
//        System.out.println(id1);
//        System.out.println(taskById);

        if (taskById.getPublishId() != id1) {
            map.put("code", 1);
            return map;
        }
        taskById.setName(name);
        taskById.setContent(content);
        taskMapper.updateTask(taskById);

        map.put("code", 0);
        return map;

    }

    @Transactional
    public Map<String, Object> reportTask(String accessToken, String reason, Integer id) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);
        Report report = new Report();
        report.setReason(reason);
        report.setReportId(userId);
        report.setTime(getTime());
        reportMapper.addReport(report);
        System.out.println(report.getId());  //4

        taskMapper.reportTask(id,report.getId() );
        map.put("code", 0);
        return map;

    }


    private Integer getId(String AccessToken) {
        Map<String, Claim> claimMap = JwtUtil.verifyAccessToken(AccessToken);
        String id = claimMap.get("userId").asString();
        Integer userId = Integer.parseInt(id);
        return userId;
    }

    private String getTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String time = sdf.format(date);
        return time;
    }

    public Map<String, Object> acceptTask(String accessToken, Integer id) {
        Map<String, Object> map = new HashMap<>();

        Integer userId = getId(accessToken);

        if (taskMapper.acceptTask(id, userId)) {

            map.put("code", 0);

        } else {
            map.put("code", 1);
        }
        return map;
    }

    @Transactional
    public Map<String, Object> cancelTask(String accessToken, Integer id) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);

        Task task = taskMapper.findTaskById(id);
        if (task.getAcceptId() == userId) {
            taskMapper.cancelTask(id);
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            map.put("code", 1);
            return map;
        }

        map.put("code", 0);
        return map;
    }

    @Transactional
    public Map<String, Object> deleteTask(String accessToken, Integer id) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);

        Task task = taskMapper.findTaskById(id);
        if (userId == task.getPublishId()) {
            taskMapper.deleteTask(id);
        } else {
            map.put("code", 5);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return map;
        }
        map.put("code", 0);
        return map;
    }

    @Transactional
    public Map<String, Object> cancelReportTask(String accessToken, Integer id, Integer taskId) {
        Integer id1 = getId(accessToken);
        Report report = reportMapper.findReportById(id);
        Map<String, Object> map = new HashMap<>();

        if (report == null || report.getReportId()!=id1) {
            map.put("code", 1);
            return map;
        }

//        取消举报    不成功
        if (!taskMapper.cancelReportTask(taskId)) {
            map.put("code", 1);
        }
//        删除举报  不成功
        else if (!reportMapper.deleteReport(id)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            map.put("code", 1);

        } else {
            map.put("code", 0);
        }
        return map;
    }


    public Map<String, Object> commentTask(String accessToken, String content, Integer taskId) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setTime(getTime());
        comment.setContent(content);
        comment.setIsReport(0);
        comment.setUserId(userId);
        if (commentMapper.addComment(comment)) {
            map.put("code", 0);
        } else {
            map.put("code", 1);
        }
        return map;
    }

    @Transactional
    public Map<String, Object> reportComment(String accessToken, Integer id, String reason) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);
        Report report = new Report();
        report.setTime(getTime());
        report.setReportId(userId);
        report.setReason(reason);
        reportMapper.addReport(report);
        if (commentMapper.reportComment(report.getId(), id)) {
            map.put("code", 0);
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            map.put("code", 1);
        }

        return map;
    }


    public Map<String, Object> deleteComment(String accessToken,Integer id) {
        Integer id1 = getId(accessToken);
//        System.out.println(id1);

        Map<String, Object> map = new HashMap<>();

        Comment comment = commentMapper.findById(id);

//        System.out.println(comment);

        if (comment == null) {
            map.put("code", 1);
            return map;
        }


        if (commentMapper.deleteComment(id)) {
            map.put("code", 0);
        } else {
            map.put("code", 1);
        }

        return map;
    }

    public Map<String, Object> getComments(Integer n,Integer taskId) {
        Map<String, Object> map = new HashMap<>();
        List<Comment> lists = commentMapper.getComments(n, taskId);
        map.put("comments", lists);
        map.put("code", 0);
        return map;
    }

    @Transactional
    public Map<String, Object> finishTask(String accessToken, Integer id) {
        Map<String, Object> map = new HashMap<>();
        Integer userId = getId(accessToken);
        Task task = taskMapper.findTaskById(id);
        if (task!=null&&userId == task.getAcceptId()) {
            taskMapper.finishTask(id);
            map.put("code", 0);
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            map.put("code", 1);
        }
        return map;
    }

    public Map<String, Object> getImg(String imgName) {
        Map<String, Object> map = new HashMap<>();
        String ImagePath = path + imgName;
        File file = new File(ImagePath);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            map.put("img", bytes);
            map.put("code", 0);
            return map;
        } catch (FileNotFoundException e) {
            map.put("code", -1);
            System.out.println("没有文件");
            return map;
        } catch (IOException e) {
            map.put("code", -1);
            e.printStackTrace();
            return map;
        }

    }
}
