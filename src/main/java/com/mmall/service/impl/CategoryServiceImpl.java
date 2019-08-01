package com.mmall.service.impl;

import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if(parentId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByError("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if(rowCount<=0)
        {
            return ServerResponse.createByError("添加品类失败");
        }
        return ServerResponse.createBySucess("添加品类成功");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByError("更新品类参数错误");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount <= 0)
        {
            return ServerResponse.createByError("更新品类名称失败");
        }
        return ServerResponse.createBySucess("更新品类名称成功");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList))
        {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySucess(categoryList);
    }

    /**
     * 递归查询本结点的ID及孩子结点的ID
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> categoryIdList = categorySet.stream().map(category -> {
            return category.getId();
        }).collect(Collectors.toList());
        return ServerResponse.createBySucess(categoryIdList);
    }

    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId)
    {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null)
        {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category categoryItem:categoryList)
        {
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
