package mmall.controller.backend;

import mmall.commons.Const;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.pojo.User;
import mmall.service.ICategoryService;
import mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录需要登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员增加我们处理的逻辑
            return iCategoryService.addCategory(categoryName, parentId);

        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }

    }
    @RequestMapping("set_CategoryName.do")
    @ResponseBody
    public ServiceResponse setCategoryName(HttpSession session ,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录需要登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员增加我们处理的逻辑
            //更新categoryName
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpServletResponse response,HttpSession session,@RequestParam(defaultValue = "0")Integer categoryId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录情登录");
        }
        response.setCharacterEncoding("utf-8");
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.selectCategoryChildrenByParentId(categoryId);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作需要管理员权限");
        }
    }


    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServiceResponse get_CategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(defaultValue = "0") Integer categoryId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录情登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            //查询当前节点id和下面递归节点的id
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作需要管理员权限");
        }
    }










}
