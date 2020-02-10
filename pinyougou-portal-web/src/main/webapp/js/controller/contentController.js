app.controller("contentController",function($scope,contentService){
	
	$scope.contentList=[];//广告集合
	// 根据广告类型ID查询列表
	$scope.findByCategoryId = function(categoryId) {
		contentService.findByCategoryId(categoryId).success(function(response) {
			$scope.contentList[categoryId] = response;
		});
	}
	
	// 首页搜索功能
	$scope.search = function() {
		location.href="http://localhost:9104/#?keywords="+$scope.keywords;
	}
});