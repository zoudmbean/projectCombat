//服务层
app.service('goodsService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../goods/findAll.do');		
	}
	//分页 
	this.findPage=function(searchEntity,page,rows){
		return $http.post('../goods/findPage.do?page='+page+'&rows='+rows, searchEntity);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../goods/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../goods/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../goods/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../goods/delete.do?ids='+ids);
	}
	
	//删除
	this.deleGoods=function(ids){
		return $http.get('../goods/deleGoods.do?ids='+ids);
	}
	
	// 审核与驳回
	this.updateAuditStatus = function(ids,status) {
		return $http.get('../goods/updateAuditStatus.do?status='+status+"&ids="+ids);
	}
});
