app.controller("baseController",function($scope){
	/*
	 * 分页控件
	 * */
	// 1. 分页控件配置 
	$scope.paginationConf = {
		 currentPage: 1,
		 totalItems: 10,
		 itemsPerPage: 10,
		 perPageOptions: [10, 20, 30, 40, 50],
		 onChange: function(){
			$scope.reloadList();
		 }
	}; 
	
	// 2. 刷新列表
	$scope.reloadList = function(){
		$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	/* 
	 * 3. 搜索实体bean定义
	 */
	$scope.seachEntity = {};
	
	/*
	 * 复选框勾选
	 * 
	 * */
	// 1. 定义一个变量用于记录用户选中的记录id
	$scope.selectIds = [];
	// 2. 定义选中函数
	$scope.updateSelection = function($event,id){
		if($event.target.checked){  // 如果复选框被选中了
			$scope.selectIds.push(id);
		} else {
			$scope.selectIds.splice($scope.selectIds.indexOf(id), 1);
		}
	}
	
	/*
	 * Json字符串获取指定的key的字符串
	 * */
	$scope.jsonToString = function(jsonstr,key) {
		if(jsonstr){
			var json=JSON.parse(jsonstr);//将json字符串转换为json对象
			var value="";
			for(var i=0;i<json.length;i++){		
				if(i>0){
					value+=","
				}
				value+=json[i][key];			
			}
			return value;

		}
	}
	
});