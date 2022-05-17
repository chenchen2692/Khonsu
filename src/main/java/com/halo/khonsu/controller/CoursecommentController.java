package com.halo.khonsu.controller;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halo.khonsu.common.Result;
import com.halo.khonsu.entity.Coursecomment;
import com.halo.khonsu.service.ICoursecommentService;
import com.halo.khonsu.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author chen
 * @since 2022-05-17
 */
@RestController
@RequestMapping("/coursecomment")
public class CoursecommentController {


    @Resource
    private ICoursecommentService coursecommentService;

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Coursecomment coursecomment) {
        if (coursecomment.getId() == null) { // 新增评论
            coursecomment.setUserId(TokenUtils.getCurrentUser().getId());
            coursecomment.setTime(DateUtil.now());

            if (coursecomment.getPid() != null) {  // 判断如果是回复，进行处理
                Integer pid = coursecomment.getPid();
                Coursecomment pComment = coursecommentService.getById(pid);
                if (pComment.getOriginId() != null) {  // 如果当前回复的父级有祖宗，那么就设置相同的祖宗
                    coursecomment.setOriginId(pComment.getOriginId());
                } else {  // 否则就设置父级为当前回复的祖宗
                    coursecomment.setOriginId(coursecomment.getPid());
                }
            }

        }
        coursecommentService.saveOrUpdate( coursecomment);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        coursecommentService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        coursecommentService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/tree/{courseId}")
    public Result findTree(@PathVariable Integer courseId) {
        List<Coursecomment> courseComments = coursecommentService.findCommentDetail(courseId); // 查询所有的评论和回复数据
        // 查询评论数据（不包括回复）
        List<Coursecomment> originList = courseComments.stream().filter(coursecomment -> coursecomment.getOriginId() == null).collect(Collectors.toList());

        // 设置评论数据的子节点，也就是回复内容
        for (Coursecomment origin : originList) {
            List<Coursecomment> CourseComments = courseComments.stream().filter(coursecomment -> origin.getId().equals(coursecomment.getOriginId())).collect(Collectors.toList());  // 表示回复对象集合
            CourseComments.forEach(coursecomment -> {
                Optional<Coursecomment> pComment = courseComments.stream().filter(c1 -> c1.getId().equals(coursecomment.getPid())).findFirst();  // 找到当前评论的父级
                pComment.ifPresent((v -> {  // 找到父级评论的用户id和用户昵称，并设置给当前的回复对象
                    coursecomment.setPUserId(v.getUserId());
                    coursecomment.setPNickname(v.getNickname());
                }));
            });
            origin.setChildren(CourseComments);
        }
        return Result.success(originList);
    }

    @GetMapping
    public Result findAll() {
        return Result.success(coursecommentService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(coursecommentService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Coursecomment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return Result.success(coursecommentService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

