app.controller("searchController",function($scope,$location,searchService){
	
	// 搜索方法
	$scope.search = function() {
		
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo); // 转换成数字
		
		searchService.search($scope.searchMap).success(function(response){
			$scope.resultMap = response;
			// 分页
			buildPageLabel();
			
		});
	}
	
	// 构建分页栏
	function buildPageLabel(	){
		$scope.pageLabel=[];//新增分页栏属性
		var firstPage = 1;
		var lastPage = $scope.resultMap.totalPages;
		
		if($scope.resultMap.totalPages > 5){ // 如果总页数大于5
			
			if($scope.searchMap.pageNo <= 3){ // 如果当前页 <= 3 显示前5页
				lastPage = 5
			} else if($scope.searchMap.pageNo >= $scope.resultMap.totalPages-2){ // 如果当前页 >= 后两页  显示后5页
				firstPage = $scope.resultMap.totalPages - 4;
			} else {
				firstPage = $scope.searchMap.pageNo - 2 ;
				lastPage = $scope.searchMap.pageNo + 2 ;
			}
			
		}
		
		//循环产生页码标签	
		for(var i = firstPage ; i <= lastPage ; i++){
			$scope.pageLabel.push(i);
		}
	}
	
	/*
	 * 定义搜索面板数据结构  搜索关键字、分类、品牌、规格、价格区间
	 * */
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',"pageNo":1,"pageSize":40,"sort":'',"sortField":''};//搜索对象
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}	
		// 调用搜索方法
		$scope.search();
	}
	
	// 撤销选中项
	$scope.cancleSearchitem = function(key) {
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key]; // 删除为key的属性
		}	
		// 调用搜索方法
		$scope.search();
	}
	
	// 分页查询
	$scope.queryPage = function(page) {
		
		if(page < 1 || page > $scope.resultMap.totalPages){
			return;
		}
		
		$scope.searchMap.pageNo = page;
		$scope.search();
	}
	
	// 排序
	$scope.sortSearch = function(sort,sortField) {
		
		$scope.searchMap.sort = sort;
		$scope.searchMap.sortField = sortField;
		
		// 执行查询
		$scope.search();
	}
	
	// 隐藏品牌列表
	$scope.keywordsIsBrand = function() {
		var bList = $scope.resultMap.brandList;
		for(var i = 0 ; i < bList.length ; i ++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) != -1){
				return true;  // 表示存在
			}
		}
		return false;
		
	}
	
	// 接收首页的跳转请求
	$scope.loadkeywords = function() {
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();
	}
});