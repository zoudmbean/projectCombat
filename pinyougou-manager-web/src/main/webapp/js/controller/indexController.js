app.controller("indexController",function($scope,loginService){
	$scope.loginname = function() {
		loginService.loginname().success(function(data){
			$scope.loginname = data.loginName;
		});
	}
});