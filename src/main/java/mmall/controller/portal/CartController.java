package mmall.controller.portal;

import mmall.commons.Const;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.pojo.User;
import mmall.service.ICartService;
import mmall.util.CookieUtils;
import mmall.util.JsonUtil;
import mmall.util.RedisPoolUtils;
import mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RequestMapping("/cart/")
@Controller
public class CartController {

    @Autowired
    private ICartService iCartService;
    @RequestMapping("add.do")
    @ResponseBody
    public ServiceResponse<CartVo> add(HttpSession session, Integer count, Integer productId, HttpServletRequest request){
        User user= JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }
    @RequestMapping("update.do")
    @ResponseBody
    public ServiceResponse<CartVo> update(HttpSession session, Integer count, Integer productId,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    @RequestMapping("delete.do")
    @ResponseBody
    public ServiceResponse<CartVo> deleteProdcut(HttpSession session, String productIds,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<CartVo> getList(HttpSession session){

        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }
    @RequestMapping("checkAll.do")
    @ResponseBody
    public ServiceResponse<CartVo> checkAll(HttpSession session,HttpServletRequest request){
       // User user= (User) session.getAttribute(Const.CURRENT_USER);
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.cart.CHECKED,null);
    }

    @RequestMapping("unCheckAll.do")
    @ResponseBody
    public ServiceResponse<CartVo> unCheckAll(HttpSession session, HttpServletRequest request){
      //  User user= (User) session.getAttribute(Const.CURRENT_USER);
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.cart.UN_CHECKED,null);
    }

    @RequestMapping("unCheck.do")
    @ResponseBody
    public ServiceResponse<CartVo> unCheck(HttpSession session,Integer productId,HttpServletRequest request){
      //  User user= (User) session.getAttribute(Const.CURRENT_USER);
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.cart.UN_CHECKED,productId);
    }
    @RequestMapping("Check.do")
    @ResponseBody
    public ServiceResponse<CartVo> Check(HttpSession session,Integer productId,HttpServletRequest request){
   //     User user= (User) session.getAttribute(Const.CURRENT_USER);
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.cart.CHECKED,productId);
    }

    @RequestMapping("count.do")
    @ResponseBody
    public ServiceResponse<Integer> count(HttpSession session,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        //    User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createBySuccess(0);
        }
        return iCartService.countProductNum(user.getId());
    }
}
