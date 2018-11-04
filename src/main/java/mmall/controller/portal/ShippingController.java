package mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.pojo.Shipping;
import mmall.pojo.User;
import mmall.service.IShippingService;
import mmall.util.CookieUtils;
import mmall.util.JsonUtil;
import mmall.util.RedisPoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;
    @RequestMapping("add.do")
    @ResponseBody
    public ServiceResponse add(HttpSession session, Shipping shipping, HttpServletRequest request){
        User user= JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    @RequestMapping("del.do")
    @ResponseBody
    public ServiceResponse del(HttpSession session, Integer shippingId,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(),shippingId);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServiceResponse update(HttpSession session,Shipping shipping,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return  iShippingService.update(user.getId(),shipping);
    }
    @RequestMapping("select.do")
    @ResponseBody
    public ServiceResponse select(HttpSession session,Integer shippingId,HttpServletRequest request){
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if (user==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return  iShippingService.select(user.getId(),shippingId);
    }
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                         HttpSession session,HttpServletRequest request){
        //User user = (User)session.getAttribute(Const.CURRENT_USER);
        User user=JsonUtil.string2Obj( RedisPoolUtils.get(CookieUtils.readCookie(request)),User.class);
        if(user ==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }



}
