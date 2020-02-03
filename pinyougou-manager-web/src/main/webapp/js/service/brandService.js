app.service("brandService",function($http){
	// 查询所有品牌列表
	this.findAll = function(){
		return $http.get("../brand/findAll.do");
	}
	
	// 查询品牌分页列表
	this.findPage = function(page,size,entity){
		return $http.post("../brand/findPage.do?page=" + page + "&size=" + size,entity);
	}
	
	// 添加
	this.add = function(entity){
		return $http.post("../brand/add.do",entity);
	}
	
	// 修改
	this.update = function(entity){
		return $http.post("../brand/update.do",entity);
	}
	
	// 查询单个品牌
	this.findOne = function(id){
		return $http.get("../brand/findOne.do?id="+id);
	}
	
	// 删除
	this.dele = function(ids){
		if(ids.length > 0){
			return $http.get("../brand/delete.do?ids="+ids);
		}
	}
	
	// 查询品牌信息
	this.selectOptionList = function(){
		return $http.get("../brand/selectOptionList.do");
	}
	
});