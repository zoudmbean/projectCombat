// 定义模块
var app = angular.module("pinyougou",[]);

// 定义过滤器
app.filter("trustHtml",['$sce',function($sce){
	
	// data 表示被过滤的内容
	return function(data) {
		return $sce.trustAsHtml(data);
	}
}]);

