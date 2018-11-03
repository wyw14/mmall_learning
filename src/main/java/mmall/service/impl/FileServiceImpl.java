package mmall.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import mmall.service.IFileService;
import mmall.util.FTPUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {
    //private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){

        String fileName=file.getOriginalFilename();
        String fileExtensionName=fileName.substring(fileName.indexOf(".")+1);
        String uploadFileName= UUID.randomUUID().toString().replace("-","")+"."+fileExtensionName;
        log.info("开始上传文件,上传的文件名:{},上传的路径:{}新文件名:{}",fileName,path,uploadFileName);
        File fileDir=new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile=new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            //文件已经上传成功
            FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }



}
