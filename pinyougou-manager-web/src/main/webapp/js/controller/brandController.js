app.controller("brandController",function($scope,$http,$controller,brandService){
	
	$controller("baseController",{$scope:$scope});
	
	// 查询所有品牌列表
	$scope.findAll = function(){
		brandService.findAll().success(function(response){
			$scope.list = response;
		});
	}
	
	// 查询品牌分页列表
	$scope.findPage = function(page,size){
		brandService.findPage(page,size,$scope.seachEntity).success(function(response){
			$scope.list = response.rows;						// 显示当前页数据
			$scope.paginationConf.totalItems = response.total;  // 修改总记录数
		});    			
	}
	
	// 新增/修改
	$scope.save = function(){
		var object = null;
		if($scope.entity.id){
			object = brandService.update($scope.entity);
		} else {
			object = brandService.add($scope.entity);
		}
		object.success(function(response){
			if(response.success){
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
		
	}
	
	// 根据id查询
	$scope.findOne = function(id){
		brandService.findOne(id).success(function(response){
			$scope.entity = response;
		});
	}
	
	// 删除
	$scope.dele = function(){
		brandService.dele($scope.selectIds).success(function(response){
			if(response.success){
				$scope.reloadList();
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
		});
	}
	
});