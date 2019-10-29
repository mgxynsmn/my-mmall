package com.mmall.service.impl;

import com.google.common.collect.Lists;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements ICategoryService {

    Logger logger  = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId==null|| StringUtils.isBlank(categoryName)){
            return ServerResponse.createBySuccessMessage("请求参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);  //新添加的品类默认是可以使用的

        int insertRow = categoryMapper.insert(category);
        if (insertRow>0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }

        return ServerResponse.createByErrorMessage("添加品类失败");
    }


    public ServerResponse setCategoryName(String categoryName,Integer categoryId){
        if (categoryId==null|| StringUtils.isBlank(categoryName)){
            return ServerResponse.createBySuccessMessage("请求参数错误");
        }

        Category category = new Category();

        category.setName(categoryName);
        category.setId(categoryId);

        int updateRow = categoryMapper.updateByPrimaryKeySelective(category);

        if (updateRow>0){
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
            return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParalelCategory(Integer categoryId) {
       List<Category> categoryList = categoryMapper.selectCategoryChildByParentId(categoryId);
       if (CollectionUtils.isEmpty(categoryList)){
           logger.info("未找到当前分类的子分类");
       }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId!=null){
            for (Category categoryItem:categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }


      //递归算法，算出子节点
      private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectCategoryChildByParentId(categoryId);
        for (Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
