app.service("uploadService",function($http){
	this.uploadFile = function() {
		var formData=new FormData();
	    formData.append("file",file.files[0]);   
		return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        });		
	}
});

/*
 * 	headers: {'Content-Type':undefined},
 * 上传的是文件的时候，需要指定Content-Type为undefined，不指定，默认的是json类型
 * transformRequest: angular.identity，angular提供的对表单进行二进制序列化
 * 两者搭配使用，就是指定的上传文件的类型（multipart/form-data）
 * 
 */