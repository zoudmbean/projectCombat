app.controller("indexController",function($scope,loginService){
	$scope.getLoginname = function() {
		loginService.getLoginname().success(function(data) {
			$scope.loginName = data.loginName;
		});
	}
})