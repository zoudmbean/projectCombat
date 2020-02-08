package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

@RestController
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		try {
			String fullName = file.getOriginalFilename();
			String exeName = fullName.substring(fullName.lastIndexOf(".")+1);
			FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			String fileId = client.uploadFile(file.getBytes(),exeName);
			String picUrl = FILE_SERVER_URL + fileId; // 图片完整地址
			return new Result(true, picUrl);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败！");
		}
	}

}
