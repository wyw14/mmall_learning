package mmall.service;

import mmall.commons.ServiceResponse;

public interface ICategoryService {
    public ServiceResponse addCategory(String categoryName, Integer parentId);

    public ServiceResponse updateCategoryName(Integer categoryId, String categoryName);

    public ServiceResponse selectCategoryChildrenByParentId(Integer categoryId);

    public ServiceResponse selectCategoryAndChildrenById(Integer categoryId);

}
