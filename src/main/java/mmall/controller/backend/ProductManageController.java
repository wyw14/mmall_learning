package mmall.controller.backend;

import com.google.common.collect.Maps;
import mmall.commons.Const;
import mmall.commons.ServiceResponse;
import mmall.pojo.Product;
import mmall.pojo.User;
import mmall.service.IFileService;
import mmall.service.IProductService;
import mmall.service.IUserService;
import mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServiceResponse productSave(HttpSession session, Product product){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.saveOrUpdate(product);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
    }
    @RequestMapping("set_status.do")
    @ResponseBody
    public ServiceResponse setSellStatus(HttpSession session,Integer productId,Integer productStatus){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.setStatus(productId,productStatus);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse getProductDetail(HttpSession session,Integer productId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.manageProduct(productId);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
    }
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.getProductList(pageNum,pageSize);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
    }
    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse searchProduct(HttpServletResponse response,HttpSession  session,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize,@RequestParam(required = false) String productName,@RequestParam(required = false) Integer productId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            response.setCharacterEncoding("utf-8");
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
    }
    @RequestMapping("upload.do")
    @ResponseBody
    public ServiceResponse upload(HttpSession session,@RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServiceResponse.createByErrorMessage("用户未登录,请登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName=iFileService.upload(file,path);
            String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map fileMap= Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServiceResponse.createBySuccess(fileMap);
        }else {
            return ServiceResponse.createByErrorMessage("无操作权限,请登录管理员账号");
        }
        }

    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap=Maps.newHashMap();
        User user= (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName=iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }








}
