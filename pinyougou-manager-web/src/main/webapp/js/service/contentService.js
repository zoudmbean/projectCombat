//服务层
app.service('contentService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../content/findAll.do');		
	}
	//分页 
	this.findPage=function(entity,page,rows){
		return $http.post('../content/findPage.do?page='+page+'&rows='+rows,entity);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../content/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../content/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../content/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../content/delete.do?ids='+ids);
	}
});
