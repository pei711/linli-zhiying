package com.hmdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.ScrollResult;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author 超大王
 * @since 2025-09-25
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final IUserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final IFollowService followService;

    public BlogServiceImpl(IUserService userService, StringRedisTemplate stringRedisTemplate, IFollowService followService) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.followService = followService;
    }

    @Override
    public Result queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不见啦啦啦啦");
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        // 3.查询blog是否被点赞
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    @Override
    public Result likeBlog(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前登录用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        //3.如果未点赞，可以点赞
        if(score == null){
            //未点赞，可以点赞
            //数据库点赞数+1
            boolean success = update().setSql("liked = liked + 1").eq("id", id).update();
            if(success){
                //保存用户到Redis的set集合
                stringRedisTemplate.opsForZSet().add(key,userId.toString(), System.currentTimeMillis());//时间戳
            }
        } else {
            //4.如果已经点赞，取消点赞
            boolean success = update().setSql("liked = liked - 1").eq("id", id).update();
            if(success){
                //保存用户到Redis的set集合
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        // 1.查询top5的点赞用户 zrange key 0 4
        Set<String> topFive = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(topFive == null || topFive.isEmpty()){
            return Result.ok();
        }
        // 2.解析出其中的用户id
        List<Long> ids = topFive.stream()
                .map(Long::valueOf)
                .toList();
        String idStr = StrUtil.join(",", ids);//用逗号拼接字符串
        // 3.根据用户id查询用户 WHERE id IN (5,1,2) ORDER BY FIELD(id, 5,1,2)
        List<UserDTO> userDTOS = userService
                .query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()//last拼接sql语句
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .toList();
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        blog.setUserId(userId);
        //2.保存探店笔记
        boolean success = save(blog);
        if(!success){
            return Result.fail("笔记保存失败咯~");
        }
        // 3.查询笔记作者的所有粉丝
        List<Follow> followedUsers = followService.query().eq("follow_user_id", userId).list();
        // 4.推送笔记id给所有粉丝
        for(Follow followedUser : followedUsers){
            Long followId = followedUser.getUserId();
            String key = RedisConstants.FEED_KEY + followId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
        return Result.ok(blog.getId());
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if(user == null){
            //用户未登录，无需查询
            return;
        }
        Long userId = user.getId();
        if (userId == null) {
            // 用户ID为空，同样无需查询
            return;
        }
        String key = RedisConstants.BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score != null){
            blog.setIsLike(true);
        }
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key max min WITHSCORES LIMIT offset count
        /*
         * key: 你要查询的有序集合的键名。在你的代码场景中，这可能是某个用户的 "收件箱"，例如 feed:userId。
         * max: 分数区间的最大值。命令会返回分数小于或等于 max 的成员。
         * min: 分数区间的最小值。命令会返回分数大于或等于 min 的成员。
         * [WITHSCORES] (可选): 如果加上这个选项，返回结果中不仅会包含成员（member），
         * 还会包含该成员对应的分数（score）。返回的列表会是 [member1, score1, member2, score2, ...] 的形式。
         * [LIMIT offset count] (可选): 这个选项用于分页。
         * offset: 表示从符合条件的成员中，跳过前 offset 个。
         * count: 表示在跳过 offset 个之后，最多返回 count 个成员
         */
        String key = RedisConstants.FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        // 3.非空判断
        if(typedTuples == null || typedTuples.isEmpty()){
            return Result.ok();
        }
        // 4.解析数据：blogId、minTime（时间戳）、offset（偏移量）
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1; //偏移量
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples){
            // 4.1.获取id
            ids.add(Long.valueOf(typedTuple.getValue()));
            // 4.2.获取分数（时间戳）
            long time = typedTuple.getScore().longValue();
            if(time == minTime){
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        os = minTime == max ? os + offset : os;
        /*
          在 max 这个时间点上，有非常多的数据，一页都装不下。
          所以下一次查询仍然需要从 max 这个时间点开始，但是需要跳过更多的数据。
          跳过的数量就是 os + offset（上次跳过的数量 + 这次新获取的数量）。
         */

        // 5.根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();

        blogs.stream().forEach(blog -> {
            // 5.1.查询blog有关的用户
            queryBlogUser(blog);
            // 5.2.查询blog是否被点赞
            isBlogLiked(blog);
        });

        // 6.封装并返回
        ScrollResult result = new ScrollResult();
        result.setList(blogs);
        result.setMinTime(minTime);
        result.setOffset(os);
        return Result.ok(result);
    }
}
