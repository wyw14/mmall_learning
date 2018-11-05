package mmall.service.impl;

import mmall.commons.Const;
import mmall.commons.ServiceResponse;
import mmall.dao.UserMapper;
import mmall.pojo.User;
import mmall.service.IUserService;
import mmall.util.MD5Util;
import mmall.util.RedisPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(@Param("username") String username,@Param("password") String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount==0){
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        String md5password=MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5password);
        if (user==null){
            return ServiceResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
       return ServiceResponse.createBySuccess("登陆成功",user);

        //todo 密码登录md5
    }

    public ServiceResponse<String>register(User user){
        ServiceResponse<String> checkValid = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!checkValid.isSuccess()){
            return checkValid;
        }
        checkValid = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!checkValid.isSuccess()){
            return checkValid;
        }
        int resultCount;
        user.setRole(Const.role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        resultCount=userMapper.insert(user);
        if (resultCount==0){
            ServiceResponse.createByErrorMessage("注册失败");
        }
        //md5加密
        return ServiceResponse.createSuccessByMessage("注册成功");
    }
    public ServiceResponse<String> checkValid(String str,String type ) {
        int resultCount=0;
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)){
                resultCount = userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServiceResponse.createByErrorMessage("用户名已被注册");
                }
            }
            if (Const.EMAIL.equals(type)){
                resultCount = userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServiceResponse.createByErrorMessage("邮箱已被使用");
                }
            }
        }
        return ServiceResponse.createSuccessByMessage("校验成功");
    }

    public ServiceResponse checkQuestion(String username){
        ServiceResponse checkValid = this.checkValid(username,Const.USERNAME);
        if (checkValid.isSuccess()){
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空的");
    }
    public ServiceResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username , question,answer);
        if(resultCount>0){
            String forgetToken= UUID.randomUUID().toString();
            //TokenCache.setkey(Const.TOKEN_PREFIX+username,forgetToken);
            RedisPoolUtils.setex(Const.TOKEN_PREFIX+username,forgetToken,60*60*12);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByErrorMessage("问题的答案错误");
    }

    public ServiceResponse<String> forgetResetPassword(String username,String newPassword,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServiceResponse.createByErrorMessage("参数错误,Token需要传递");
        }
        ServiceResponse checkValid = this.checkValid(username,Const.USERNAME);
        if (checkValid.isSuccess()){
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        //String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        String token=RedisPoolUtils.get(Const.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServiceResponse.createSuccessByMessage("token无效或过期");
        }
        if (StringUtils.equals(forgetToken,token)){
            String mdPassword=MD5Util.MD5EncodeUtf8(newPassword);
            int rowCount=userMapper.updatePasswordByUsername(username,mdPassword);

            if (rowCount>0){
                return ServiceResponse.createSuccessByMessage("修改密码成功");
            }
        }
        else {
            return ServiceResponse.createByErrorMessage("token错误请重新获取重置密码的token");
        }
        return ServiceResponse.createSuccessByMessage("密码修改失败");
    }

        public ServiceResponse<String> resetPassword(String oldPassword, String newPassword, User user){
            int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
            if (resultCount==0){
                return ServiceResponse.createSuccessByMessage("旧密码错误");
            }
            user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
            int updateCount = userMapper.updateByPrimaryKeySelective(user);
            if (updateCount>0){
                return ServiceResponse.createSuccessByMessage("密码更新成功");
            }
            return ServiceResponse.createByErrorMessage("密码更新失败");
        }


        public ServiceResponse<User> updateInfomation(User user){
            //username不能更改
            int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
            if (resultCount>0){
                return ServiceResponse.createByErrorMessage("email已经存在请更换email再尝试gengxin");
            }
            User updateUser=new User();
            updateUser.setId(user.getId());
            updateUser.setEmail(user.getEmail());
            updateUser.setPhone(user.getPhone());
            updateUser.setQuestion(user.getQuestion());
            updateUser.setAnswer(user.getAnswer());

            int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
            if (updateCount>0){
                return ServiceResponse.createBySuccess("更新个人信息成功",updateUser);

            }
            return ServiceResponse.createByErrorMessage("更新个人信息失败 ");
        }
        public  ServiceResponse<User> getInformation(Integer userId){
            User user=userMapper.selectByPrimaryKey(userId);
            if (user==null){
                return ServiceResponse.createByErrorMessage("找不到当前用户");
            }
            user.setPassword(StringUtils.EMPTY);
            return ServiceResponse.createBySuccess(user);
        }
    /*
    校验是否是管理员
     */
    @Override
    public ServiceResponse checkAdminRole(User user) {
        if (user!=null&&user.getRole().intValue()==Const.role.ROLE_ADMIN){
            return ServiceResponse.createBySuccess();
        }
        return  ServiceResponse.createByError();
    }
}
