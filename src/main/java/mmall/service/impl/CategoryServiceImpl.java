package mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mmall.commons.ServiceResponse;
import mmall.dao.CategoryMapper;
import mmall.pojo.Category;
import mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServiceResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServiceResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setcategoryname(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServiceResponse.createSuccessByMessage("添加品类成功");
        }
        return ServiceResponse.createByErrorMessage("添加品类失败");
    }

    public ServiceResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServiceResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setcategoryname(categoryName);
        category.setId(categoryId);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServiceResponse.createSuccessByMessage("更新品类名称成功");
        } else {
            return ServiceResponse.createByErrorMessage("更新品类出错");
        }
    }


    public ServiceResponse<List<Category>> selectCategoryChildrenByParentId(Integer categoryId) {
        if (categoryId == null) {
            return ServiceResponse.createByErrorMessage("查询品类参数错误");
        }
       List<Category> categoryList= categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (categoryList.isEmpty()){
            logger.info("未找到当前分类的子节点");
        }
        return ServiceResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id以及孩子节点的id
     * @param categoryId
     * @return
     */
    public ServiceResponse selectCategoryAndChildrenById(Integer categoryId){
        Set<Category>categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId!=null){
            for (Category categoryItem:categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return  ServiceResponse.createBySuccess(categoryIdList);
    }



    private Set<Category> findChildCategory(Set<Category>categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }





}














