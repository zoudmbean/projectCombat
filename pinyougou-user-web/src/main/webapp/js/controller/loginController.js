 //控制层 
app.controller('loginController' ,function($scope,userService){	
	// 获取登录的用户名
	$scope.showName = function() {
		userService.showName().success(function(res) {
			if(res.loginName){
				console.log(res.loginName);
				$scope.loginName = res.loginName; // loginName
			}
		});
	}
	
});	
