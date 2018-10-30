package mmall.service;

import mmall.commons.ServiceResponse;
import mmall.pojo.User;
import org.springframework.stereotype.Service;

@Service
public interface IUserService {
    ServiceResponse<User> login(String username, String password);

    public ServiceResponse<String>register(User user);

    public ServiceResponse<String> checkValid(String str,String type );

    public ServiceResponse checkQuestion(String username);

    public ServiceResponse<String> checkAnswer(String username,String question,String answer);

    public ServiceResponse<String> forgetResetPassword(String username,String newPassword,String forgetToken);

    public ServiceResponse<String> resetPassword(String oldPassword, String newPassword, User user);

    public ServiceResponse<User> updateInfomation(User user);

    public  ServiceResponse<User> getInformation(Integer userId);

    public ServiceResponse checkAdminRole(User user);
}
