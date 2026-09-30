package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.Dto.KnowledgeArticleCreateDTO;
import com.lzc.mindaispringboot.Dto.KnowledgeArticlePageQuery;
import com.lzc.mindaispringboot.Dto.KnowledgeArticleUpdateDTO;
import com.lzc.mindaispringboot.entity.KnowledgeArticle;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.KnowledgeArticleMapper;
import com.lzc.mindaispringboot.mappper.KnowledgeCategoryMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
public class KnowledgeArticleService {
    @Resource
    private KnowledgeArticleMapper knowledgeArticleMapper;
    @Resource
    private KnowledgeCategoryMapper knowledgeCategoryMapper;
    public Page<KnowledgeArticle> page(KnowledgeArticlePageQuery query, boolean skipPublishFilter){
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (!skipPublishFilter){
            wrapper.eq(KnowledgeArticle :: getStatus, 1);
        }else if (query.getStatus() != null){
            wrapper.eq(KnowledgeArticle :: getStatus, query.getStatus());
        }
        if (query.getCategoryId() != null){
            wrapper.eq(KnowledgeArticle :: getCategoryId,query.getCategoryId());
        }
        if (StringUtils.hasText(query.getKeyword())){
            wrapper.and(w -> w.like(KnowledgeArticle :: getTitle,query.getKeyword())
                    .or().like(KnowledgeArticle :: getSummary,query.getKeyword())
            );
        }
        //排序：发布时间 > 创建时间降序
        wrapper.orderByDesc(KnowledgeArticle :: getPublishedAt)
                .orderByDesc(KnowledgeArticle :: getCreatedAt);
        Page<KnowledgeArticle> page = new Page<>(query.getPageNum(), query.getPageSize());
        return knowledgeArticleMapper.selectPage(page,wrapper);
    }
    //新增文章
    public void create(@Valid KnowledgeArticleCreateDTO dto, Long userId) {
        if (knowledgeCategoryMapper.selectById(dto.getCategoryId()) == null){
            throw new BusionessException("文章分类不存在");
        }
        boolean publish = dto.getStatus() == null || dto.getStatus() == 1;
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(UUID.randomUUID().toString())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .coverImage(dto.getCoverImage())
                .tags(dto.getTags())
                .authorId(userId)
                .readCount(0)
                .status(publish ? 1 : 0)
                .publishedAt(publish ? LocalDateTime.now() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        knowledgeArticleMapper.insert(article);
    }
    //更新文章
    public void update(String id,KnowledgeArticleUpdateDTO dto) {
        KnowledgeArticle knowledgeArticle = knowledgeArticleMapper.selectById(id);
        if (knowledgeArticle == null){
            throw new BusionessException("文章不存在");
        }
        if (dto.getCategoryId() != null && knowledgeCategoryMapper.selectById(dto.getCategoryId()) == null){
            throw new BusionessException("文章分类不存在");
        }
        BeanUtil.copyProperties(dto, knowledgeArticle, CopyOptions.create().ignoreNullValue());

        knowledgeArticle.setUpdatedAt(LocalDateTime.now());
        knowledgeArticleMapper.updateById(knowledgeArticle);
    }
    //更新状态 （发布 / 下架）
    public void updateStatus(String id, Integer status) {
        if (knowledgeArticleMapper.selectById(id) == null){
            throw new BusionessException("文章不存在");
        }
        if (status == null || (status != 0 && status != 1)){
            throw new BusionessException("状态值不合法（0:下架 1:发布）");
        }
        knowledgeArticleMapper.update(null,new LambdaUpdateWrapper<KnowledgeArticle>()
                .eq(KnowledgeArticle :: getId, id)
                .set(KnowledgeArticle :: getStatus, status)
                .set(status == 1,KnowledgeArticle :: getPublishedAt,LocalDateTime.now())
                .set(KnowledgeArticle :: getUpdatedAt,LocalDateTime.now())
        );
    }
    public KnowledgeArticle detail(String id, boolean skipPublishFilter,Long userId) {
        KnowledgeArticle Article_ID = knowledgeArticleMapper.selectById(id);
        if (Article_ID == null) {
            throw new BusionessException("文章不存在");
        }
        if (Article_ID.getStatus() == null || Article_ID.getStatus() != 1) {
            throw new BusionessException("文章未发布");
        }
        if (!skipPublishFilter){
            /// 转字符串
            String userIdStr = String.valueOf(userId);
            ///文章对象里取出上次记录的值
            String visitedUsers = Article_ID.getVisitedUsers();
            boolean alreadyVisited = visitedUsers != null
                    && Arrays.asList(visitedUsers.split(",")).contains(userIdStr);
            if (!alreadyVisited){
                //添加浏览量
                knowledgeArticleMapper.update(null,
                        new LambdaUpdateWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getId, id).setSql("read_count = read_count + 1")
                );
                Article_ID.setReadCount(Article_ID.getReadCount() == null ? 1 : Article_ID.getReadCount() + 1);
                String newVisitedUsers = visitedUsers == null
                        ? userIdStr : visitedUsers + "," + userIdStr;
                //更新用户访问的userid
                knowledgeArticleMapper.update(null,
                        new LambdaUpdateWrapper<KnowledgeArticle>()
                        .eq(KnowledgeArticle::getId, id)
                        .set(KnowledgeArticle::getVisitedUsers, newVisitedUsers));
            }
        }
        return Article_ID;
    }

    public void delete(String id) {
        if (knowledgeArticleMapper.selectById(id) == null){
            throw new BusionessException("文章不存在");
        }
        knowledgeArticleMapper.deleteById(id);
    }
}