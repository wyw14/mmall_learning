package mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import mmall.commons.Const;
import mmall.commons.ServiceResponse;
import mmall.pojo.User;
import mmall.service.IOrderService;
import mmall.service.IUserService;
import mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/order/manage/")
public class OrderaManageController {
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;
    @RequestMapping("order_info.do")
    @ResponseBody
    public ServiceResponse<PageInfo> orderInfo(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("请登录账号后再试");
        }
        ServiceResponse serviceResponse = iUserService.checkAdminRole(user);
        if (serviceResponse.isSuccess()){

            return iOrderService.manageList(pageNum,pageSize);
        }
        else return ServiceResponse.createByErrorMessage("无权限操作");
    }
    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse<OrderVo> detail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("请登录账号后再试");
        }
        ServiceResponse serviceResponse = iUserService.checkAdminRole(user);
        if (serviceResponse.isSuccess()){
            return iOrderService.manageGetOrderDetail(orderNo);
        }
        else return ServiceResponse.createByErrorMessage("无权限操作");
    }
    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse<PageInfo> search(HttpSession session, Long orderNo,
                                           @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("请登录账号后再试");
        }
        ServiceResponse serviceResponse = iUserService.checkAdminRole(user);
        if (serviceResponse.isSuccess()){
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }
        else return ServiceResponse.createByErrorMessage("无权限操作");
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServiceResponse<String> sendGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("请登录账号后再试");
        }
        ServiceResponse serviceResponse = iUserService.checkAdminRole(user);
        if (serviceResponse.isSuccess()){
            return iOrderService.manageSendGoods(orderNo);
        }
        else return ServiceResponse.createByErrorMessage("无权限操作");
    }
}
